// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.delta;

public class DeltaEncoder extends DeltaCoder {
    public DeltaEncoder(int distance) {
        super(distance);
    }

    public void encode(byte[] in, int in_off, int len, byte[] out) {
        for (int i = 0; i < len; ++i) {
            byte tmp = history[(distance + pos) & DISTANCE_MASK];
            history[pos-- & DISTANCE_MASK] = in[in_off + i];
            out[i] = (byte)(in[in_off + i] - tmp);
        }
    }
}
