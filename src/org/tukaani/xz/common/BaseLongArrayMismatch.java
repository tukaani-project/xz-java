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
 * Base class to facilitate reading/comparing 8 bytes at a time in
 * native (endian) byte order.
 * 
 * @author Brett Okken
 */
abstract class BaseLongArrayMismatch implements ArrayMismatch {

    /**
     * The method this is bound to at runtime is depends native byte order of the system.
     * <p>
     * Will be bound to either {@link Long#numberOfLeadingZeros(long)} or
     * {@link Long#numberOfTrailingZeros(long)} depending on
     * {@link ByteOrder#nativeOrder()}.
     * </p>
     */
    private static final MethodHandle LEADING_ZEROS;

    static {
        MethodHandle leadingZeros = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            leadingZeros = lookup.findStatic(Long.class,
                    ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder() ? "numberOfLeadingZeros"
                            : "numberOfTrailingZeros",
                    methodType(int.class, long.class));
        } catch (Throwable t) {
            leadingZeros = null;
            final Logger logger = Logger.getLogger(BaseLongArrayMismatch.class.getName());
            logger.log(Level.FINE, "failed trying to load MethodHandle to find leading zeros", t);
        }
        LEADING_ZEROS = leadingZeros;
    }

    BaseLongArrayMismatch() {
        if (LEADING_ZEROS == null) {
            throw new IllegalStateException("could not create MethodHandle to calculate leading zeros");
        }
    }

    @Override
    public int mismatch(byte[] a, int aFromIndex, int bFromIndex, int length) throws Throwable {
        // the actual implementation class can choose to validate input or not

        int i = 0;
        for (int j = length - 7; i < j; i += 8) {
            final long aVal = getLong(a, aFromIndex + i);
            final long bVal = getLong(a, bFromIndex + i);
            if (aVal != bVal) {
                // this returns a value where bits which match are 0 and bits which differ are 1
                final long diff = aVal ^ bVal;
                // the first (in native byte order) bit which differs tells us which byte
                // differed
                final int leadingZeros = (int) LEADING_ZEROS.invokeExact(diff);
                return i + (leadingZeros >>> 3);
            }
        }

        int result = length;
        switch (length & 7) {
        case 7:
            if (a[aFromIndex + i + 6] != a[bFromIndex + i + 6])
                result = i + 6;
        case 6:
            if (a[aFromIndex + i + 5] != a[bFromIndex + i + 5])
                result = i + 5;
        case 5:
            if (a[aFromIndex + i + 4] != a[bFromIndex + i + 4])
                result = i + 4;
        case 4:
            if (a[aFromIndex + i + 3] != a[bFromIndex + i + 3])
                result = i + 3;
        case 3:
            if (a[aFromIndex + i + 2] != a[bFromIndex + i + 2])
                result = i + 2;
        case 2:
            if (a[aFromIndex + i + 1] != a[bFromIndex + i + 1])
                result = i + 1;
        case 1:
            if (a[aFromIndex + i] != a[bFromIndex + i])
                result = i;
        }
        return result;
    }

    /**
     * Reads <i>long</i> value in native byte order from <i>bytes</i> starting at <i>index</i>.
     */
    abstract long getLong(byte[] bytes, int index) throws Throwable;
}
