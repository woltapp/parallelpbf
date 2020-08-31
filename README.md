# Parallel OSM PBF parser

[OSM PBF format](https://wiki.openstreetmap.org/wiki/PBF_Format) multithreaded reader/writer written in Java. Supports all 
current OSM PBF features and options (only for reading)

## Rationale


The OSMPBF format consists of sequence of independent blobs, containing actual OSM data. All existing Java readers
of OSMPBF format read that file sequentially, processing each blob one by one using just a single thread. 
Parsing single blob usually involves decompressing it and calculating OSM entity values from a delta-packed
data ([check wiki](https://wiki.openstreetmap.org/wiki/PBF_Format) for details). Obviously it is more
CPU bound task, than IO bound task, so loading CPU up should speed up the processing. The Simplest way to do that
is to distribute the work on all the cores. And here we go...

## Download

### Maven 

```xml
<dependency>
    <groupId>com.wolt.osm</groupId>
    <artifactId>parallelpbf</artifactId>
    <version>0.3.1</version>
</dependency>
```
        
### Gradle

```gradle
compile group: 'com.wolt.osm', name: 'parallelpbf', version: '0.3.1'
```
        
### SBT 

```sbt
libraryDependencies += "com.wolt.osm" % "parallelpbf" % "0.3.1"
```
        
### GitHub release

→ https://github.com/woltapp/parallelpbf/releases/tag/v0.3.1
        
## Reading                
        
As parsing is asynchronous, it heavily relies on the callbacks. There are 7 callbacks defined:

* `Consumer<Node> onNode` - is called for each Node in the OSM PBF file. This callback must be reenterable as it will be 
called simultaneously from the different parallel executing threads.

* `Consumer<Way> onWay` - is called for each Way in the OSM PBF file. This callback must be reenterable as it will be 
called simultaneously from the different parallel executing threads.

* `Consumer<Relation> onRelation` - is called for each Relation in the OSM PBF file. This callback must be reenterable as it will be 
called simultaneously from the different parallel executing threads.

* `Consumer<Changeset> onChangeSet` - is called for each ChangeSet in the OSM PBF file. This callback must be reenterable as it will be 
called simultaneously from the different parallel executing threads.

* `Consumer<Header> onHeader` - is called for the Header object of the OSM PBF file. Each OSM PBF file should have just a single Header object,
so it is safe to assume, that this callback will be called just once.

* `Consumer<BoundBox> onBoundBox` - is called for the Bounding box object of the OSM PBF file. OSM file may not have BoundBox object written,
so this callback may never be called. As BoundBox is a part of OSMPBF Header object, it is safe to assume, that this callback will be called just once.

* `Runnable onComplete` - called only in case of successful parse completion. All the other callbacks are guaranteed 
to finish before calling `onComplete` and no other callbacks will happen after `onComplete` call.

Callbacks can be attached to the parser using appropriate calls:

```java
InputStream input = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("sample.pbf");

new ParallelBinaryParser(input, 1)
        .onHeader(this::processHeader)
        .onBoundBox(this::processBoundingBox)
        .onComplete(this::printOnCompletions)
        .onNode(this::processNodes)
        .onWay(this::processWays)
        .onRelation(this::processRelations)
        .onChangeset(this::processChangesets)
        .parse();
```

All callbacks are optional, if you do not set some callback, nothing will break. Parsing of data for missing callback 
will be skipped. So, for example, if you need just relations data, you should not set other callbacks and data blocks carrying
other types of OSM data will be skipped completely, thus saving processing time. 
There is an exception from that rule - Header data block is always parsed, even if no callback is set.
Even more, if no Node/Way/Relation/Changeset callbacks will be set,  actual processing of data will be skipped 
after finding first Header block. 

`ParallelBinaryParser` constructor accepts two mandatory arguments:

* `InputStream input` - InputStream pointing to the beginning of the OSMPBF data. 
* `int threads` - Number of threads for parallel processing. Parser will automatically throttle and stop input 
reading if all threads are busy. Each thread keeps blob data in memory, so memory usage will be at least
64MB per thread, but probably couple of hundreds megabytes per thread, depending on a block content.

There are also two optional arguments for partitioning support:

* `noPartitions` - Total number of partitions processed file should be divided.
* `myShard` - Number of partition, associated with the this instance of the parser.

The partitioning is added to support multi-host parallel loading, when each hosts reads it's own amount of data 
independently and then somehow combines that data or continues processing it on each hosts separately. The whole idea
of partitioning here is that we split up the file to some number of partitions or shard and only process OSMData blocks  
from our 'own' shard, skipping all data blocks belonging to the other shard. Even with partitioning enabled, the whole
InputStream will be processed and all OSMHeader blocks will be read and analyzed.

To start actually processing the input stream, you should call `parse()` function. It will create all required threads
and start data reading from the input and parsing it. That function is intentionally blocking, but it is safe to 
wrap it to some other thread and wait for completion using `onComplete` callback.  

### Warning on order instability

OSM PBF file can be sorted and stored in a ordered way. Unfortunately, due to parallel nature of the parser, that 
ordering will be broken during parsing and several consequent parse runs may return data in a different order for 
each run. In case order is important for you, you can either sort after parse or switch back to the single threaded
parsers. 


### Performance comparision

| Region         | Size in GB | Single thread read time in seconds | 24 threads read time in seconds |
|----------------|------------|------------------------------------|---------------------------------|
| Czech republic |  0.7       |  133                               | 40                              |
| Asia           |  7.3       |  2381                              | 405                             |
| Europe         |  21        |  3545                              | 953                             |
| Planet         |  47        |  8204                              | 3203                            |

## Writing

Write API differs from the Reading API, as it makes no sense to use callbacks here. The writer object provides three
methods to start writing, feed the writer with data and close writer. Writing function itself is thread-safe and reenterable,
so can be used from parallel threads.

So the correct workflow will be:

```java
writer = new ParallelBinaryWriter(output,1, bbox);
writer.start();
writer.write(node);
writer.close();
```

`ParallelBinaryWriter` accepts two mandatory arguments and one optional:

* `OutputStream output` - OutputStream that will hold OSM PBF data.
* `int threads` - Number of threads for parallel processing. Writer will automatically throttle and block on `.write()` call if all threads are busy. Each thread keeps blob data in memory, so memory usage will be at least 16MB per thread, but may be more, depending on block content.
* `BoundBox boundBox` - Optional BoundBox of the data to be written, can be `null`

OSM PBF header will be written to the OutputStream during construction.

`.start()` call actually spawns writing threads and allows to make `.write()` calls. The `.start()` call is not thread-safe.

`.write(OsmEntity)` call sends specified entity to one of the writing threads. This call is thread safe and calling it in parallel
is recommended. In case of writing threads overload, the `.write()` call will block and wait for an empty writing thread to handle request.

`.close()` will flush block to the output stream and terminate writing threads. Writer should not be used after calling `.close()`
on it. 

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/akashihi/parallelpbf/tags). 

## Authors

* **Denis Chaplygin** - *Initial work* - [akashihi](https://github.com/akashihi)
* **Scott Crosby** - *.proto definition files* - [scrosby](https://github.com/openstreetmap/OSM-binary) 

## License

This project is licensed under the GPLv3 License - see the LICENSE file for details.
The .proto definition files are licensed under the MIT license.
