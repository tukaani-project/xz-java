// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.check;

public class CRC32 extends Check {
    private final java.util.zip.CRC32 state = new java.util.zip.CRC32();

    public CRC32() {
        size = 4;
        name = "CRC32";
    }

    @Override
    public void update(byte[] buf, int off, int len) {
        state.update(buf, off, len);
    }

    @Override
    public byte[] finish() {
        long value = state.getValue();
        byte[] buf = { (byte)(value),
                       (byte)(value >>> 8),
                       (byte)(value >>> 16),
                       (byte)(value >>> 24) };
        state.reset();
        return buf;
    }
}
