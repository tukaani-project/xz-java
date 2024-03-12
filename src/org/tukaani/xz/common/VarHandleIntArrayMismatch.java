/*
 * VarHandleIntArrayMismatch
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses a <a href=
 * "https://docs.oracle.com/javase%2F9%2Fdocs%2Fapi%2F%2F/java/lang/invoke/MethodHandles.html#byteArrayViewVarHandle-java.lang.Class-java.nio.ByteOrder-">
 * byteArrayViewVarHandle</a> for {@code int[]} using the
 * {@link ByteOrder#nativeOrder()} to process 4 bytes at a time.
 * 
 * The constructor will fail if {@code byteArrayViewVarHandle} cannot be invoked.
 * 
 * @author Brett Okken
 */
final class VarHandleIntArrayMismatch extends BaseIntArrayMismatch {

    /**
     * <p>
     * A jdk 9+ {@code byteArrayViewVarHandle} for {@code int[]} using
     * the {@link ByteOrder#nativeOrder()}. The method signature is
     * {@code int get(byte[], int)}.
     * </p>
     */
    static final MethodHandle GET_PRIMITIVE;

    static {
        MethodHandle getPrimitive = null;
        try {
            final Class<?> varHandleClazz = Class.forName("java.lang.invoke.VarHandle", true, null);
            final Method byteArrayViewHandle = MethodHandles.class.getDeclaredMethod("byteArrayViewVarHandle",
                    new Class[] { Class.class, ByteOrder.class });
            final Object varHandle = byteArrayViewHandle.invoke(null, int[].class, ByteOrder.nativeOrder());
            final Class<?> accessModeEnum = Class.forName("java.lang.invoke.VarHandle$AccessMode", true, null);
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Object getAccessModeEnum = Enum.valueOf((Class) accessModeEnum, "GET");
            final Method toMethodHandle = varHandleClazz.getDeclaredMethod("toMethodHandle", accessModeEnum);
            getPrimitive = (MethodHandle) toMethodHandle.invoke(varHandle, getAccessModeEnum);
            // do a test read to confirm unsafe is actually functioning
            int val = (int) getPrimitive.invokeExact(new byte[] { 0, 0, 0, 0 }, 0);
            if (val != 0) {
                throw new IllegalStateException("invalid value: " + val);
            }
        } catch (Throwable t) {
            getPrimitive = null;
            final Logger logger = Logger.getLogger(VarHandleIntArrayMismatch.class.getName());
            logger.log(Level.FINE, "failed trying to load byteArrayViewVarHandle and related method handles", t);
        }
        GET_PRIMITIVE = getPrimitive;
    }

    VarHandleIntArrayMismatch() {
        if (GET_PRIMITIVE == null) {
            throw new IllegalStateException("could not load byteArrayViewVarHandle and related method handles");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    int getInt(byte[] bytes, int index) throws Throwable {
        return (int) GET_PRIMITIVE.invokeExact(bytes, index);
    }
}
