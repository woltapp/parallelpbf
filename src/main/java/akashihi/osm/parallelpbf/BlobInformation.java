package akashihi.osm.parallelpbf;

/**
 * A better wrapper over BlobHeader.
 *
 * Keeps blob size and blob type together for future processing.
 */
class BlobInformation {
    private final Integer size;
    private final String type;

    BlobInformation(Integer size, String type) {
        this.size = size;
        this.type = type;
    }

    Integer getSize() {
        return size;
    }

    public String getType() {
        return type;
    }
}
