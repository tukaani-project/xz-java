/*
 * LZMA2Coder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.raw;

abstract class LZMA2Coder implements FilterCoder {
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
