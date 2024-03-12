/*
 * ArrayUtil
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for optimized array interactions.
 * 
 * <p>
 * The means of comparing arrays can be controlled by setting the system property
 * {@code org.tukaani.xz.ArrayComparison} to a value from
 * {@link ArrayComparison}.
 * </p>
 *
 * @author Brett Okken
 */
public final class ArrayUtil {

    /**
     * Enumerated options for controlling implementation of how to compare arrays.
     */
    public static enum ArrayComparison {
        /**
         * Uses {@code VarHandle} for {@code int[]} access.
         * <p>
         * This is default behavior on jdk9+ for 32 bit x86.
         * </p>
         */
        VH_INT,
        /**
         * Uses {@code VarHandle} for {@code int[]} access after attempting to align the
         * reads on 4 byte boundaries.
         */
        VH_INT_ALIGN,
        /**
         * Uses {@code VarHandle} for {@code long[]} access.
         * <p>
         * This is default behavior on jdk9+ for 64 bit x86.
         * </p>
         */
        VH_LONG,
        /**
         * Uses {@code VarHandle} for {@code long[]} access after attempting to align
         * the reads.
         */
        VH_LONG_ALIGN,
        /**
         * Uses {@code Arrays.mismatch()} to perform vectorized comparison.
         * <p>
         * This is default behavior on jdk9+ for non-x86.
         * </p>
         */
        VECTOR,
        /**
         * Uses {@code sun.misc.Unsafe.getInt()} for unaligned {@code int[]} access.
         * <p>
         * This is default behavior on jdk 8 and prior for 32 bit x86.
         * </p>
         */
        UNSAFE_GET_INT,
        /**
         * Uses {@code sun.misc.Unsafe.getLong()} for unaligned {@code long[]} access.
         * <p>
         * This is default behavior on jdk 8 and prior for 64 bit x86.
         * </p>
         */
        UNSAFE_GET_LONG,
        /**
         * Performs byte-by-byte comparison.
         */
        LEGACY;

        static ArrayComparison getFromProperty(String prop) {
            if (prop == null || prop.isEmpty()) {
                return null;
            }
            try {
                return ArrayComparison.valueOf(prop.toUpperCase(Locale.US));
            } catch (Exception e) {
                final Logger logger = Logger.getLogger(ArrayUtil.class.getName());
                logger.log(Level.INFO, "Invalid ArrayComparison option, using default behavior", e);
                return null;
            }
        }
    }

    private static final ArrayMismatch IMPL;

    static {
        final Properties props = System.getProperties();
        final ArrayComparison algo = ArrayComparison
                .getFromProperty(props.getProperty("org.tukaani.xz.ArrayComparison"));
        final String arch = props.getProperty("os.arch", "");

        IMPL = createInstance(algo, arch);
        final Logger logger = Logger.getLogger(ArrayUtil.class.getName());
        logger.log(Level.FINE, "ArrayMismatch implementation chosen: {0}", IMPL.getClass().getSimpleName());
    }

    static ArrayMismatch createInstance(ArrayComparison algo, String arch) {
        final Logger logger = Logger.getLogger(ArrayUtil.class.getName());
        ArrayMismatch impl = null;
        try {
            final boolean x86 = arch.matches("^(i[3-6]86|x86(_64)?|x64|amd64)$");

            // if x86, or explicitly configured, try VarHandles
            if ((x86 && algo == null) || algo == ArrayComparison.VH_LONG || algo == ArrayComparison.VH_LONG_ALIGN
                    || algo == ArrayComparison.VH_INT || algo == ArrayComparison.VH_INT_ALIGN) {
                try {
                    final boolean doLong = (algo == null && arch.contains("64")) || algo == ArrayComparison.VH_LONG
                            || algo == ArrayComparison.VH_LONG_ALIGN;
                    impl = doLong ? new VarHandleLongArrayMismatch() : new VarHandleIntArrayMismatch();

                    if (algo == ArrayComparison.VH_LONG_ALIGN) {
                        assert impl instanceof VarHandleLongArrayMismatch;
                        impl = new LongAlignedArrayMismatch(impl);
                    } else if (algo == ArrayComparison.VH_INT_ALIGN) {
                        assert impl instanceof VarHandleIntArrayMismatch;
                        impl = new IntAlignedArrayMismatch(impl);
                    }
                } catch (Throwable t) {
                    logger.log(Level.FINE,
                            "failed trying to load a MethodHandle to invoke get on a byteArrayViewVarHandle", t);
                    impl = null;
                }
            }

            // for architectures other than x86, or explicitly configured, used Arrays
            // vectorized comparison
            if (impl == null && ((!x86 && algo == null) || algo == ArrayComparison.VECTOR)) {
                try {
                    impl = new VectorArrayMismatch();
                } catch (Throwable t) {
                    logger.log(Level.FINE, "failed trying to load a MethodHandle to invoke Arrays.mismatch", t);
                    impl = null;
                }
            }

            // if byteArrayViewVarHandle for a long[] could not be loaded, then
            // try to load sun.misc.Unsafe for unaligned archs only
            if (impl == null && (((x86 || arch.equals("aarch64")) && algo == null)
                    || algo == ArrayComparison.UNSAFE_GET_LONG || algo == ArrayComparison.UNSAFE_GET_INT)) {
                if (algo == ArrayComparison.UNSAFE_GET_LONG || (algo == null && arch.contains("64"))) {
                    impl = new UnsafeLongArrayMismatch();
                } else {
                    impl = new UnsafeIntArrayMismatch();
                }
            }
        } catch (Throwable t) {
            logger.log(Level.FINE, "failed trying to load means to optimize compison of byte[]", t);
        }
        if (impl == null) {
            impl = new LegacyArrayMismatch();
        }
        return impl;
    }

    /**
     * Compares the values in <i>bytes</i>, starting at <i>aFromIndex</i> and
     * <i>bFromIndex</i> and returns the zero-based index of the first {@code byte}
     * which differs.
     * 
     * @param bytes      The {@code byte[]} for comparison.
     * @param aFromIndex The first offset into <i>bytes</i> to start reading from.
     * @param bFromIndex The second offset into <i>bytes</i> to start reading from.
     * @param length     The number of bytes to compare.
     * @return The offset from the starting indexes of the first byte which differs.
     *         If all match, <i>length</i> will be returned.
     */
    public static int mismatch(byte[] bytes, int aFromIndex, int bFromIndex, int length) {
        try {
            return length > 0 ? IMPL.mismatch(bytes, aFromIndex, bFromIndex, length) : 0;
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Compares the values in <i>bytes</i>, starting at <i>aFromIndex</i> and
     * <i>bFromIndex</i> and returns the zero-based index of the first {@code byte}
     * which differs.
     * <p>
     * This differs from {@link #mismatch(byte[], int, int, int)} by optimizing for
     * the case where the first bytes compared do not match.
     * </p>
     * 
     * @param bytes      The {@code byte[]} for comparison.
     * @param aFromIndex The first offset into <i>bytes</i> to start reading from.
     * @param bFromIndex The second offset into <i>bytes</i> to start reading from.
     * @param length     The number of bytes to compare.
     * @return The offset from the starting indexes of the first byte which differs.
     *         If all match, <i>length</i> will be returned.
     */
    public static int checkFirstMismatch(byte[] bytes, int aFromIndex, int bFromIndex, int length) {
        try {
            return length > 0 && bytes[aFromIndex] == bytes[bFromIndex]
                    ? IMPL.mismatch(bytes, aFromIndex + 1, bFromIndex + 1, length - 1) + 1
                    : 0;
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private ArrayUtil() {
    }
}