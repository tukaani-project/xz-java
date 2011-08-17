/*
 * LZMA2Options
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.InputStream;
import java.io.IOException;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lzma.LZMAEncoder;

/**
 * LZMA2 compression options.
 * <p>
 * While this allows setting the LZMA2 compression options in detail,
 * often you only need <code>LZMA2Options()</code> or
 * <code>LZMA2Options(int)</code>.
 */
public class LZMA2Options extends FilterOptions {
    /**
     * Minimum valid compression preset level is 0.
     */
    public static final int PRESET_MIN = 0;

    /**
     * Maximum valid compression preset level is 9.
     */
    public static final int PRESET_MAX = 9;

    /**
     * Default compression preset level is 6.
     */
    public static final int PRESET_DEFAULT = 6;

    /**
     * Minimum dictionary size is 4 KiB.
     */
    public static final int DICT_SIZE_MIN = 4096;

    /**
     * Maximum dictionary size for compression.
     * <p>
     * The decompressor supports bigger dictionaries, up to almost 2 GiB.
     * With HC4 the encoder would support dictionaries bigger than 768 MiB.
     * The 768 MiB limit comes from the current implementation of BT4 where
     * we would otherwise hit the limits of signed ints in array indexing.
     * <p>
     * If you really need bigger dictionary for decompression,
     * use {@link LZMA2InputStream} directly.
     */
    public static final int DICT_SIZE_MAX = 768 << 20;

    /**
     * The default dictionary size is 8 MiB.
     */
    public static final int DICT_SIZE_DEFAULT = 8 << 20;

    /**
     * Maximum value for lc + lp is 4.
     */
    public static final int LC_LP_MAX = 4;

    /**
     * The default number of literal context bits is 3.
     */
    public static final int LC_DEFAULT = 3;

    /**
     * The default number of literal position bits is 0.
     */
    public static final int LP_DEFAULT = 0;

    /**
     * Maximum value for pb is 4.
     */
    public static final int PB_MAX = 4;

    /**
     * The default number of position bits is 2.
     */
    public static final int PB_DEFAULT = 2;

    /**
     * Compression mode: uncompressed.
     * The data is wrapped into a LZMA2 stream without compression.
     */
    public static final int MODE_UNCOMPRESSED = 0;

    /**
     * Compression mode: fast.
     * This is usually combined with a hash chain match finder.
     */
    public static final int MODE_FAST = LZMAEncoder.MODE_FAST;

    /**
     * Compression mode: normal.
     * This is usually combined with a binary tree match finder.
     */
    public static final int MODE_NORMAL = LZMAEncoder.MODE_NORMAL;

    /**
     * Minimum value for <code>niceLen</code> is 8.
     */
    public static final int NICE_LEN_MIN = 8;

    /**
     * Maximum value for <code>niceLen</code> is 273.
     */
    public static final int NICE_LEN_MAX = 273;

    /**
     * Match finder: Hash Chain 2-3-4
     */
    public static final int MF_HC4 = LZEncoder.MF_HC4;

    /**
     * Match finder: Binary tree 2-3-4
     */
    public static final int MF_BT4 = LZEncoder.MF_BT4;

    private static final int[] presetToDictSize = {
            1 << 18, 1 << 20, 1 << 21, 1 << 22, 1 << 22,
            1 << 23, 1 << 23, 1 << 24, 1 << 25, 1 << 26 };

    private static final int[] presetToDepthLimit = { 4, 8, 24, 48 };

    private int dictSize;
    private byte[] presetDict = null;
    private int lc;
    private int lp;
    private int pb;
    private int mode;
    private int niceLen;
    private int mf;
    private int depthLimit;

    /**
     * Creates new LZMA2 options and sets them to the default values.
     * This is equivalent to <code>LZMA2Options(PRESET_DEFAULT)</code>.
     */
    public LZMA2Options() throws UnsupportedOptionsException {
        setPreset(PRESET_DEFAULT);
    }

    /**
     * Creates new LZMA2 options and sets them to the given preset.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>preset</code> is not supported
     */
    public LZMA2Options(int preset) throws UnsupportedOptionsException {
        setPreset(preset);
    }

    /**
     * Creates new LZMA2 options and sets them to the given custom values.
     *
     * @throws      UnsupportedOptionsException
     *                          unsupported options were specified
     */
    public LZMA2Options(int dictSize, int lc, int lp, int pb, int mode,
                        int niceLen, int mf, int depth)
            throws UnsupportedOptionsException {
        setDictSize(dictSize);
        setLcLp(lc, lp);
        setPb(pb);
        setMode(mode);
        setNiceLen(niceLen);
        setMatchFinder(mf);
        setDepthLimit(depthLimit);
    }

    /**
     * Sets the compression options to the given preset.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>preset</code> is not supported
     */
    public void setPreset(int preset) throws UnsupportedOptionsException {
        if (preset < 0 || preset > 9)
            throw new UnsupportedOptionsException(
                    "Unsupported preset: " + preset);

        lc = LC_DEFAULT;
        lp = LP_DEFAULT;
        pb = PB_DEFAULT;
        dictSize = presetToDictSize[preset];

        if (preset <= 3) {
            mode = MODE_FAST;
            mf = MF_HC4;
            niceLen = preset <= 1 ? 128 : NICE_LEN_MAX;
            depthLimit = presetToDepthLimit[preset];
        } else {
            mode = MODE_NORMAL;
            mf = MF_BT4;
            niceLen = (preset == 4) ? 16 : (preset == 5) ? 32 : 64;
            depthLimit = 0;
        }
    }

    /**
     * Sets the dictionary size in bytes.
     * Any value in the range [DICT_SIZE_MIN, DICT_SIZE_MAX] is valid,
     * but sizes of 2^n and 2^n&nbsp;+&nbsp;2^(n-1) bytes are somewhat
     * recommended.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>dictSize</code> is not supported
     */
    public void setDictSize(int dictSize) throws UnsupportedOptionsException {
        if (dictSize < DICT_SIZE_MIN)
            throw new UnsupportedOptionsException(
                    "LZMA2 dictionary size must be at least 4 KiB: "
                    + dictSize + " B");

        if (dictSize > DICT_SIZE_MAX)
            throw new UnsupportedOptionsException(
                    "LZMA2 dictionary size must not exceed "
                    + (DICT_SIZE_MAX >> 20) + " MiB: " + dictSize + " B");

        this.dictSize = dictSize;
    }

    /**
     * Gets the dictionary size in bytes.
     */
    public int getDictSize() {
        return dictSize;
    }

    /**
     * Sets a preset dictionary. Use null to disable the use of
     * a preset dictionary. By default there is no preset dictionary.
     * <p>
     * <b>The .xz format doesn't support a preset dictionary for now.
     * Do not set a preset dictionary unless you use raw LZMA2.</b>
     */
    public void setPresetDict(byte[] presetDict) {
        this.presetDict = presetDict;
    }

    /**
     * Gets the preset dictionary.
     */
    public byte[] getPresetDict() {
        return presetDict;
    }

    /**
     * Sets the number of literal context bits and literal position bits.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>lc</code> and <code>lp</code>
     *                          are invalid
     */
    public void setLcLp(int lc, int lp) throws UnsupportedOptionsException {
        if (lc < 0 || lp < 0 || lc > LC_LP_MAX || lp > LC_LP_MAX
                || lc + lp > LC_LP_MAX)
            throw new UnsupportedOptionsException(
                    "lc + lp must not exceed " + LC_LP_MAX + ": "
                    + lc + " + " + lp);

        this.lc = lc;
        this.lp = lp;
    }

    /**
     * Sets the number of literal context bits.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>lc</code> is invalid, or the sum
     *                          of <code>lc</code> and <code>lp</code>
     *                          exceed LC_LP_MAX
     */
    public void setLc(int lc) throws UnsupportedOptionsException {
        setLcLp(lc, lp);
    }

    /**
     * Sets the number of literal position bits.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>lp</code> is invalid, or the sum
     *                          of <code>lc</code> and <code>lp</code>
     *                          exceed LC_LP_MAX
     */
    public void setLp(int lp) throws UnsupportedOptionsException {
        setLcLp(lc, lp);
    }

    /**
     * Gets the number of literal context bits.
     */
    public int getLc() {
        return lc;
    }

    /**
     * Gets the number of literal position bits.
     */
    public int getLp() {
        return lp;
    }

    /**
     * Sets the number of position bits.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>pb</code> is invalid
     */
    public void setPb(int pb) throws UnsupportedOptionsException {
        if (pb < 0 || pb > PB_MAX)
            throw new UnsupportedOptionsException(
                    "pb must not exceed " + PB_MAX + ": " + pb);

        this.pb = pb;
    }

    /**
     * Gets the number of position bits.
     */
    public int getPb() {
        return pb;
    }

    /**
     * Sets the compression mode.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>mode</code> is not supported
     */
    public void setMode(int mode) throws UnsupportedOptionsException {
        if (mode < MODE_UNCOMPRESSED || mode > MODE_NORMAL)
            throw new UnsupportedOptionsException(
                    "Unsupported compression mode: " + mode);

        this.mode = mode;
    }

    /**
     * Gets the compression mode.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Sets the nice length of matches.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>nice</code> is invalid
     */
    public void setNiceLen(int niceLen) throws UnsupportedOptionsException {
        if (niceLen < NICE_LEN_MIN)
            throw new UnsupportedOptionsException(
                    "Minimum nice length of matches is "
                    + NICE_LEN_MIN + " bytes: " + niceLen);

        if (niceLen > NICE_LEN_MAX)
            throw new UnsupportedOptionsException(
                    "Maximum nice length of matches is " + NICE_LEN_MAX
                    + ": " + niceLen);

        this.niceLen = niceLen;
    }

    /**
     * Gets the nice length of matches.
     */
    public int getNiceLen() {
        return niceLen;
    }

    /**
     * Sets the match finder type.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>mf</code> is not supported
     */
    public void setMatchFinder(int mf) throws UnsupportedOptionsException {
        if (mf != MF_HC4 && mf != MF_BT4)
            throw new UnsupportedOptionsException(
                    "Unsupported match finder: " + mf);

        this.mf = mf;
    }

    /**
     * Gets the match finder type.
     */
    public int getMatchFinder() {
        return mf;
    }

    /**
     * Sets the match finder search depth limit.
     * <p>
     * The special value of <code>0</code> indicates that the depth limit
     * should be automatically calculated by the selected match finder
     * from the nice length of matches.
     *
     * @throws      UnsupportedOptionsException
     *                          <code>depthLimit</code> is invalid
     */
    public void setDepthLimit(int depthLimit)
            throws UnsupportedOptionsException {
        if (depthLimit < 0)
            throw new UnsupportedOptionsException(
                    "Depth limit cannot be negative: " + depthLimit);

        this.depthLimit = depthLimit;
    }

    /**
     * Gets the match finder search depth limit.
     */
    public int getDepthLimit() {
        return depthLimit;
    }

    public int getEncoderMemoryUsage() {
        return (mode == MODE_UNCOMPRESSED)
               ? UncompressedLZMA2OutputStream.getMemoryUsage()
               : LZMA2OutputStream.getMemoryUsage(this);
    }

    public FinishableOutputStream getOutputStream(FinishableOutputStream out) {
        if (mode == MODE_UNCOMPRESSED)
            return new UncompressedLZMA2OutputStream(out);

        return new LZMA2OutputStream(out, this);
    }

    /**
     * Gets how much memory the LZMA2 decoder will need to decompress the data
     * that was encoded with these options and stored in a .xz file.
     * <p>
     * The returned value may bigger than the value returned by a direct call
     * to {@link LZMA2OutputStream#getMemoryUsage(int)} if the dictionary size
     * is not 2^n or 2^n&nbsp;+&nbsp;2^(n-1) bytes. This is because the .xz
     * headers store the dictionary size in such a format and other values
     * are rounded up to the next such value. Such rounding is harmess except
     * it might waste some memory if an unsual dictionary size is used.
     * <p>
     * If you use raw LZMA2 streams and unusual dictioanary size, call
     * {@link LZMA2InputStream#getMemoryUsage} directly to get raw decoder
     * memory requirements.
     */
    public int getDecoderMemoryUsage() {
        // Round the dictionary size up to the next 2^n or 2^n + 2^(n-1).
        int d = dictSize - 1;
        d |= d >>> 2;
        d |= d >>> 3;
        d |= d >>> 4;
        d |= d >>> 8;
        d |= d >>> 16;
        return LZMA2InputStream.getMemoryUsage(d + 1);
    }

    public InputStream getInputStream(InputStream in) throws IOException {
        return new LZMA2InputStream(in, dictSize);
    }

    FilterEncoder getFilterEncoder() {
        return new LZMA2Encoder(this);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            assert false;
            throw new RuntimeException();
        }
    }
}
