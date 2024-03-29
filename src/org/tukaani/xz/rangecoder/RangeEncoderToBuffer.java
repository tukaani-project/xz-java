// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>
// SPDX-FileContributor: Igor Pavlov <https://7-zip.org/>

package org.tukaani.xz.rangecoder;

import java.io.OutputStream;
import java.io.IOException;
import org.tukaani.xz.ArrayCache;

public final class RangeEncoderToBuffer extends RangeEncoder {
    private final byte[] buf;
    private int bufPos;

    public RangeEncoderToBuffer(int bufSize, ArrayCache arrayCache) {
        buf = arrayCache.getByteArray(bufSize, false);
        reset();
    }

    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(buf);
    }

    public void reset() {
        super.reset();
        bufPos = 0;
    }

    public int getPendingSize() {
        // With LZMA2 it is known that cacheSize fits into an int.
        return bufPos + (int)cacheSize + 5 - 1;
    }

    public int finish() {
        // super.finish() cannot throw an IOException because writeByte()
        // provided in this file cannot throw an IOException.
        try {
            super.finish();
        } catch (IOException e) {
            throw new Error();
        }

        return bufPos;
    }

    public void write(OutputStream out) throws IOException {
        out.write(buf, 0, bufPos);
    }

    @Override
    void writeByte(int b) {
        buf[bufPos++] = (byte)b;
    }
}
