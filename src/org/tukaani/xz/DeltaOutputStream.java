// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.IOException;
import org.tukaani.xz.delta.DeltaEncoder;

class DeltaOutputStream extends FinishableOutputStream {
    private static final int FILTER_BUF_SIZE = 4096;

    private FinishableOutputStream out;
    private final DeltaEncoder delta;
    private final byte[] filterBuf = new byte[FILTER_BUF_SIZE];

    private boolean finished = false;
    private IOException exception = null;

    private final byte[] tempBuf = new byte[1];

    static int getMemoryUsage() {
        return 1 + FILTER_BUF_SIZE / 1024;
    }

    DeltaOutputStream(FinishableOutputStream out, DeltaOptions options) {
        this.out = out;
        delta = new DeltaEncoder(options.getDistance());
    }

    @Override
    public void write(int b) throws IOException {
        tempBuf[0] = (byte)b;
        write(tempBuf, 0, 1);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length)
            throw new IndexOutOfBoundsException();

        if (exception != null)
            throw exception;

        if (finished)
            throw new XZIOException("Stream finished");

        try {
            while (len > FILTER_BUF_SIZE) {
                delta.encode(buf, off, FILTER_BUF_SIZE, filterBuf);
                out.write(filterBuf);
                off += FILTER_BUF_SIZE;
                len -= FILTER_BUF_SIZE;
            }

            delta.encode(buf, off, len, filterBuf);
            out.write(filterBuf, 0, len);
        } catch (IOException e) {
            exception = e;
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        if (exception != null)
            throw exception;

        if (finished)
            throw new XZIOException("Stream finished or closed");

        try {
            out.flush();
        } catch (IOException e) {
            exception = e;
            throw e;
        }
    }

    @Override
    public void finish() throws IOException {
        if (!finished) {
            if (exception != null)
                throw exception;

            try {
                out.finish();
            } catch (IOException e) {
                exception = e;
                throw e;
            }

            finished = true;
        }
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                if (exception == null)
                    exception = e;
            }

            out = null;
        }

        if (exception != null)
            throw exception;
    }
}
