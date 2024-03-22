// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Brett Okken <brett.okken.os@gmail.com>
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.check;

import org.tukaani.xz.common.ByteArrayView;

public class CRC64 extends Check {
    private static final long[] TABLE = new long[4 * 256];

    static {
        final long poly64 = 0xC96C5795D7870F42L;

        for (int s = 0; s < 0x400; s += 0x100) {
            for (int b = 0; b < 0x100; ++b) {
                long r = s == 0 ? b : TABLE[s - 0x100 + b];
                for (int i = 0; i < 8; ++i) {
                    if ((r & 1) == 1) {
                        r = (r >>> 1) ^ poly64;
                    } else {
                        r >>>= 1;
                    }
                }
                TABLE[s + b] = r;
            }
        }
    }

    private long crc = -1;

    public CRC64() {
        size = 8;
        name = "CRC64";
    }

    @Override
    public void update(byte[] buf, int off, int len) {
        final int end = off + len;
        int i = off;

        while ((i & 3) != ByteArrayView.ALIGN_INT && i < end)
            crc = TABLE[(buf[i++] & 0xFF) ^ ((int)crc & 0xFF)] ^ (crc >>> 8);

        // If end is small then end4 might become negative. That is fine.
        for (int end4 = ((end - ByteArrayView.ALIGN_INT) & ~3); i < end4;
                i += 4) {
            final int tmp = ByteArrayView.getIntLE(buf, i) ^ (int)crc;
            crc = TABLE[0x300 + (tmp & 0xFF)] ^
                  TABLE[0x200 + ((tmp >>> 8) & 0xFF)] ^
                  (crc >>> 32) ^
                  TABLE[0x100 + ((tmp >>> 16) & 0xFF)] ^
                  TABLE[((tmp >>> 24) & 0xFF)];
        }

        while (i < end)
            crc = TABLE[(buf[i++] & 0xFF) ^ ((int)crc & 0xFF)] ^ (crc >>> 8);
    }

    @Override
    public byte[] finish() {
        byte[] buf = new byte[8];
        ByteArrayView.setLongLE(buf, 0, ~crc);
        crc = -1;
        return buf;
    }
}
