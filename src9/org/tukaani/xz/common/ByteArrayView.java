// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.common;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ByteArrayView {
    // byteArray[0] isn't necessarily aligned to a multiple of 2, 4, or 8
    // bytes, and thus accessing it as short, int, or long could be
    // misaligned. VarHandle guarantees support for misaligned access
    // for the basic .get and .set methods but it is slower even on
    // processors that support misaligned access in hardware.
    //
    // These constants tell how many bytes to add to reach an appropriately
    // aligned offset. For example, byteArray[ALIGN_INT] is 4-byte aligned
    // in memory.
    public static final int ALIGN_SHORT;
    public static final int ALIGN_INT;
    public static final int ALIGN_LONG;

    static {
        // The use of alignmentOffset is based on the example code
        // from the documentation of byteArrayViewVarHandle in JDK 21.
        //
        // If the given offset is a too large power of 2, it will throw
        // UnsupportedOperationException. Thus, try them in order from
        // the smallest to the largest. Zero is used as a fallback to ensure
        // that the code will still run. OpenJDK should support 2, 4, and 8.
        ByteBuffer bb = ByteBuffer.wrap(new byte[0]);
        int aoShort = 0;
        int aoInt = 0;
        int aoLong = 0;

        try {
            aoShort = bb.alignmentOffset(0, 2);
            aoInt = bb.alignmentOffset(0, 4);
            aoLong = bb.alignmentOffset(0, 8);
        } catch (UnsupportedOperationException e) {}

        // 4 * n - alignmentOffset(0, 4) is four-byte aligned but
        // 4 * n + ALIGN_INT is more convenient (subtraction vs. addition).
        ALIGN_SHORT = (2 - aoShort) & 1;
        ALIGN_INT = (4 - aoInt) & 3;
        ALIGN_LONG = (8 - aoLong) & 7;
    }

    private static final VarHandle bytesAsShortLE
            = MethodHandles.byteArrayViewVarHandle(
                    short[].class, ByteOrder.LITTLE_ENDIAN);

    private static final VarHandle bytesAsShortBE
            = MethodHandles.byteArrayViewVarHandle(
                    short[].class, ByteOrder.BIG_ENDIAN);

    private static final VarHandle bytesAsIntLE
            = MethodHandles.byteArrayViewVarHandle(
                    int[].class, ByteOrder.LITTLE_ENDIAN);

    private static final VarHandle bytesAsIntBE
            = MethodHandles.byteArrayViewVarHandle(
                    int[].class, ByteOrder.BIG_ENDIAN);

    private static final VarHandle bytesAsLongLE
            = MethodHandles.byteArrayViewVarHandle(
                    long[].class, ByteOrder.LITTLE_ENDIAN);

    private static final VarHandle bytesAsLongBE
            = MethodHandles.byteArrayViewVarHandle(
                    long[].class, ByteOrder.BIG_ENDIAN);

    public static short getShortBE(byte[] buf, int index) {
        return (short)bytesAsShortBE.get(buf, index);
    }

    public static short getShortLE(byte[] buf, int index) {
        return (short)bytesAsShortLE.get(buf, index);
    }

    public static int getIntBE(byte[] buf, int index) {
        return (int)bytesAsIntBE.get(buf, index);
    }

    public static int getIntLE(byte[] buf, int index) {
        return (int)bytesAsIntLE.get(buf, index);
    }

    public static long getLongBE(byte[] buf, int index) {
        return (long)bytesAsLongBE.get(buf, index);
    }

    public static long getLongLE(byte[] buf, int index) {
        return (long)bytesAsLongLE.get(buf, index);
    }

    public static void setShortBE(byte[] buf, int index, short value) {
        bytesAsShortBE.set(buf, index, value);
    }

    public static void setShortLE(byte[] buf, int index, short value) {
        bytesAsShortLE.set(buf, index, value);
    }

    public static void setIntBE(byte[] buf, int index, int value) {
        bytesAsIntBE.set(buf, index, value);
    }

    public static void setIntLE(byte[] buf, int index, int value) {
        bytesAsIntLE.set(buf, index, value);
    }

    public static void setLongBE(byte[] buf, int index, long value) {
        bytesAsLongBE.set(buf, index, value);
    }

    public static void setLongLE(byte[] buf, int index, long value) {
        bytesAsLongLE.set(buf, index, value);
    }

    private ByteArrayView() {}
}
