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

package com.wolt.osm.parallelpbf.blob;

import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Fileformat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Handles all stream operations and retrieves Blob* messages.
 */
@Slf4j
@RequiredArgsConstructor
public final class BlobReader {
    /**
     * The size field have fixed length of 4 bytes.
     */
    private static final int SIZE_FIELD_LENGTH = 4;

    /**
     * BlobHeader is never bigger then 64K.
     */
    private static final int MAX_HEADER_SIZE = 64 * 1024;

    /**
     * Blob is never bigger then 32M.
     */
    private static final int MAX_BLOB_SIZE = 32 * 1024 * 1024;

    /**
     * Input data stream.
     */
    private final InputStream input;

    /**
     * Just tries to read specified amount of bytes from the stream.
     * @param bytesToRead how many bytes should be read.
     * @return Buffer of bytesToRead size or empty,
     *         in case of EOF or IOException.
     */
    private Optional<byte[]> readFromStream(final int bytesToRead) {
        byte[] buffer = new byte[bytesToRead];
        try {
            int bytesRead = input.read(buffer);
            if (bytesRead != bytesToRead) {
                return Optional.empty();
            }
        } catch (IOException e) {
            log.error("Error reading from the stream: {}", e.getMessage(), e);
            return Optional.empty();
        }
        return Optional.of(buffer);
    }

    /**
     * Reads next blob header length from the current stream position.
     * As blob header length is just 4 bytes in network byte order,
     * this functions makes no checks and will return garbage
     * if called within a wrong stream position.
     *
     * @return length of next block header or empty if can't be read.
     */
    public Optional<Integer> readBlobHeaderLength() {
        Optional<byte[]> blobHeaderLengthBuffer = readFromStream(SIZE_FIELD_LENGTH);
        Optional<Integer> result = blobHeaderLengthBuffer.map(value -> {
            ByteBuffer blobHeaderLengthWrapped = ByteBuffer.wrap(value);
            int blobHeaderLength = blobHeaderLengthWrapped.getInt();
            log.trace("Read BlobHeaderLength: {}", blobHeaderLength);
            return blobHeaderLength;
        });
        return result.flatMap(value -> {
            if (value > MAX_HEADER_SIZE) {
                log.warn("BlobHeader size is too big: {}", value);
                return Optional.empty();
            } else {
                return result;
            }
        });
    }

    /**
     * Reads next blob header from the current stream position. Size of the header is
     * specified in the parameters. As BlobHeader is a protobuf entity, basic validity checking
     * is made and 0 will be returned in case of failure. Same 0 will be returned if header can't be read fully
     * or eof is reached.
     *
     * @param headerLength Number of bytes to read and interpret as BlobHeader
     * @return Size of the following Blob in bytes or empty in case of read error.
     */
    public Optional<BlobInformation> readBlobHeader(final int headerLength) {
        Optional<byte[]> blobHeaderBuffer = readFromStream(headerLength);
        Optional<BlobInformation> result = blobHeaderBuffer.flatMap(value -> {
            Fileformat.BlobHeader header;
            try {
                header = Fileformat.BlobHeader.parseFrom(blobHeaderBuffer.get());
                log.trace("Got BlobHeader with type: {}, data size: {}", header.getType(), header.getDatasize());
                return Optional.of(new BlobInformation(header.getDatasize(), header.getType()));
            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse BlobHeader: {}", e.getMessage(), e);
                return Optional.empty();
            }
        });
        return result.flatMap(value -> {
            if (value.getSize() > MAX_BLOB_SIZE) {
                log.warn("Blob size is too big: {}", value);
                return Optional.empty();
            } else {
                return result;
            }
        });
    }

    /**
     * Reads next blob from the current stream position. Size of the blob is
     * specified in the parameters.
     * @param blobLength Number of bytes to read
     * @return Blob value or empty in case of read error
     */
    public Optional<byte[]> readBlob(final int blobLength) {
        return readFromStream(blobLength);
    }

    /**
     * Fast forwards input stream to the offset. Used in conjunction with
     * partitioning.
     * @param offset Number of bytes to skip from the stream.
     * @return Optional with `offset` value or empty in case of failure.
     */
    public Optional<Integer> skip(final Integer offset) {
        long left = offset;
        try {
            while (left != 0) {
                long skipped = input.skip(left);
                left -= skipped;
            }
            return Optional.of(offset);
        } catch (IOException e) {
            log.error("Error fast forwarding the stream: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
