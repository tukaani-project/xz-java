/*
 * UnsafeIntArrayMismatch
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
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses {@code sun.misc.Unsafe} to process 4 bytes at a time. Must only be used
 * with platforms which support un-aligned accesses.
 * 
 * The constructor will fail if {@code sun.misc.Unsafe} cannot be accessed.
 * 
 * @author Brett Okken
 */
final class UnsafeIntArrayMismatch extends BaseIntArrayMismatch {

    /**
     * <p>
     * This is bound to {@code sun.misc.Unsafe.getInt(Object, long)}.
     * </p>
     */
    static final MethodHandle GET_PRIMITIVE;

    /**
     * Populated from reflected read of
     * {@code sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET} if one of the unsafe
     * implementations is used.
     */
    private static final long ARRAY_BASE_OFFSET;

    static {
        long arrayBaseOffset = 0;
        MethodHandle getPrimitive = null;
        try {
            Class<?> unsafeClazz = Class.forName("sun.misc.Unsafe", true, null);
            Constructor<?> unsafeConstructor = unsafeClazz.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            Object unsafe = unsafeConstructor.newInstance();

            arrayBaseOffset = unsafeClazz.getField("ARRAY_BYTE_BASE_OFFSET").getLong(null);

            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            MethodHandle virtualGetInt = lookup.findVirtual(unsafeClazz, "getInt",
                    methodType(int.class, Object.class, long.class));
            getPrimitive = virtualGetInt.bindTo(unsafe);

            // do a test read to confirm unsafe is actually functioning
            int val = (int) getPrimitive.invokeExact((Object) new byte[] { 0, 0, 0, 0 }, arrayBaseOffset + 0L);
            if (val != 0) {
                throw new IllegalStateException("invalid value: " + val);
            }
        } catch (Throwable t) {
            getPrimitive = null;
            final Logger logger = Logger.getLogger(UnsafeIntArrayMismatch.class.getName());
            logger.log(Level.FINE, "failed trying to load sun.misc.Unsafe and related method handles", t);
        }
        GET_PRIMITIVE = getPrimitive;
        ARRAY_BASE_OFFSET = arrayBaseOffset;
    }

    UnsafeIntArrayMismatch() {
        if (GET_PRIMITIVE == null) {
            throw new IllegalStateException("could not load sun.misc.Unsafe");
        }
    }

    @Override
    public int mismatch(byte[] bytes, int aFromIndex, int bFromIndex, int length) throws Throwable {
        // it is important to check the indexes prior to making the Unsafe calls,
        // as Unsafe does not validate and could result in SIGSEGV if out of bounds
        if (aFromIndex < 0 || bFromIndex < 0 || Math.max(aFromIndex, bFromIndex) > bytes.length - length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return super.mismatch(bytes, aFromIndex, bFromIndex, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int getInt(byte[] bytes, int index) throws Throwable {
        return (int) GET_PRIMITIVE.invokeExact((Object) bytes, ARRAY_BASE_OFFSET + index);
    }
}
