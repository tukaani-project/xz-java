// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.lz;

interface MatchLengthFinder {
    /** See {@code MatchLength.getExtraSize}. */
    int getExtraSize();

    /** See {@code MatchLength.getLen}. */
    int getLen(byte[] buf, int off, int delta, int len, int lenLimit);
}
