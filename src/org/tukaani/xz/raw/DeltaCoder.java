/*
 * DeltaCoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.raw;

abstract class DeltaCoder implements FilterCoder {
    public boolean changesSize() {
        return false;
    }

    public boolean nonLastOK() {
        return true;
    }

    public boolean lastOK() {
        return false;
    }
}
