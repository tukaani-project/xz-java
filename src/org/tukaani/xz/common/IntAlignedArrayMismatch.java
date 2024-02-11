/*
 * IntAlignedArrayMismatch
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

/**
 * Utility implementation of {@code ArrayMismatch} which attempts to align reads
 * to 4 byte or 2 byte offsets before deferring to another implementation.
 *
 * This is intended to be used in combination with an implementation which would
 * be expected to perform better if reads could be aligned to 4 byte offsets.
 * 
 * @author Brett Okken
 */
final class IntAlignedArrayMismatch implements ArrayMismatch {

    private final ArrayMismatch realMismatch;

    IntAlignedArrayMismatch(ArrayMismatch realMismatch) {
        assert realMismatch != null;
        this.realMismatch = realMismatch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int mismatch(byte[] a, int aFromIndex, int bFromIndex, int length) throws Throwable {

        // while we could do an index check, the real mismatch incorporates a check
        // making any check here duplicative
        int aFromAlignment = aFromIndex & 3;
        int bFromAlignment = bFromIndex & 3;

        // if either are aligned, just go, none of the adjustments below apply
        if (aFromAlignment == 0 || bFromAlignment == 0) {
            return realMismatch.mismatch(a, aFromIndex, bFromIndex, length);
        }

        int i = 0;
        // if both are similarly out of alignment, adjust
        if (aFromAlignment == bFromAlignment) {
            for (int j = Math.min(4 - aFromAlignment, length); i < j; ++i) {
                if (a[aFromIndex + i] != a[bFromIndex + i]) {
                    return i;
                }
            }
        } else if ((aFromAlignment & 1) == 1 && (bFromAlignment & 1) == 1) {
            // if they both have an odd alignment, adjust by one
            if (a[aFromIndex] != a[bFromIndex]) {
                return 0;
            }
            ++i;
        }

        return i + realMismatch.mismatch(a, aFromIndex + i, bFromIndex + i, length - i);
    }
}
