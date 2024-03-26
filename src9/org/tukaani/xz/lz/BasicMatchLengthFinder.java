// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.lz;

import java.util.Arrays;

// This works on all Java >= 9 platforms. On x86-64 this is at least
// as fast as a simple byte-by-byte loop. With long matches (when
// a file compresses very well) this is significantly faster than
// a simple loop: when compressing zeros, the compression time might
// be reduced over 60 % but that is the most extreme situation.
final class BasicMatchLengthFinder implements MatchLengthFinder {
    @Override
    public int getExtraSize() {
        return 0;
    }

    @Override
    public int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        int start = off + len;
        int end = off + lenLimit;

        int mismatch = Arrays.mismatch(buf, start, end,
                                       buf, start - delta, end - delta);

        return mismatch < 0 ? lenLimit : len + mismatch;

        // This would avoid the branch *here* but it seems that
        // this might be a tiny bit slower in practice.
        // int mismatch = Arrays.mismatch(buf, start, end,
        //                                buf, start - delta, end - delta + 1);
        // return len + mismatch;
    }
}
