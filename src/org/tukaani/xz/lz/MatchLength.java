// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.lz;

// See the version in the src9 directory for documentation.
final class MatchLength {
    static final int EXTRA_SIZE = 0;

    static int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        assert off >= 0;
        assert delta > 0;
        assert len >= 0;
        assert lenLimit >= len;

        lenLimit += off;
        int i = off + len;

        while (i < lenLimit && buf[i] == buf[i - delta])
            ++i;

        return i - off;
    }
}
