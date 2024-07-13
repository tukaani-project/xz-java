// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.InputStream;
import java.io.IOException;

/**
 * Input stream with random access support.
 */
public abstract class SeekableInputStream extends InputStream {
    /**
     * Seeks {@code n} bytes forward in this stream.
     * <p>
     * This will not seek past the end of the file. If the current position
     * is already at or past the end of the file, this doesn't seek at all
     * and returns {@code 0}. Otherwise, if skipping {@code n} bytes
     * would cause the position to exceed the stream size, this will do
     * equivalent of {@code seek(length())} and the return value will
     * be adjusted accordingly.
     * <p>
     * If {@code n} is negative, the position isn't changed and
     * the return value is {@code 0}. It doesn't seek backward
     * because it would conflict with the specification of
     * {@link java.io.InputStream#skip(long) InputStream.skip}.
     *
     * @return      {@code 0} if {@code n} is negative,
     *              less than {@code n} if skipping {@code n}
     *              bytes would seek past the end of the file,
     *              {@code n} otherwise
     *
     * @throws      IOException might be thrown by {@link #seek(long)}
     */
    public long skip(long n) throws IOException {
        if (n <= 0)
            return 0;

        long size = length();
        long pos = position();
        if (pos >= size)
            return 0;

        if (size - pos < n)
            n = size - pos;

        seek(pos + n);
        return n;
    }

    /**
     * Gets the size of the stream.
     */
    public abstract long length() throws IOException;

    /**
     * Gets the current position in the stream.
     */
    public abstract long position() throws IOException;

    /**
     * Seeks to the specified absolute position in the stream.
     * <p>
     * Seeking past the end of the file should be supported by the subclasses
     * unless there is a good reason to do otherwise. If one has seeked
     * past the end of the stream, {@code read} will return
     * {@code -1} to indicate end of stream.
     *
     * @param       pos         new read position in the stream
     *
     * @throws      IOException if {@code pos} is negative or if
     *                          a stream-specific I/O error occurs
     */
    public abstract void seek(long pos) throws IOException;
}
