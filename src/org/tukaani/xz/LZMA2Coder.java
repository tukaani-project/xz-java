// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

abstract class LZMA2Coder implements FilterCoder {
    public static final long FILTER_ID = 0x21;

    public boolean changesSize() {
        return true;
    }

    public boolean nonLastOK() {
        return false;
    }

    public boolean lastOK() {
        return true;
    }
}
