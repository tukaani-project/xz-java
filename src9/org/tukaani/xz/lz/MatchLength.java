// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.lz;

import java.nio.ByteOrder;

final class MatchLength {
    /**
     * Number of additional bytes that {@code getLen} might read even though
     * it doesn't need them. The buffer must have this many bytes of extra
     * space at the end to make it safe to use {@code getLen}.
     */
    static final int EXTRA_SIZE;

    private static final MatchLengthFinder matchLengthFinder;

    static {
        // The autodetection can be overridden with a system property.
        String prop = System.getProperty("org.tukaani.xz.MatchLengthFinder");

        if (prop == null) {
            String arch = System.getProperty("os.arch");

            // Big endian ARM64 might be rare but endianness check is cheap.
            if (arch != null
                    && arch.matches("^(amd64|x86_64|aarch64)$")
                    && ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                prop = "UnalignedLongLE";
            } else {
                prop = "Basic";
            }
        }

        switch (prop) {
            case "Basic":
                matchLengthFinder = new BasicMatchLengthFinder();
                break;

            case "UnalignedLongLE":
                matchLengthFinder = new UnalignedLongLEMatchLengthFinder();
                break;

            default:
                throw new Error("Unsupported value '" + prop +
                                "' in the system property " +
                                "org.tukaani.xz.MatchLengthFinder. " +
                                "Supported values: Basic, UnalignedLongLE");
        }

        EXTRA_SIZE = matchLengthFinder.getExtraSize();
    }

    /**
     * Compares two byte ranges in a buffer and returns how many equal bytes
     * were found before the first pair of bytes that differ.
     *
     * @param       buf         input buffer from which two byte ranges
     *                          are compared
     *
     * @param       off         index to the start of the latter byte range
     *                          (the caller's current read position)
     *
     * @param       delta       backward offset from {@code off}; unlike the
     *                          zero-based distance in other LZ code, this
     *                          value is one-based and thus {@code delta}
     *                          must be positive
     *
     * @param       len         how many bytes the caller already knows to be
     *                          equal and thus don't need to be compared
     *                          again; that is, the first bytes to compare are
     *                          {@code buf[off + len]} and
     *                          {@code buf[off + len - delta]}
     *
     * @param       lenLimit    maximum number of bytes to compare, including
     *                          the bytes already counted in {@code len}
     *
     * @return      number of bytes that are equal but
     *              at most {@code lenLimit}
     */
    static int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        assert off >= 0;
        assert delta > 0;
        assert len >= 0;
        assert lenLimit >= len;

        return matchLengthFinder.getLen(buf, off, delta, len, lenLimit);
    }

    private MatchLength() {}
}
