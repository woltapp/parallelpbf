/*
 * This file is part of parallelpbf.
 *
 *     parallelpbf is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wolt.osm.parallelpbf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.wolt.osm.parallelpbf.entity.*;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class ParalelBinaryParserExample {

    private final StringBuilder output = new StringBuilder();
    private final AtomicLong nodesCounter = new AtomicLong();
    private final AtomicLong waysCounter = new AtomicLong();
    private final AtomicLong relationsCounter = new AtomicLong();
    private final AtomicLong changesetsCounter = new AtomicLong();

    private void processHeader(Header header) {
        synchronized (output) {
            output.append(header);
            output.append("\n");
        }
    }

    private void processBoundingBox(BoundBox bbox) {
        synchronized (output) {
            output.append(bbox);
            output.append("\n");
        }
    }

    private void processNodes(Node node) {
        nodesCounter.incrementAndGet();
    }

    private void processWays(Way way) {
        waysCounter.incrementAndGet();
    }

    private void processRelations(Relation way) {
        relationsCounter.incrementAndGet();
    }

    private void processChangesets(Long id) {
        changesetsCounter.incrementAndGet();
    }

    private void printOnCompletions() {
        output.append("Node count: ");
        output.append(nodesCounter.get());
        output.append("\n");

        output.append("Way count: ");
        output.append(waysCounter.get());
        output.append("\n");

        output.append("Relations count: ");
        output.append(relationsCounter.get());
        output.append("\n");

        output.append("Changesets count: ");
        output.append(changesetsCounter.get());
        output.append("\n");

        System.out.println("Reading results:");
        System.out.println(output);
    }

    private void execute() throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.pbf");
        new ParallelBinaryParser(input, 1)
                .onHeader(this::processHeader)
                .onBoundBox(this::processBoundingBox)
                .onComplete(this::printOnCompletions)
                .onNode(this::processNodes)
                .onWay(this::processWays)
                .onRelation(this::processRelations)
                .onChangeset(this::processChangesets)
                .parse();
    }

    public static void main(String[] args) throws Exception {
        new ParalelBinaryParserExample().execute();
    }
}
