// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>
// SPDX-FileContributor: Igor Pavlov <https://7-zip.org/>

package org.tukaani.xz.simple;

// BCJ filter for little endian ARM-Thumb instructions
public final class ARMThumb implements SimpleFilter {
    private final boolean isEncoder;
    private int pos;

    public ARMThumb(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        pos = startPos + 4;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int end = off + len - 4;
        int i;

        for (i = off; i <= end; i += 2) {
            if ((buf[i + 1] & 0xF8) == 0xF0 && (buf[i + 3] & 0xF8) == 0xF8) {
                int src = ((buf[i + 1] & 0x07) << 19)
                          | ((buf[i] & 0xFF) << 11)
                          | ((buf[i + 3] & 0x07) << 8)
                          | (buf[i + 2] & 0xFF);
                src <<= 1;

                int dest;
                if (isEncoder)
                    dest = src + (pos + i - off);
                else
                    dest = src - (pos + i - off);

                dest >>>= 1;
                buf[i + 1] = (byte)(0xF0 | ((dest >>> 19) & 0x07));
                buf[i] = (byte)(dest >>> 11);
                buf[i + 3] = (byte)(0xF8 | ((dest >>> 8) & 0x07));
                buf[i + 2] = (byte)dest;
                i += 2;
            }
        }

        i -= off;
        pos += i;
        return i;
    }
}
