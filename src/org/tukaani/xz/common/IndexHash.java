/*
 * IndexHash
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CheckedInputStream;
import org.tukaani.xz.CorruptedInputException;

public class IndexHash {
    private long blocksSizeSum = 0;
    private long uncompressedSizeSum = 0;
    private long indexListSize = 0;
    private long recordCount = 0;
    private org.tukaani.xz.check.Check hash;

    public IndexHash() {
        try {
            hash = new org.tukaani.xz.check.SHA256();
        } catch (java.security.NoSuchAlgorithmException e) {
            hash = new org.tukaani.xz.check.CRC32();
        }
    }

    public void update(long unpaddedSize, long uncompressedSize)
            throws IOException {
        blocksSizeSum += (unpaddedSize + 3) & ~3;
        uncompressedSizeSum += uncompressedSizeSum;
        indexListSize += Util.getVLISize(unpaddedSize)
                         + Util.getVLISize(uncompressedSize);
        ++recordCount;

        if (blocksSizeSum < 0 || uncompressedSizeSum < 0
                || Util.getIndexSize(recordCount, indexListSize)
                    > Util.BACKWARD_SIZE_MAX
                || Util.getStreamSizeFromIndex(blocksSizeSum,
                    recordCount, indexListSize) < 0)
            throw new CorruptedInputException();

        ByteBuffer buf = ByteBuffer.allocate(2 * 8);
        buf.putLong(unpaddedSize);
        buf.putLong(uncompressedSize);
        hash.update(buf.array());
    }

    public void validate(InputStream in) throws IOException {
        // Index Indicator (0x00) has already been read by BlockInputStream
        // so add 0x00 to the CRC32 here.
        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        crc32.update('\0');
        CheckedInputStream inChecked = new CheckedInputStream(in, crc32);

        // Get and validate the Number of Records field.
        long storedRecordCount = DecoderUtil.decodeVLI(inChecked);
        if (storedRecordCount != recordCount)
            throw new CorruptedInputException("XZ Index is corrupt");

        // Decode and hash the Index field and compare it to
        // the hash value calculated from the decoded Blocks.
        IndexHash stored = new IndexHash();
        for (long i = 0; i < recordCount; ++i) {
            long unpaddedSize = DecoderUtil.decodeVLI(inChecked);
            long totalSize = DecoderUtil.decodeVLI(inChecked);

            try {
                stored.update(unpaddedSize, totalSize);
            } catch (CorruptedInputException e) {
                throw new CorruptedInputException("XZ Index is corrupt");
            }

            if (stored.blocksSizeSum > blocksSizeSum
                    || stored.uncompressedSizeSum > uncompressedSizeSum
                    || stored.indexListSize > indexListSize)
                throw new CorruptedInputException("XZ Index is corrupt");
        }

        if (stored.blocksSizeSum != blocksSizeSum
                || stored.uncompressedSizeSum != uncompressedSizeSum
                || stored.indexListSize != indexListSize
                || !Arrays.equals(stored.hash.finish(), hash.finish()))
            throw new CorruptedInputException("XZ Index is corrupt");

        // Index Padding
        DataInputStream inData = new DataInputStream(inChecked);
        for (int i = Util.getIndexPaddingSize(recordCount, indexListSize);
                i > 0; --i)
            if (inData.readUnsignedByte() != 0x00)
                throw new CorruptedInputException("XZ Index is corrupt");

        // CRC32
        long value = crc32.getValue();
        for (int i = 0; i < 4; ++i)
            if (((value >>> (i * 8)) & 0xFF) != inData.readUnsignedByte())
                throw new CorruptedInputException("XZ Index is corrupt");
    }

    public long getBackwardSize() {
        return Util.getIndexSize(recordCount, indexListSize);
    }
}
