/*
 * Util
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

public class Util {
    public static final int STREAM_HEADER_SIZE = 12;
    public static final long BACKWARD_SIZE_MAX = 1L << 34;
    public static final int BLOCK_HEADER_SIZE_MAX = 1024;
    public static final long VLI_MAX = Long.MAX_VALUE;
    public static final int VLI_SIZE_MAX = 9;

    public static int getVLISize(long num) {
        int size = 0;
        do {
            ++size;
            num >>= 7;
        } while (num != 0);

        return size;
    }

    public static long getUnpaddedIndexSize(long recordCount,
                                            long indexListSize) {
        return 1 + getVLISize(recordCount) + indexListSize + 4;
    }

    public static long getIndexSize(long recordCount, long indexListSize) {
        return (getUnpaddedIndexSize(recordCount, indexListSize) + 3) & ~3;
    }

    public static long getStreamSizeFromIndex(
            long blocksSizeSum, long recordCount, long indexListSize) {
        return STREAM_HEADER_SIZE + blocksSizeSum
                + getIndexSize(recordCount, indexListSize)
                + STREAM_HEADER_SIZE;
    }

    public static int getIndexPaddingSize(long recordCount,
                                          long indexListSize) {
        return (int)(
                (4 - getUnpaddedIndexSize(recordCount, indexListSize)) & 3);
    }
}
