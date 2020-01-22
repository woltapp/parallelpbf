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

package com.wolt.osm.parallelpbf.io;

import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;
import com.wolt.osm.parallelpbf.entity.BoundBox;
import com.wolt.osm.parallelpbf.entity.Header;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * Implemented parser for OSMHeader message.
 *
 * @see Header
 */
@Slf4j
public final class OSMHeaderReader extends OSMReader {
    /**
     * Conversion from nano- to non-scaled.
     */
    private static final double NANO = 1e9;

    /**
     * Header processing callback. Must be reentrant.
     */
    private final Consumer<Header> headerCb;

    /**
     * Bounding box processing callback. Must be reentrant.
     */
    private final Consumer<BoundBox> boundBoxCb;

    /**
     * Constructs reader object.
     *
     * @param blob         blob to parse.
     * @param tasksLimiter task limiting semaphore.
     * @param onHeader     Callback to call with a filled Header entity.
     *                     Header parsing will be partially skipped if set to null.
     * @param onBoundBox   Callback to call if bounding box present in header.
     *                     Bounding box parsing will be skipped completely if set to null
     */
    public OSMHeaderReader(final byte[] blob,
                    final Semaphore tasksLimiter,
                    final Consumer<Header> onHeader,
                    final Consumer<BoundBox> onBoundBox) {
        super(blob, tasksLimiter);
        this.headerCb = onHeader;
        this.boundBoxCb = onBoundBox;
    }

    /**
     * Check, that all required features are supported by that parser.
     *
     * @param features Features list.
     * @return true if all required features are supported, false otherwise.
     */
    private boolean checkRequiredFeatures(final List<String> features) {
        Optional<String> unsupported = features.stream()
                .filter(f -> !f.equalsIgnoreCase(Header.FEATURE_OSM_SCHEMA))
                .filter(f -> !f.equalsIgnoreCase(Header.FEATURE_DENSE_NODES))
                .filter(f -> !f.equalsIgnoreCase(Header.FEATURE_HISTORICAL_INFORMATION))
                .findAny();
        unsupported.ifPresent(s -> log.error("Unsupported required feature found: {}", s));
        return !unsupported.isPresent();
    }

    /**
     * Parses OSMHeader value and checks required feature list from it.
     * <p>
     * Check for required features is mandatory and is actually a reason, why we can't
     * just skip parsing if no callbacks are set.
     *
     * @param message Raw OSMHeader blob.
     */
    @Override
    protected void read(final byte[] message) {
        Osmformat.HeaderBlock headerData;
        try {
            headerData = Osmformat.HeaderBlock.parseFrom(message);
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing OSMHeader block: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        if (!checkRequiredFeatures(headerData.getRequiredFeaturesList())) {
            throw new RuntimeException("Can't proceed with unsupported features");
        }

        if (headerCb != null) {
            Header header = new Header(headerData.getRequiredFeaturesList(), headerData.getOptionalFeaturesList());
            if (headerData.hasWritingprogram()) {
                header.setWritingProgram(headerData.getWritingprogram());
            }
            if (headerData.hasSource()) {
                header.setSource(headerData.getSource());
            }
            log.debug("Header: {}", header.toString());
            headerCb.accept(header);
        }

        if (boundBoxCb != null && headerData.hasBbox()) {
            BoundBox bbox = new BoundBox(headerData.getBbox().getLeft() / NANO,
                    headerData.getBbox().getTop() / NANO,
                    headerData.getBbox().getRight() / NANO,
                    headerData.getBbox().getBottom() / NANO);
            log.debug("Bounding box: {}", bbox.toString());
            boundBoxCb.accept(bbox);
        }
    }
}
