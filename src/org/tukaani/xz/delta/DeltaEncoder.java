// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.delta;

public class DeltaEncoder extends DeltaCoder {
    public DeltaEncoder(int distance) {
        super(distance);
    }

    public void encode(byte[] in, int in_off, int len, byte[] out) {
        int i=0;
        // first deal with comparisons to the history buffer
        for (int j=Math.min(len, distance); i<j; ++i) {
            out[i] = (byte) (in[in_off + i] - history[i]);
        }
        // now fill the history buffer with the final (distance) bytes in source
        if (len >= distance) {
            System.arraycopy(in, in_off + len - distance, history, 0, distance);
        } else {
            assert i == len;
            // copy from end of history buffer to beginning
            System.arraycopy(history, i, history, 0, distance - i);
            // now copy all of "in" to end of history buffer
            System.arraycopy(in, in_off, history, distance - i, len);
        }

        for ( ; i < len; ++i) {
            out[i] = (byte) (in[in_off + i] - in[in_off + i - distance]);
        }
    }
}
