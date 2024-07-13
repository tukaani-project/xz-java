// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Counts the number of bytes written to an output stream.
 * <p>
 * The {@code finish} method does nothing.
 * This is {@code FinishableOutputStream} instead
 * of {@code OutputStream} solely because it allows
 * using this as the output stream for a chain of raw filters.
 */
class CountingOutputStream extends FinishableOutputStream {
    private final OutputStream out;
    private long size = 0;

    public CountingOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        if (size >= 0)
            ++size;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        if (size >= 0)
            size += len;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public long getSize() {
        return size;
    }
}
