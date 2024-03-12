/*
 * ArrayMismatch
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
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class to facilitate reading/comparing 4 bytes at a time in
 * native (endian) byte order.
 * 
 * @author Brett Okken
 */
abstract class BaseIntArrayMismatch implements ArrayMismatch {

    /**
     * The method this is bound to at runtime is depends native byte order of the
     * system.
     * <p>
     * Will be bound to either {@link Integer#numberOfLeadingZeros(int)} or
     * {@link Integer#numberOfTrailingZeros(int)} depending on
     * {@link ByteOrder#nativeOrder()}.
     * </p>
     * 
     * NOTE: With java 8 this can be replaced with a IntUnaryOperator lambda. It will have
     * no performance impacts, but will be easier to read.
     */
    private static final MethodHandle LEADING_ZEROS;

    static {
        MethodHandle leadingZeros = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            leadingZeros = lookup.findStatic(Integer.class,
                    ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder() ? "numberOfLeadingZeros" : "numberOfTrailingZeros",
                    methodType(int.class, int.class));
        } catch (Throwable t) {
            leadingZeros = null;
            final Logger logger = Logger.getLogger(BaseIntArrayMismatch.class.getName());
            logger.log(Level.FINE, "failed trying to load MethodHandle to find leading zeros", t);
        }
        LEADING_ZEROS = leadingZeros;
    }

    BaseIntArrayMismatch() {
        if (LEADING_ZEROS == null) {
            throw new IllegalStateException("could not create MethodHandle to calculate leading zeros");
        }
    }

    @Override
    public int mismatch(byte[] bytes, int aFromIndex, int bFromIndex, int length) throws Throwable {
        // the actual implementation class can choose to validate input or not
        int i = 0;
        for (int j = length - 3; i < j; i += 4) {
            final int aVal = getInt(bytes, aFromIndex + i);
            final int bVal = getInt(bytes, bFromIndex + i);
            if (aVal != bVal) {
                // this returns a value where bits which match are 0 and bits which
                // differ are 1
                final int diff = aVal ^ bVal;
                // the first (in native byte order) bit which differs tells us
                // which byte differed
                final int leadingZeros = (int) LEADING_ZEROS.invokeExact(diff);
                return i + (leadingZeros >>> 3);
            }
        }

        int result = length;
        switch (length & 3) {
        case 3:
            if (bytes[aFromIndex + i + 2] != bytes[bFromIndex + i + 2])
                result = i + 2;
        case 2:
            if (bytes[aFromIndex + i + 1] != bytes[bFromIndex + i + 1])
                result = i + 1;
        case 1:
            if (bytes[aFromIndex + i] != bytes[bFromIndex + i])
                result = i;
        }
        return result;
    }

    /**
     * Reads <i>int</i> value in native byte order from <i>bytes</i> starting at
     * <i>index</i>.
     */
    abstract int getInt(byte[] bytes, int index) throws Throwable;
}
