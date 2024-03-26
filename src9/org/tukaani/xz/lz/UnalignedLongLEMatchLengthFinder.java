// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.lz;

import org.tukaani.xz.common.ByteArrayView;

// This is for 64-bit little endian processors that support fast
// unaligned access. This is based on XZ Utils' memcmplen.h.
// This may read up to 7 extra bytes past the end of the specified
// end offset (off + lenLimit) so the caller must ensure that
// there are EXTRA_SIZE bytes available at the end of the buffer.
//
// In the extreme case of compressing a sequence of zero bytes, this
// can reduce compression time by 30 % compared to Arrays.mismatch.
// In normal files with mostly short matches, this method is faster
// than Arrays.mismatch too. This was tested with OpenJDK 21 on x86-64.
final class UnalignedLongLEMatchLengthFinder implements MatchLengthFinder {
    @Override
    public int getExtraSize() {
        return 7;
    }

    @Override
    public int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        while (len < lenLimit) {
            // With little endian both subtraction and xor work here.
            // Xor is slightly more intuitive but subtraction might have
            // a very tiny speed benefit in specific cases[*]. However, it
            // probably doesn't really matter here as the Java version isn't
            // fast anyway.
            //
            // [*] Some x86-64 processor can fuse sub+jz and sub+jnz but not
            //     xor+jz or xor+jnz.
            long x = ByteArrayView.getLongLE(buf, off + len) -
                     ByteArrayView.getLongLE(buf, off + len - delta);

            if (x != 0) {
                len += Long.numberOfTrailingZeros(x) >>> 3;
                return Math.min(len, lenLimit);
            }

            len += 8;
        }

        return lenLimit;
    }
}
