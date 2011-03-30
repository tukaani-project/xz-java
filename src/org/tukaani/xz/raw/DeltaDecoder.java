/*
 * DeltaDecoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.raw;

import java.io.InputStream;
import org.tukaani.xz.DeltaInputStream;
import org.tukaani.xz.UnsupportedOptionsException;

class DeltaDecoder extends DeltaCoder implements FilterDecoder {
    private int distance;

    DeltaDecoder(byte[] props) throws UnsupportedOptionsException {
        if (props.length != 1)
            throw new UnsupportedOptionsException(
                    "Unsupported Delta filter properties");

        distance = (props[0] & 0xFF) + 1;
    }

    public int getMemoryUsage() {
        return 1;
    }

    public InputStream getInputStream(InputStream in) {
        return new DeltaInputStream(in, distance);
    }
}
