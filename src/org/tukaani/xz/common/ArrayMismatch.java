/*
 * ArrayMismatch
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

/**
 * 
 * @author Brett Okken
 */
interface ArrayMismatch {

    /**
     * Compares the values in <i>bytes</i>, starting at <i>aFromIndex</i> and
     * <i>bFromIndex</i> and returns the zero-based index of the first {@code byte}
     * which differs.
     * 
     * @param bytes      The {@code byte[]} for comparison.
     * @param aFromIndex The first offset into <i>bytes</i> to start reading from.
     * @param bFromIndex The second offset into <i>bytes</i> to start reading from.
     * @param length     The number of bytes to compare. Will always be at least {@code 0}.
     * @return The offset from the starting indexes of the first byte which differs.
     *         If all match, <i>length</i> will be returned.
     * @throws Throwable If any failures occur.
     */
    public int mismatch(byte[] bytes, int aFromIndex, int bFromIndex, int length) throws Throwable;
}