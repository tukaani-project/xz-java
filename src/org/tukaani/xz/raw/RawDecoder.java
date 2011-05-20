/*
 * RawDecoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.raw;

import java.io.InputStream;
import org.tukaani.xz.UnsupportedOptionsException;

public class RawDecoder extends RawCoder {
    private FilterDecoder[] filters;

    public RawDecoder(long[] filterIDs, byte[][] filterProps)
            throws UnsupportedOptionsException {
        filters = new FilterDecoder[filterIDs.length];

        for (int i = 0; i < filterIDs.length; ++i) {
            if (filterIDs[i] == LZMA2Coder.FILTER_ID)
                filters[i] = new LZMA2Decoder(filterProps[i]);

            else if (filterIDs[i] == DeltaCoder.FILTER_ID)
                filters[i] = new DeltaDecoder(filterProps[i]);

            else
                throw new UnsupportedOptionsException(
                        "Unknown Filter ID " + filterIDs[i]);
        }

        validate(filters);
    }

    public int getMemoryUsage() {
        int memoryUsage = 0;
        for (int i = 0; i < filters.length; ++i)
            memoryUsage += filters[i].getMemoryUsage();

        return memoryUsage;
    }

    public InputStream getFilterChain(InputStream in) {
        for (int i = filters.length - 1; i >= 0; --i)
            in = filters[i].getInputStream(in);

        return in;
    }
}
