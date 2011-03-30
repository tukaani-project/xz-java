/*
 * BlockInputStream
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Arrays;
import org.tukaani.xz.raw.RawDecoder;
import org.tukaani.xz.check.Check;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.MemoryLimitException;

public class BlockInputStream extends InputStream {
    private final InputStream in;
    private final DataInputStream inData;
    private final CountingInputStream inCounted;
    private final InputStream filterChain;
    private final Check check;

    private long uncompressedSizeInHeader = -1;
    private long compressedSizeInHeader = -1;
    private long compressedSizeLimit;
    private int headerSize;
    private long uncompressedSize = 0;

    public BlockInputStream(InputStream in, Check check, int memoryLimit)
            throws IOException, IndexIndicatorException {
        this.in = in;
        this.check = check;
        inData = new DataInputStream(in);

        byte[] buf = new byte[Util.BLOCK_HEADER_SIZE_MAX];

        // Block Header Size or Index Indicator
        inData.readFully(buf, 0, 1);

        // See if this begins the Index field.
        if (buf[0] == 0x00)
            throw new IndexIndicatorException();

        // Read the rest of the Block Header.
        headerSize = 4 * (buf[0] + 1);
        inData.readFully(buf, 1, headerSize - 1);

        // Validate the CRC32.
        if (!DecoderUtil.isCRC32Valid(buf, 0, headerSize - 4, headerSize - 4))
            throw new CorruptedInputException("XZ Block Header is corrupt");

        // Check for reserved bits in Block Flags.
        if ((buf[1] & 0x3C) != 0)
            throw new UnsupportedOptionsException(
                    "Unsupported options in XZ Block Header");

        // Memory for the Filter Flags field
        int filterCount = (buf[1] & 0x03) + 1;
        long[] filterIDs = new long[filterCount];
        byte[][] filterProps = new byte[filterCount][];

        // Use a stream to parse the fields after the Block Flags field.
        // Exclude the CRC32 field at the end.
        ByteArrayInputStream bufStream = new ByteArrayInputStream(
                buf, 2, headerSize - 6);

        try {
            // Decode Compressed Size if the relevant flag is set
            // in Block Flags.
            if ((buf[1] & 0x40) != 0x00) {
                compressedSizeInHeader = DecoderUtil.decodeVLI(bufStream);
                compressedSizeLimit = compressedSizeInHeader;
            } else {
                compressedSizeLimit = (Util.VLI_MAX & ~3)
                                      - headerSize - check.getSize();
            }

            // Decode Uncompressed Size if the relevant flag is set
            // in Block Flags.
            if ((buf[1] & 0x80) != 0x00)
                uncompressedSizeInHeader = DecoderUtil.decodeVLI(bufStream);

            // Decode Filter Flags.
            for (int i = 0; i < filterCount; ++i) {
                filterIDs[i] = DecoderUtil.decodeVLI(bufStream);

                long filterPropsSize = DecoderUtil.decodeVLI(bufStream);
                if (filterPropsSize > bufStream.available())
                    throw new CorruptedInputException();

                filterProps[i] = new byte[(int)filterPropsSize];
                bufStream.read(filterProps[i]);
            }

        } catch (IOException e) {
            throw new CorruptedInputException("XZ Block Header is corrupt");
        }

        // Check that the remaining bytes are zero.
        for (int i = bufStream.available(); i > 0; --i)
            if (bufStream.read() != 0x00)
                throw new UnsupportedOptionsException(
                        "Unsupported options in XZ Block Header");

        // Check if the Filter IDs are supported, decode
        // the Filter Properties, and check that they are
        // supported by this decoder implementation.
        RawDecoder rawDecoder = new RawDecoder(filterIDs, filterProps);

        // Check the memory usage limit.
        if (memoryLimit >= 0) {
            int memoryNeeded = rawDecoder.getMemoryUsage();
            if (memoryNeeded > memoryLimit)
                throw new MemoryLimitException(memoryNeeded, memoryLimit);
        }

        // Use an input size counter to calculate
        // the size of the Compressed Data field.
        inCounted = new CountingInputStream(in);

        // Initialize the filter chain.
        filterChain = rawDecoder.getFilterChain(inCounted);
    }

    public int read() throws IOException {
        byte[] buf = new byte[1];
        return read(buf, 0, 1) == -1 ? -1 : (buf[0] & 0xFF);
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int ret = filterChain.read(buf, off, len);
        long compressedSize = inCounted.getSize();

        if (ret > 0) {
            check.update(buf, off, ret);
            uncompressedSize += ret;

            // Catch invalid values.
            if (compressedSize < 0
                    || compressedSize > compressedSizeLimit
                    || uncompressedSize < 0
                    || (uncompressedSizeInHeader != -1
                        && uncompressedSize > uncompressedSizeInHeader))
                throw new CorruptedInputException();

        } else if (ret == -1) {
            // Validate Compressed Size and Uncompressed Size if they were
            // present in Block Header.
            if ((compressedSizeInHeader != -1
                        && compressedSizeInHeader != compressedSize)
                    || (uncompressedSizeInHeader != -1
                        && uncompressedSizeInHeader != uncompressedSize))
                throw new CorruptedInputException();

            // Block Padding bytes must be zeros.
            for (long i = compressedSize; (i & 3) != 0; ++i)
                if (inData.readUnsignedByte() != 0x00)
                    throw new CorruptedInputException();

            // Validate the integrity check.
            byte[] storedCheck = new byte[check.getSize()];
            inData.readFully(storedCheck);
            if (!Arrays.equals(check.finish(), storedCheck))
                throw new CorruptedInputException("Integrity ("
                        + check.getName() + ") check does not match");
        }

        return ret;
    }

    public int available() throws IOException {
        return filterChain == null ? 0 : filterChain.available();
    }

    public long getUnpaddedSize() {
        return headerSize + inCounted.getSize() + check.getSize();
    }

    public long getUncompressedSize() {
        return uncompressedSize;
    }
}
