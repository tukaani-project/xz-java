// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.index;

import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CheckedOutputStream;
import org.tukaani.xz.common.EncoderUtil;
import org.tukaani.xz.XZIOException;

public class IndexEncoder extends IndexBase {
    private final ArrayList<IndexRecord> records
            = new ArrayList<IndexRecord>();

    public IndexEncoder() {
        super(new XZIOException("XZ Stream or its Index has grown too big"));
    }

    @Override
    public void add(long unpaddedSize, long uncompressedSize)
            throws XZIOException {
        super.add(unpaddedSize, uncompressedSize);
        records.add(new IndexRecord(unpaddedSize, uncompressedSize));
    }

    public void encode(OutputStream out) throws IOException {
        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        CheckedOutputStream outChecked = new CheckedOutputStream(out, crc32);

        // Index Indicator
        outChecked.write(0x00);

        // Number of Records
        EncoderUtil.encodeVLI(outChecked, recordCount);

        // List of Records
        for (IndexRecord record : records) {
            EncoderUtil.encodeVLI(outChecked, record.unpadded);
            EncoderUtil.encodeVLI(outChecked, record.uncompressed);
        }

        // Index Padding
        for (int i = getIndexPaddingSize(); i > 0; --i)
            outChecked.write(0x00);

        // CRC32
        long value = crc32.getValue();
        for (int i = 0; i < 4; ++i)
            out.write((byte)(value >>> (i * 8)));
    }
}
