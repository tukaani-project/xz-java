// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.delta;

public class DeltaDecoder extends DeltaCoder {
    public DeltaDecoder(int distance) {
        super(distance);
    }

    public void decode(byte[] buf, int off, int len) {
        int i=0;
        // first process from history buffer
        for (int j = Math.min(len, distance); i < j; ++i) {
            buf[off + i] += history[i];
        }
        
        // then process rest just within buf
        for ( ; i<len; ++i) {
            buf[off + i] += buf[off + i - distance];
        }

        // finally, populate the history buffer
        if (len >= distance) {
            System.arraycopy(buf, off + len - distance, history, 0, distance);
        } else {
            assert i == len;
            // copy from end of buffer to beginning
            System.arraycopy(history, i, history, 0, distance - i);
            // now copy all of in to the end of the buffer
            System.arraycopy(buf, off, history, distance - i, len);
        }
    }
}
