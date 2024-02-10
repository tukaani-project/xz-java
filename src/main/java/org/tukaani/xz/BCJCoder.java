/*
 * BCJCoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

abstract class BCJCoder implements FilterCoder {
    public static final long X86_FILTER_ID = 0x04;
    public static final long POWERPC_FILTER_ID = 0x05;
    public static final long IA64_FILTER_ID = 0x06;
    public static final long ARM_FILTER_ID = 0x07;
    public static final long ARMTHUMB_FILTER_ID = 0x08;
    public static final long SPARC_FILTER_ID = 0x09;
    public static final long ARM64_FILTER_ID = 0X0A;

    public static boolean isBCJFilterID(long filterID) {
        return filterID >= X86_FILTER_ID && filterID <= ARM64_FILTER_ID;
    }

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
