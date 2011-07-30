/*
 * DeltaOutputStream
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.IOException;
import org.tukaani.xz.delta.DeltaEncoder;

class DeltaOutputStream extends FinishableOutputStream {
    private static final int TMPBUF_SIZE = 4096;

    private final FinishableOutputStream out;
    private final DeltaEncoder delta;
    private final byte[] tmpbuf = new byte[TMPBUF_SIZE];

    static int getMemoryUsage() {
        return 1 + TMPBUF_SIZE / 1024;
    }

    DeltaOutputStream(FinishableOutputStream out, DeltaOptions options) {
        this.out = out;
        delta = new DeltaEncoder(options.getDistance());
    }

    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte)b;
        write(buf, 0, 1);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length)
            throw new IndexOutOfBoundsException();

        while (len > TMPBUF_SIZE) {
            delta.encode(buf, off, TMPBUF_SIZE, tmpbuf);
            out.write(tmpbuf);
            off += TMPBUF_SIZE;
            len -= TMPBUF_SIZE;
        }

        delta.encode(buf, off, len, tmpbuf);
        out.write(tmpbuf, 0, len);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void finish() throws IOException {
        out.finish();
    }

    public void close() throws IOException {
        out.close();
    }
}
