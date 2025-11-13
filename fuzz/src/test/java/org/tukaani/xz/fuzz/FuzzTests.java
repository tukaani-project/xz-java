package org.tukaani.xz.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.tukaani.xz.ARM64Options;
import org.tukaani.xz.ARMOptions;
import org.tukaani.xz.ARMThumbOptions;
import org.tukaani.xz.DeltaOptions;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.IA64Options;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;
import org.tukaani.xz.PowerPCOptions;
import org.tukaani.xz.RISCVOptions;
import org.tukaani.xz.SPARCOptions;
import org.tukaani.xz.SeekableInputStream;
import org.tukaani.xz.SeekableXZInputStream;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.X86Options;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class FuzzTests {
    @FuzzTest(maxDuration = "15m")
    public void fuzzXZEncoderDecoder(FuzzedDataProvider fdp) {
        // Use fuzzer data to set various options
        int preset = fdp.consumeInt(0, LZMA2Options.PRESET_MAX);
        int checkType = fdp.pickValue(new int[]{XZ.CHECK_NONE, XZ.CHECK_CRC32, XZ.CHECK_CRC64, XZ.CHECK_SHA256});
        boolean useDelta = fdp.consumeBoolean();
        boolean useBCJ = fdp.consumeBoolean();
        int deltaDist = useDelta ? fdp.consumeInt(DeltaOptions.DISTANCE_MIN, DeltaOptions.DISTANCE_MAX) : 1;
        byte[] inputData = fdp.consumeRemainingAsBytes();

        FilterOptions[] filters = new FilterOptions[useDelta && useBCJ ? 3 : useDelta || useBCJ ? 2 : 1];
        try {
            int idx = 0;
            if (useDelta) {
                filters[idx++] = new DeltaOptions(deltaDist);
            }
            if(useBCJ) {
                filters[idx++] = getRandomBCJ(fdp);
            }
            LZMA2Options lzma2 = new LZMA2Options(preset);
            filters[idx] = lzma2;
        } catch (UnsupportedOptionsException e) {
            throw new RuntimeException(e);
        }

        try {
            // Encode
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (XZOutputStream xzos = new XZOutputStream(baos, filters, checkType)) {
                xzos.write(inputData);
            }
            byte[] compressed = baos.toByteArray();

            // Decode
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            try (XZInputStream xzis = new XZInputStream(bais)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = xzis.read(buffer)) != -1) {
                    result.write(buffer, 0, n);
                }
            }

            // Verify roundtrip
            byte[] decompressed = result.toByteArray();
            if (!java.util.Arrays.equals(inputData, decompressed)) {
                throw new AssertionError("Roundtrip failed: data corrupted");
            }

        } catch (IOException ignored) {
        }
    }

    @FuzzTest(maxDuration = "15m")
    public void fuzzLZMAEncoderDecoder(FuzzedDataProvider fdp) {
        // Use fuzzer data to set various options
        int lc = fdp.consumeInt(0, LZMA2Options.LC_LP_MAX);
        int lp = fdp.consumeInt(0, LZMA2Options.LC_LP_MAX);
        if (lc + lp > 4) {
            return;
        }
        int dictSize = fdp.consumeInt(LZMA2Options.DICT_SIZE_MIN, 1024 * 1024);
        int pb = fdp.consumeInt(0, LZMA2Options.PB_MAX);
        byte[] inputData = fdp.consumeRemainingAsBytes();

        LZMA2Options options = new LZMA2Options();
        try {
            options.setLcLp(lc, lp);
            options.setPb(pb);
            options.setDictSize(dictSize);
        } catch (UnsupportedOptionsException e) {
            throw new RuntimeException(e);
        }

        try {
            // Encode
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (LZMAOutputStream lzmaOut = new LZMAOutputStream(baos, options, -1L)) {
                lzmaOut.write(inputData);
            }
            byte[] compressed = baos.toByteArray();

            // Decode
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            try (LZMAInputStream lzmaIn = new LZMAInputStream(bais)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = lzmaIn.read(buffer)) != -1) {
                    result.write(buffer, 0, n);
                }
            }

            // Verify roundtrip
            byte[] decompressed = result.toByteArray();
            if (!java.util.Arrays.equals(inputData, decompressed)) {
                throw new AssertionError("Roundtrip failed: data corrupted");
            }

        } catch (IOException ignored) {
        }
    }

    @FuzzTest(maxDuration = "15m")
    public void fuzzSeekableXZ(FuzzedDataProvider fdp) {
        byte[] inputData = fdp.consumeBytes(2048);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XZOutputStream xzos = new XZOutputStream(baos, new LZMA2Options());
            xzos.write(inputData);
            xzos.close();

            byte[] compressed = baos.toByteArray();

            SeekableInMemoryByteSource source = new SeekableInMemoryByteSource(compressed);
            SeekableXZInputStream seekStream = new SeekableXZInputStream(source);

            int operations = fdp.consumeInt(1, 20);
            for (int i = 0; i < operations; i++) {
                if (fdp.consumeBoolean()) {
                    long pos = fdp.consumeLong(0, inputData.length);
                    seekStream.seek(pos);
                } else {
                    seekStream.read();
                }
            }
            seekStream.close();

        } catch (IOException ignored) {
        }
    }

    private static class SeekableInMemoryByteSource extends SeekableInputStream {
        private final byte[] buf;
        private int pos;

        public SeekableInMemoryByteSource(byte[] buf) {
            this.buf = buf;
            this.pos = 0;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (pos >= buf.length) return -1;
            int avail = buf.length - pos;
            if (len > avail) len = avail;
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        @Override
        public void seek(long pos) throws IOException {
            this.pos = (int) pos;
        }

        @Override
        public long length() throws IOException {
            return buf.length;
        }

        @Override
        public long position() throws IOException {
            return pos;
        }

        @Override
        public void close() throws IOException {}

        @Override
        public int read() throws IOException {
            if (pos >= buf.length) return -1;
            return buf[pos++] & 0xFF;
        }
    }

    private FilterOptions getRandomBCJ(FuzzedDataProvider fdp) {
        switch (fdp.consumeInt(0, 7)) {
            case 0: return new X86Options();
            case 1: return new ARMOptions();
            case 2: return new ARM64Options();
            case 3: return new ARMThumbOptions();
            case 4: return new PowerPCOptions();
            case 5: return new IA64Options();
            case 6: return new RISCVOptions();
            default: return new SPARCOptions();
        }
    }
}