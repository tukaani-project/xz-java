// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.simple;

import org.tukaani.xz.common.ByteArrayView;

// BCJ filter encoder for RISC-V instructions
public final class RISCVEncoder implements SimpleFilter {
    private int pos;

    public RISCVEncoder(int startPos) {
        pos = startPos;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int end = off + len - 8;
        int i;

        // The loop is advanced by 2 bytes every iteration since the
        // instruction stream may include 16-bit instructions (C extension).
        for (i = off; i <= end; i += 2) {
            int inst = buf[i] & 0xFF;

            if (inst == 0xEF) {
                // JAL
                final int b1 = buf[i + 1] & 0xFF;

                // Only filter rd=x1(ra) and rd=x5(t0).
                if ((b1 & 0x0D) != 0)
                    continue;

                // The 20-bit immediate is in four pieces.
                // The encoder stores it in big endian form
                // since it improves compression slightly.
                final int b2 = buf[i + 2] & 0xFF;
                final int b3 = buf[i + 3] & 0xFF;
                final int pc = pos + i - off;

// The following chart shows the highest three bytes of JAL, focusing on
// the 20-bit immediate field [31:12]. The first row of numbers is the
// bit position in a 32-bit little endian instruction. The second row of
// numbers shows the order of the immediate field in a J-type instruction.
// The last row is the bit number in each byte.
//
// To determine the amount to shift each bit, subtract the value in
// the last row from the value in the second last row. If the number
// is positive, shift left. If negative, shift right.
//
// For example, at the rightmost side of the chart, the bit 4 in b1 is
// the bit 12 of the address. Thus that bit needs to be shifted left
// by 12 - 4 = 8 bits to put it in the right place in the addr variable.
//
// NOTE: The immediate of a J-type instruction holds bits [20:1] of
// the address. The bit [0] is always 0 and not part of the immediate.
//
// |          b3             |          b2             |          b1         |
// | 31 30 29 28 27 26 25 24 | 23 22 21 20 19 18 17 16 | 15 14 13 12 x x x x |
// | 20 10  9  8  7  6  5  4 |  3  2  1 11 19 18 17 16 | 15 14 13 12 x x x x |
// |  7  6  5  4  3  2  1  0 |  7  6  5  4  3  2  1  0 |  7  6  5  4 x x x x |

                int addr = ((b1 & 0xF0) << 8)
                           | ((b2 & 0x0F) << 16)
                           | ((b2 & 0x10) << 7)
                           | ((b2 & 0xE0) >>> 4)
                           | ((b3 & 0x7F) << 4)
                           | ((b3 & 0x80) << 13);

                addr += pc;

                buf[i + 1] = (byte)((b1 & 0x0F) | ((addr >>> 13) & 0xF0));
                buf[i + 2] = (byte)(addr >>> 9);
                buf[i + 3] = (byte)(addr >>> 1);

                // The "-2" is included because the for-loop will always
                // increment by 2. In this case, we want to skip an extra
                // 2 bytes since we used 4 bytes of input.
                i += 4 - 2;

            } else if ((inst & 0x7F) == 0x17) {
                // AUIPC
                inst |= (buf[i + 1] & 0xFF) << 8;
                inst |= (buf[i + 2] & 0xFF) << 16;
                inst |= (buf[i + 3] & 0xFF) << 24;

                // Branch based on AUIPC's rd. The bitmask test does
                // the same thing as this:
                //
                //     int auipcRd = (inst >>> 7) & 0x1F;
                //     if (auipcRd != 0 && auipcRd != 2) {
                if ((inst & 0xE80) != 0) {
                    // AUIPC's rd doesn't equal x0 or x2.

                    // Check if AUIPC+inst2 are a pair.
                    int inst2 = ByteArrayView.getIntLE(buf, i + 4);

                    // This checks two conditions at once:
                    //    - AUIPC rd == inst2 rs1.
                    //    - inst2 opcode has the lowest two bits set.
                    //
                    // The 8 bit left shift aligns the rd of AUIPC with
                    // the rs1 of inst2. By XORing the registers, any
                    // non-zero value in those bits indicates the registers
                    // are not equal and thus not an AUIPC pair. The mask
                    // tests if any of the register or opcode bits are set.
                    // Only the lowest two opcode bits should be set if inst2
                    // is a pair to AUIPC.
                    if ((((inst << 8) ^ inst2) & 0xF8003) != 3) {
                        // The above check allows a false AUIPC+AUIPC pair
                        // if the bits [19:15] (where rs1 would be) in the
                        // second AUIPC match the rd of the first AUIPC.
                        //
                        // We must skip enough forward so that the first two
                        // bytes of the second AUIPC cannot get converted.
                        // Such a conversion could make the current pair
                        // become a valid pair which would desync the decoder.
                        //
                        // Skipping six bytes is enough even though the above
                        // condition looks at the lowest four bits of the
                        // buf[i + 6] too. This is safe because this filter
                        // never changes those bits if a conversion at
                        // that position is done.
                        i += 6 - 2;
                        continue;
                    }

                    // Convert AUIPC+inst2 to a special format:
                    //
                    //   - The lowest 7 bits [6:0] retain the AUIPC opcode.
                    //
                    //   - The rd [11:7] is set to x2(sp). x2 is used as
                    //     the stack pointer so AUIPC with rd=x2 should be
                    //     very rare in real-world executables.
                    //
                    //   - The remaining 20 bits [31:12] (that normally hold
                    //     the pc-relative immediate) are used to store the
                    //     lowest 20 bits of inst2. That is, the 12-bit
                    //     immediate of inst2 is not included.
                    //
                    //   - The location of the original inst2 is used to store
                    //     the 32-bit absolute address in big endian format.
                    //     Compared to the 20+12-bit split encoding, this
                    //     results in a longer uninterrupted sequence of
                    //     identical common bytes when the same address is
                    //     referred with different instruction pairs (like
                    //     AUIPC+LD vs. AUIPC+ADDI) or when the occurrences
                    //     of the same pair use different registers. When
                    //     referring to adjacent memory locations (like
                    //     function calls that go via the ELF PLT), in
                    //     big endian order only the last 1-2 bytes differ;
                    //     in little endian the differing 1-2 bytes would be
                    //     in the middle of the 8-byte sequence.
                    //
                    // When reversing the transformation, the original rd
                    // of AUIPC can be restored from inst2's rs1 as they
                    // are required to be the same.

                    // Arithmetic right shift makes sign extension trivial.
                    int addr = (inst & 0xFFFFF000) + (inst2 >> 20);

                    addr += pos + i - off;

                    // Construct the first 32 bits:
                    //   [6:0]    AUIPC opcode
                    //   [11:7]   Special AUIPC rd = x2
                    //   [31:12]  The lowest 20 bits of inst2
                    inst = 0x17 | (2 << 7) | (inst2 << 12);

                    ByteArrayView.setIntLE(buf, i, inst);

                    // The second 32 bits store the absolute
                    // address in big endian order.
                    ByteArrayView.setIntBE(buf, i + 4, addr);
                } else {
                    // AUIPC's rd equals x0 or x2.
                    //
                    // x0 indicates a landing pad (LPAD). It's always skipped.
                    //
                    // AUIPC with rd == x2 is used for the special format
                    // as explained above. When the input contains a byte
                    // sequence that matches the special format, "fake"
                    // decoding must be done to keep the filter bijective
                    // (that is, safe to apply on arbitrary data).
                    //
                    // See the "x0 or x2" section in RISCVDecoder for how
                    // the "real" decoding is done. The "fake" decoding is
                    // a simplified version of "real" decoding with the
                    // following differences:
                    // (1) The lowest 12 bits aren't sign-extended.
                    // (2) No address conversion is done.
                    // (3) Big endian format isn't used (the fake
                    //     address is in little endian order).

                    // Check if inst matches the special format.
                    final int fakeRs1 = inst >>> 27;

                    // This checks multiple conditions:
                    //   (1) AUIPC rd [11:7] == x2 (special rd value).
                    //   (2) AUIPC bits 12 and 13 set (the lowest two
                    //       opcode bits of packed inst2).
                    //   (3) inst2 rs1 doesn't equal x0 or x2 because
                    //       the opposite conversion is only done when
                    //       AUIPC rd != x0 &&
                    //       AUIPC rd != x2 &&
                    //       AUIPC rd == inst2 rs1.
                    //
                    // The left-hand side takes care of (1) and (2).
                    //   (a) If AUIPC rd equals x2, subtracting 0x100 makes
                    //       bits [11:7] zeros. If rd doesn't equal x2,
                    //       then there will be at least one non-zero bit
                    //       and the next step (b) is irrelevant.
                    //   (b) If the lowest two opcode bits of the packed inst2
                    //       are set in [13:12], then subtracting 0x3000 will
                    //       make those bits zeros. Otherwise there will be
                    //       at least one non-zero bit.
                    //
                    // The bitwise-and removes the uninteresting bits from
                    // the final '>=' comparison and ensures that any
                    // non-zero result will be larger than any possible
                    // result from the right-hand side of the comparison.
                    //
                    // On the right-hand side, rs1 & 0x1D will be non-zero
                    // as long as rs1 is not x0 or x2.
                    //
                    // The final '>=' comparison will make the expression
                    // true if:
                    //   - The subtraction caused any bits to be set
                    //     (special AUIPC rd value not used or inst2
                    //     opcode bits not set). (non-zero >= non-zero or 0)
                    //   - The subtraction did not cause any bits to be set
                    //     but rs1 was x0 or x2. (0 >= 0)
                    if (((inst - 0x3100) & 0x3F80) >= (fakeRs1 & 0x1D)) {
                        i += 4 - 2;
                        continue;
                    }

                    final int fakeAddr = ByteArrayView.getIntLE(buf, i + 4);

                    // Construct the second 32 bits:
                    //   [19:0]   Upper 20 bits from AUIPC
                    //   [31:20]  The lowest 12 bits of fakeAddr
                    final int fakeInst2 = (inst >>> 12) | (fakeAddr << 20);

                    // Construct new first 32 bits from:
                    //   [6:0]   AUIPC opcode
                    //   [11:7]  Fake AUIPC rd = fakeRs1
                    //   [31:12] The highest 20 bits of fakeAddr
                    inst = 0x17 | (fakeRs1 << 7) | (fakeAddr & 0xFFFFF000);

                    ByteArrayView.setIntLE(buf, i, inst);
                    ByteArrayView.setIntLE(buf, i + 4, fakeInst2);
                }

                i += 8 - 2;
            }
        }

        i -= off;
        pos += i;
        return i;
    }
}
