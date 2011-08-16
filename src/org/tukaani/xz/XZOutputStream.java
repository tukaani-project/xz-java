/*
 * XZOutputStream
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.OutputStream;
import java.io.IOException;
import java.util.zip.CheckedOutputStream;
import org.tukaani.xz.common.EncoderUtil;
import org.tukaani.xz.common.StreamFlags;
import org.tukaani.xz.check.Check;
import org.tukaani.xz.index.IndexEncoder;

/**
 * Compresses into the .xz file format.
 *
 * <h4>Examples</h4>
 * <p>
 * Getting an output stream to compress with LZMA2 using the default
 * settings and the default integrity check type (CRC64):
 * <p><blockquote><pre>
 * FileOutputStream outfile = new FileOutputStream("foo.xz");
 * XZOutputStream outxz = new XZOutputStream(outfile, new LZMA2Options());
 * </pre></blockquote>
 * <p>
 * Using the preset level <code>8</code> for LZMA2 (the default
 * is <code>6</code>) and SHA-256 instead of CRC64 for integrity checking:
 * <p><blockquote><pre>
 * XZOutputStream outxz = new XZOutputStream(outfile, new LZMA2Options(8),
 *                                           XZ.CHECK_SHA256);
 * </pre></blockquote>
 * <p>
 * Using the x86 BCJ filter together with LZMA2 to compress x86 executables
 * and printing the memory usage information before creating the
 * XZOutputStream:
 * <p><blockquote><pre>
 * X86Options x86 = new X86Options();
 * LZMA2Options lzma2 = new LZMA2Options();
 * FilterOptions options = { x86, lzma2 };
 * System.out.println("Encoder memory usage: "
 *                    + FilterOptions.getEncoderMemoryUsage(options)
 *                    + " KiB");
 * System.out.println("Decoder memory usage: "
 *                    + FilterOptions.getDecoderMemoryUsage(options)
 *                    + " KiB");
 * XZOutputStream outxz = new XZOutputStream(outfile, options);
 * </pre></blockquote>
 */
public class XZOutputStream extends FinishableOutputStream {
    private OutputStream out;
    private StreamFlags streamFlags = new StreamFlags();
    private Check check;
    private IndexEncoder index = new IndexEncoder();

    private BlockOutputStream blockEncoder = null;
    private FilterEncoder[] filters;

    /**
     * True if the current filter chain supports flushing.
     * If it doesn't support flushing, <code>flush()</code>
     * will use <code>flushBlock()</code> as a fallback.
     */
    private boolean filtersSupportFlushing;

    /**
     * True if <code>flush()</code> should use <code>flushBlock()</code>
     * even if the filter chain does support flushing.
     */
    private boolean alwaysFlushBlock = false;

    private IOException exception = null;
    private boolean finished = false;

    /**
     * Creates a new XZ compressor using one filter and CRC64 as
     * the integrity check. This constructor is equivalent to passing
     * a single-member FilterOptions array to
     * <code>XZOutputStream(OutputStream, FilterOptions[])</code>.
     *
     * @param       out         output stream to which the compressed data
     *                          will be written
     *
     * @param       filterOptions
     *                          filter options to use
     *
     * @throws      UnsupportedOptionsException
     *                          invalid filter chain
     *
     * @throws      IOException may be thrown from <code>out</code>
     */
    public XZOutputStream(OutputStream out, FilterOptions filterOptions)
            throws IOException {
        this(out, filterOptions, XZ.CHECK_CRC64);
    }

    /**
     * Creates a new XZ compressor using one filter and the specified
     * integrity check type. This constructor is equivalent to
     * passing a single-member FilterOptions array to
     * <code>XZOutputStream(OutputStream, FilterOptions[], int)</code>.
     *
     * @param       out         output stream to which the compressed data
     *                          will be written
     *
     * @param       filterOptions
     *                          filter options to use
     *
     * @param       checkType   type of the integrity check,
     *                          for example XZ.CHECK_CRC32
     *
     * @throws      UnsupportedOptionsException
     *                          invalid filter chain
     *
     * @throws      IOException may be thrown from <code>out</code>
     */
    public XZOutputStream(OutputStream out, FilterOptions filterOptions,
                          int checkType) throws IOException {
        this(out, new FilterOptions[] { filterOptions }, checkType);
    }

    /**
     * Creates a new XZ compressor using 1-4 filters and CRC64 as
     * the integrity check. This constructor is equivalent
     * <code>XZOutputStream(out, filterOptions, XZ.CHECK_CRC64)</code>.
     *
     * @param       out         output stream to which the compressed data
     *                          will be written
     *
     * @param       filterOptions
     *                          array of filter options to use
     *
     * @throws      UnsupportedOptionsException
     *                          invalid filter chain
     *
     * @throws      IOException may be thrown from <code>out</code>
     */
    public XZOutputStream(OutputStream out, FilterOptions[] filterOptions)
            throws IOException {
        this(out, filterOptions, XZ.CHECK_CRC64);
    }

    /**
     * Creates a new XZ compressor using 1-4 filters and the specified
     * integrity check type.
     *
     * @param       out         output stream to which the compressed data
     *                          will be written
     *
     * @param       filterOptions
     *                          array of filter options to use
     *
     * @param       checkType   type of the integrity check,
     *                          for example XZ.CHECK_CRC32
     *
     * @throws      UnsupportedOptionsException
     *                          invalid filter chain
     *
     * @throws      IOException may be thrown from <code>out</code>
     */
    public XZOutputStream(OutputStream out, FilterOptions[] filterOptions,
                          int checkType) throws IOException {
        this.out = out;
        updateFilters(filterOptions);

        streamFlags.checkType = checkType;
        check = Check.getInstance(checkType);

        encodeStreamHeader();
    }

    /**
     * Updates the filter chain with a single filter.
     * This is equivalent to passing a single-member FilterOptions array
     * to <code>updateFilters(FilterOptions[])</code>.
     *
     * @param       filterOptions
     *                          new filter to use
     *
     * @throws      UnsupportedOptionsException
     *                          unsupported filter chain, or trying to change
     *                          the filter chain in the middle of a Block
     */
    public void updateFilters(FilterOptions filterOptions)
            throws XZIOException {
        FilterOptions[] opts = new FilterOptions[1];
        opts[0] = filterOptions;
        updateFilters(opts);
    }

    /**
     * Updates the filter chain with 1-4 filters.
     * <p>
     * Currently this cannot be used to update e.g. LZMA2 options in the
     * middle of a XZ Block. Use <code>flushBlock()</code> to finish the
     * current XZ Block before calling this function. The new filter chain
     * will then be used for the next XZ Block.
     *
     * @param       filterOptions
     *                          new filter chain to use
     *
     * @throws      UnsupportedOptionsException
     *                          unsupported filter chain, or trying to change
     *                          the filter chain in the middle of a Block
     */
    public void updateFilters(FilterOptions[] filterOptions)
            throws XZIOException {
        if (blockEncoder != null)
            throw new UnsupportedOptionsException("Changing filter options "
                    + "in the middle of a XZ Block not implemented");

        if (filterOptions.length < 1 || filterOptions.length > 4)
            throw new UnsupportedOptionsException(
                        "XZ filter chain must be 1-4 filters");

        filtersSupportFlushing = true;
        FilterEncoder[] newFilters = new FilterEncoder[filterOptions.length];
        for (int i = 0; i < filterOptions.length; ++i) {
            newFilters[i] = filterOptions[i].getFilterEncoder();
            filtersSupportFlushing &= newFilters[i].supportsFlushing();
        }

        RawCoder.validate(newFilters);
        filters = newFilters;
    }

    /**
     * Writes one byte to be compressed.
     *
     * @throws      XZIOException
     *                          XZ Stream has grown too big
     *
     * @throws      XZIOException
     *                          <code>finish()</code> or <code>close()</code>
     *                          was already called
     *
     * @throws      IOException may be thrown by the underlying output stream
     */
    public void write(int b) throws IOException {
        byte[] buf = new byte[] { (byte)b };
        write(buf, 0, 1);
    }

    /**
     * Writes an array of bytes to be compressed.
     * The compressors tend to do internal buffering and thus the written
     * data won't be readable from the compressed output immediately.
     * Use <code>flush()</code> to force everything written so far to
     * be written to the underlaying output stream, but be aware that
     * flushing reduces compression ratio.
     *
     * @param       buf         buffer of bytes to be written
     * @param       off         start offset in <code>buf</code>
     * @param       len         number of bytes to write
     *
     * @throws      XZIOException
     *                          XZ Stream has grown too big: total file size
     *                          about 8&nbsp;EiB or the Index field exceeds
     *                          16&nbsp;GiB; you shouldn't reach these sizes
     *                          in practice
     *
     * @throws      XZIOException
     *                          <code>finish()</code> or <code>close()</code>
     *                          was already called and len &gt; 0
     *
     * @throws      IOException may be thrown by the underlying output stream
     */
    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length)
            throw new IndexOutOfBoundsException();

        if (len == 0)
            return;

        if (exception != null)
            throw exception;

        if (finished)
            throw new XZIOException("Cannot write to a finished stream");

        try {
            if (blockEncoder == null)
                blockEncoder = new BlockOutputStream(out, filters, check);

            blockEncoder.write(buf, off, len);
        } catch (IOException e) {
            exception = e;
            throw e;
        }
    }

    /**
     * Finishes the current XZ Block. This is a helper for flushBlock()
     * and finish().
     */
    private void endBlock() throws IOException {
        if (exception != null)
            throw exception;

        if (blockEncoder != null) {
            try {
                blockEncoder.finish();
                index.add(blockEncoder.getUnpaddedSize(),
                          blockEncoder.getUncompressedSize());
                blockEncoder = null;
            } catch (IOException e) {
                exception = e;
                throw e;
            }
        }
    }

    /**
     * Finishes the current XZ Block (but not the whole XZ Stream) and
     * calls <code>out.flush()</code>.
     * All buffered pending data will then be decompressible from
     * the output stream. If there is no unfinished Block open,
     * no empty Block will be created.
     * <p>
     * <code>flushBlock()</code> resets the encoder state so there will be
     * a bigger penalty in compressed file size than with <code>flush()</code>.
     * <p>
     * <code>flushBlock()</code> can be useful, for example, to create
     * random-accessible .xz files.
     *
     * @throws      XZIOException
     *                          XZ Stream has grown too big
     *
     * @throws      IOException may be thrown by the underlying output stream
     */
    public void flushBlock() throws IOException {
        endBlock();
        out.flush();
    }

    /**
     * Flushes the encoder and calls <code>out.flush()</code>.
     * All buffered pending data will then be decompressible from
     * the output stream.
     * <p>
     * Calling this function very often may increase the compressed
     * file size a lot. The filter chain options may affect the size
     * increase too. For example, with LZMA2 the HC4 match finder has
     * smaller penalty with flushing than BT4.
     * <p>
     * Some filters don't support flushing. If the filter chain has
     * such a filter, <code>flush()</code> is equivalent to
     * <code>flushBlock()</code>.
     * <p>
     * If <code>setBlockFlushing(true)</code> has been used,
     * <code>flush()</code> is equivalent to <code>flushBlock()</code>
     * even if the filter chain does support flushing.
     *
     * @throws      XZIOException
     *                          XZ Stream has grown too big
     *
     * @throws      IOException may be thrown by the underlying output stream
     */
    public void flush() throws IOException {
        if (exception != null)
            throw exception;

        if (!filtersSupportFlushing || alwaysFlushBlock)
            flushBlock();
        else if (blockEncoder != null)
            blockEncoder.flush(); // This also calls out.flush().
        else
            out.flush();
    }

    /**
     * Sets the default flushing mode for <code>flush()</code>.
     * <p>
     * Calling <code>setBlockFlushing(true)</code> will make
     * <code>flush()</code> equivalent to <code>flushBlock()</code>
     * even with filter chains that support flushing. Calling
     * <code>setBlockFlushing(false)</code> will restore the default
     * behavior.
     * <p>
     * This function is rarely useful. Normally you should use
     * <code>flushBlock()</code> directly if you want to start
     * a new XZ Block.
     */
    public void setBlockFlushing(boolean flushFlushesBlock) {
        alwaysFlushBlock = flushFlushesBlock;
    }

    /**
     * Finishes compression without closing the underlying stream.
     * No more data can be written to this stream after finishing
     * (calling <code>write</code> with an empty buffer is OK).
     * <p>
     * Repeated calls to <code>finish()</code> do nothing unless
     * an exception was thrown by this stream earlier. In that case
     * the same exception is thrown again.
     * <p>
     * After finishing, the stream may be closed normally with
     * <code>close()</code>. If the stream will be closed anyway, there
     * usually is no need to call <code>finish()</code> separately.
     *
     * @throws      XZIOException
     *                          XZ Stream has grown too big
     *
     * @throws      IOException may be thrown by the underlying output stream
     */
    public void finish() throws IOException {
        if (!finished) {
            // This checks for pending exceptions so we don't need to
            // worry about it here.
            endBlock();

            try {
                index.encode(out);
                encodeStreamFooter();
                finished = true;
            } catch (IOException e) {
                exception = e;
                throw e;
            }
        }
    }

    /**
     * Finishes compression and closes the underlying stream.
     * The underlying stream <code>out</code> is closed even if finishing
     * fails. If both finishing and closing fail, the exception thrown
     * by <code>finish()</code> is thrown and the exception from the failed
     * <code>out.close()</code> is lost.
     *
     * @throws      XZIOException
     *                          XZ Stream has grown too big
     *
     * @throws      IOException may be thrown by the underlying output stream
     */
    public void close() throws IOException {
        // If finish() throws an exception, it stores the exception to
        // the variable "exception". So we can ignore the possible
        // exception here.
        try {
            finish();
        } catch (IOException e) {}

        try {
            out.close();
        } catch (IOException e) {
            // Remember the exception but only if there is no previous
            // pending exception.
            if (exception == null)
                exception = e;
        }

        if (exception != null)
            throw exception;
    }

    private void encodeStreamFlags(byte[] buf, int off) {
        buf[off] = 0x00;
        buf[off + 1] = (byte)streamFlags.checkType;
    }

    private void encodeStreamHeader() throws IOException {
        out.write(XZ.HEADER_MAGIC);

        byte[] buf = new byte[2];
        encodeStreamFlags(buf, 0);
        out.write(buf);

        EncoderUtil.writeCRC32(out, buf);
    }

    private void encodeStreamFooter() throws IOException {
        byte[] buf = new byte[6];
        long backwardSize = index.getIndexSize() / 4 - 1;
        for (int i = 0; i < 4; ++i)
            buf[i] = (byte)(backwardSize >>> (i * 8));

        encodeStreamFlags(buf, 4);

        EncoderUtil.writeCRC32(out, buf);
        out.write(buf);
        out.write(XZ.FOOTER_MAGIC);
    }
}
