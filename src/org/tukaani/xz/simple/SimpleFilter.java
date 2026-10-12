// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.simple;

import org.tukaani.xz.UnsupportedOptionsException;

public interface SimpleFilter {
    static void validateStartOffset(int startOffset, int alignment)
            throws UnsupportedOptionsException {
        if ((startOffset & (alignment - 1)) != 0)
            throw new UnsupportedOptionsException(
                    "Start offset must be a multiple of " + alignment);
    }

    int code(byte[] buf, int off, int len);
}
