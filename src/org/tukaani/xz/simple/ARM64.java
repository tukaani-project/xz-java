/*
 * BCJ filter for little endian ARM64 instructions
 *
 * Authors: Jia Tan <jiat0218@gmail.com>
 *          Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.simple;

public final class ARM64 implements SimpleFilter {
    private final boolean isEncoder;
    private int pos;

    public ARM64(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        pos = startPos;
    }

    public int code(byte[] buf, int off, int len) {
        int end = off + len - 4;
        int i;

        for (i = off; i <= end; i += 4) {
            // Handle BL instruction:
            // Convert the full 26 bit immediate, do not ignore any bits
            // for possible false positives. The full range gives important
            // compression ratio improvements for large files, but suffers
            // slightly on small files.
            if ((buf[i + 3] & 0xFC) == 0x94) {
                int src = ((buf[i + 3] & 0x03) << 24)
                          | ((buf[i + 2] & 0xFF) << 16)
                          | ((buf[i + 1] & 0xFF) << 8)
                          | (buf[i] & 0xFF);

                // Instead of shifting the immediate, we shift the program
                // counter so we do not have to shift the destination value
                // back by 2.
                final int pc = (pos + i - off) >>> 2;

                int dest;
                if (isEncoder)
                    dest = src + pc;
                else
                    dest = src - pc;

                buf[i + 3] = (byte)(0x94 | ((dest >>> 24) & 0x3));
                buf[i + 2] = (byte)(dest >>> 16);
                buf[i + 1] = (byte)(dest >>> 8);
                buf[i] = (byte)dest;

            } else if ((buf[i + 3] & 0x9F) == 0x90) {
                // Handle ADRP instruction:
                // Ignore data that appears to be a ADRP instruction if
                // the highest 3 bits in the immediate are not all the same.
                // In practice, this limits the immediate range to +/- 512 MiB,
                // which is large enough for a majority of actual ARM64
                // binary data. This reduces false positives of data that
                // is not ADRP instructions.
                int instruction = ((buf[i + 3] & 0xFF) << 24)
                                  | ((buf[i + 2] & 0xFF) << 16)
                                  | ((buf[i + 1] & 0xFF) << 8)
                                  | (buf[i] & 0xFF);

                // The ADRP instruction in AArch64:
                // Bits 0 - 4: Destination register.
                // Bits 5 - 23: High bits of immediate.
                // Bits 24 - 28: Op code.
                // Bits 29 - 30: Low bits of immediate.
                // Bit 31: Op code.
                //
                // When an ADRP instruction is executed, the immediate is
                // shifted left by 12 and the bottom 12 bits are then masked
                // out. The immediate is added to the program counter, then
                // stored in the destination register.
                final int src = ((instruction >>> 29) & 3)
                                | ((instruction >>> 3) & 0x001FFFFC);

                // This addition is an optimization to avoid the need for two
                // checks for accepted +/- range. The more readable code
                // would check if any of the four highest bits (3 ignored and
                // 1 sign bit) are set and if so, they all must be set which
                // would indicate a negative immediate. The readable version:
                // if ((src & 0x001E0000) != 0
                //     && (src & 0x001E0000 != 0x001E0000))
                //     continue;
                //
                // 0x001C0000 has the highest 3 bits set in the immediate.
                // 0x00020000 has the 17th bit set
                // If this addition results in any of the 3 highest bits set:
                // - The src already had one or more of the 4 highest bits
                //   set, but not all of them set (too large positive).
                // - The src was a very large negative so the addition did
                //   not flip the 3 highest bits in the immediate.
                if (((src + 0x00020000) & 0x001C0000) != 0)
                    continue;

                final int pc = (pos + i - off) >>> 12;

                int dest;
                if (isEncoder)
                    dest = src + pc;
                else
                    dest = src - pc;

                // Clear out all bits except for the opcode and original
                // destination register.
                instruction &= 0x9000001F;

                // Shift the new low bits back to bits 29 - 30.
                instruction |= (dest & 3) << 29;

                // Shift the new high bits back to bits 5 - 20.
                // AND out bits 21-23.
                instruction |= (dest & 0x0003FFFC) << 3;

                // If the immediate is negative (17th bit set before shifting)
                // ensure bits 21-23 (after shifting) are set. This ensures
                // proper sign extension.
                instruction |= (0 - (dest & 0x00020000)) & 0x00E00000;

                buf[i + 3] = (byte)(instruction >>> 24);
                buf[i + 2] = (byte)(instruction >>> 16);
                buf[i + 1] = (byte)(instruction >>> 8);
                buf[i] = (byte)(instruction);
            }
        }

        i -= off;
        pos += i;
        return i;
    }
}
