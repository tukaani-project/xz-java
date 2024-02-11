/*
 * VectorArrayMismatch
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses a {@link MethodHandle} to invoke the jdk 9+
 * {@code Arrays.mismatch​(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex, int bToIndex)}.
 * 
 * The constructor will fail if said method cannot be discovered.
 * 
 * @author Brett Okken
 */
final class VectorArrayMismatch implements ArrayMismatch {

    /**
     * MethodHandle to the jdk 9+
     * {@code Arrays.mismatch​(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex, int bToIndex)}.
     */
    private static final MethodHandle ARRAYS_MISMATCH;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodType arraysType = methodType(int.class, byte[].class, int.class, int.class, byte[].class, int.class,
                int.class);
        MethodHandle arraysMismatch = null;

        try {
            arraysMismatch = lookup.findStatic(Arrays.class, "mismatch", arraysType);
        } catch (Throwable t) {
            final Logger logger = Logger.getLogger(VectorArrayMismatch.class.getName());
            logger.log(Level.FINE, "failed trying to load a MethodHandle to invoke Arrays.mismatch", t);
        }
        ARRAYS_MISMATCH = arraysMismatch;
    }

    VectorArrayMismatch() {
        if (ARRAYS_MISMATCH == null) {
            throw new IllegalStateException("java.util.Arrays.mismatch not found");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int mismatch(byte[] a, int aFromIndex, int bFromIndex, int length) throws Throwable {
        final int m = (int) ARRAYS_MISMATCH.invokeExact(a,
                                                        aFromIndex,
                                                        aFromIndex + length,
                                                        a, bFromIndex,
                                                        bFromIndex + length);
        return m == -1 ? length : m;
    }
}