/*
 * LZMAOutputStream
 *
 * Authors: Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.OutputStream;
import java.io.IOException;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.rangecoder.RangeEncoderToStream;
import org.tukaani.xz.lzma.LZMAEncoder;

/**
 * Compresses into the legacy .lzma file format or into a raw LZMA stream.
 *
 * @since 1.6
 */
public class LZMAOutputStream extends FinishableOutputStream {
    private OutputStream out;

    private final LZEncoder lz;
    private final RangeEncoderToStream rc;
    private final LZMAEncoder lzma;

    private final int props;
    private final boolean useEndMarker;
    private long uncompressedSize = 0;

    private boolean finished = false;
    private IOException exception = null;

    private final byte[] tempBuf = new byte[1];

    private LZMAOutputStream(OutputStream out, LZMA2Options options,
                             boolean useHeader, boolean useEndMarker)
            throws IOException {
        if (out == null)
            throw new NullPointerException();

        this.useEndMarker = useEndMarker;

        this.out = out;
        rc = new RangeEncoderToStream(out);

        int dictSize = options.getDictSize();
        lzma = LZMAEncoder.getInstance(rc,
                options.getLc(), options.getLp(), options.getPb(),
                options.getMode(),
                dictSize, 0, options.getNiceLen(),
                options.getMatchFinder(), options.getDepthLimit());

        lz = lzma.getLZEncoder();

        byte[] presetDict = options.getPresetDict();
        if (presetDict != null && presetDict.length > 0) {
            if (useHeader)
                throw new UnsupportedOptionsException(
                        "Preset dictionary cannot be used in .lzma files "
                        + "(try a raw LZMA stream instead)");

            lz.setPresetDict(dictSize, presetDict);
        }

        props = (options.getPb() * 5 + options.getLp()) * 9 + options.getLc();

        if (useHeader) {
            out.write(props);

            for (int i = 0; i < 4; ++i) {
                out.write(dictSize & 0xFF);
                dictSize >>>= 8;
            }

            for (int i = 0; i < 8; ++i)
                out.write(0xFF);
        }
    }

    /**
     * Creates a new compressor for the legacy .lzma file format.
     * The files will always use the end of stream marker and thus
     * will not have the uncompressed size stored in the header.
     * <p>
     * Note that a preset dictionary cannot be used in .lzma files but
     * it can be used for raw LZMA streams.
     *
     * @param       out         output stream to which the compressed data
     *                          will be written
     *
     * @param       options     LZMA compression options; the same class
     *                          is used here as is for LZMA2
     *
     * @throws      IOException may be thrown from <code>out</code>
     */
    public LZMAOutputStream(OutputStream out, LZMA2Options options)
            throws IOException {
        this(out, options, true, true);
    }

    /**
     * Creates a new compressor for raw LZMA (also known as LZMA1) stream.
     * <p>
     * Raw LZMA streams can be encoded with or without end of stream marker.
     * When decompressing the stream, one must if the end marker was used
     * and tell it to the decompressor. If the end marker wasn't used, the
     * decompressor will also need to know the uncompressed size.
     *
     * @param       out         output stream to which the compressed data
     *                          will be written
     *
     * @param       options     LZMA compression options; the same class
     *                          is used here as is for LZMA2
     *
     * @param       useEndMarker
     *                          if end of stream marker should be written
     *
     * @throws      IOException may be thrown from <code>out</code>
     */
    public LZMAOutputStream(OutputStream out, LZMA2Options options,
                            boolean useEndMarker) throws IOException {
        this(out, options, false, useEndMarker);
    }

    /**
     * Returns the LZMA lc/lp/pb properties encoded into a single byte.
     * This might be useful when handling file formats other than .lzma
     * that use the same encoding for the LZMA properties as .lzma does.
     */
    public int getProps() {
        return props;
    }

    /**
     * Gets the amount of uncompressed data written to the stream.
     * This is useful when creating raw LZMA streams without
     * the end of stream marker.
     */
    public long getUncompressedSize() {
        return uncompressedSize;
    }

    public void write(int b) throws IOException {
        tempBuf[0] = (byte)b;
        write(tempBuf, 0, 1);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length)
            throw new IndexOutOfBoundsException();

        if (exception != null)
            throw exception;

        if (finished)
            throw new XZIOException("Stream finished or closed");

        uncompressedSize += len;

        try {
            while (len > 0) {
                int used = lz.fillWindow(buf, off, len);
                off += used;
                len -= used;
                lzma.encodeForLZMA1();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        }
    }

    /**
     * Flushing isn't supported and will throw XZIOException.
     */
    public void flush() throws IOException {
        throw new XZIOException("LZMAOutputStream does not support flushing");
    }

    /**
     * Finishes the stream without closing the underlying OutputStream.
     */
    public void finish() throws IOException {
        if (!finished) {
            if (exception != null)
                throw exception;

            lz.setFinishing();

            try {
                lzma.encodeForLZMA1();

                if (useEndMarker)
                    lzma.encodeLZMA1EndMarker();

                rc.finish();
            } catch (IOException e) {
                exception = e;
                throw e;
            }

            finished = true;
        }
    }

    /**
     * Finishes the stream and closes the underlying OutputStream.
     */
    public void close() throws IOException {
        if (out != null) {
            try {
                finish();
            } catch (IOException e) {}

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
