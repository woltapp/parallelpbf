package akashihi.osm.parallelpbf.entity;

import lombok.Data;

import java.util.List;

/**
 * OSM PBF file header. Is not a part of the OSM v0.6 API.
 */
@Data
public class Header {
    /**
     * List of features, required to read and process PBF data.
     */
    private final List<String> requiredFeatures;
    /**
     * List of optional features, that mey be present in PBF file.
     *
     * Please, pay attention, that those features may include soring features,
     * like 'Sort.Type_then_ID', 'Sort.Geographic' etc.
     * Due to the asynchronous nature of parallel processing,
     * that order is not guaranteed to be kept during
     * reading procedure.
     */
    private final List<String> optionalFeatures;

    /**
     * Optional name of the PBF file's origination program. May be null.
     */
    private String writingProgram;

    /**
     * Optional source information. May be null.
     */
    private String source;
}
