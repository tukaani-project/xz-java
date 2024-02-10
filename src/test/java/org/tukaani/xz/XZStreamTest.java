/*
 * XZStreamTest
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

public class XZStreamTest {
    static final byte[] BYTES = new byte[1024 * 1024];

    static {
        for (int i = 0; i < BYTES.length; ++i) {
            BYTES[i] = (byte) i;
        }

        ArrayCache.setDefaultCache(BasicArrayCache.getInstance());
    }

    @Test
    public void test_6() throws Exception {
        test(6);
    }

    @Test
    public void test_3() throws Exception {
        test(3);
    }

    @Test
    public void testFile_6() throws Exception {
        testFile(6, false);
    }

    @Test
    public void testFile_3() throws Exception {
        testFile(3, false);
    }

    @Test
    public void testFile_6_delta() throws Exception {
        testFile(6, true);
    }

    @Test
    public void testFile_3_delta() throws Exception {
        testFile(3, true);
    }

    @Test
    public void test_repeat() throws Exception {
        final byte[] bytes = new byte[16 * 1024];
        Arrays.fill(bytes, (byte) -75);

        final byte[] compressed;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final XZOutputStream xos = new XZOutputStream(baos, new LZMA2Options())) {
            for (int i = 0; i < 10240; ++i) {
                xos.write(bytes);
            }
            xos.finish();
            compressed = baos.toByteArray();
        }

        try (final ByteArrayInputStream is = new ByteArrayInputStream(compressed);
                final XZInputStream xis = new XZInputStream(is)) {
            final byte[] buffer = new byte[8 * 1024];
            int read;
            int offset = 0;
            while ((read = xis.read(buffer)) != -1) {
                int i = 0;
                for (int j = read - 7; i < j; i += 8) {
                    assertEquals("byte at: " + (offset + i), (byte) -75, buffer[i]);
                    assertEquals("byte at: " + (offset + i + 1), (byte) -75, buffer[i + 1]);
                    assertEquals("byte at: " + (offset + i + 2), (byte) -75, buffer[i + 2]);
                    assertEquals("byte at: " + (offset + i + 3), (byte) -75, buffer[i + 3]);
                    assertEquals("byte at: " + (offset + i + 4), (byte) -75, buffer[i + 4]);
                    assertEquals("byte at: " + (offset + i + 5), (byte) -75, buffer[i + 5]);
                    assertEquals("byte at: " + (offset + i + 6), (byte) -75, buffer[i + 6]);
                    assertEquals("byte at: " + (offset + i + 7), (byte) -75, buffer[i + 7]);
                }
                for (; i < read; ++i) {
                    assertEquals("byte at: " + (offset + i), (byte) -75, buffer[i]);
                }
                offset += read;
            }
        }
    }

    private static void test(int preset) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XZOutputStream xos = new XZOutputStream(baos,
                new FilterOptions[] { new DeltaOptions(13), new LZMA2Options(preset) });
        xos.write(BYTES[3]);
        xos.write(BYTES, 0, 8);
        xos.write(BYTES[10]);
        xos.write(BYTES, 0, 11);
        xos.write(BYTES, 0, 53);
        xos.write(BYTES, 0, 11);
        xos.write(BYTES, 0, 97);
        xos.write(BYTES);
        xos.finish();
        final byte[] compressed = baos.toByteArray();
        xos.close();
        final XZInputStream xis = new XZInputStream(new ByteArrayInputStream(compressed));
        final byte[] actual = new byte[BYTES.length];

        assertEquals(BYTES[3], (byte) xis.read());

        xis.read(actual, 0, 8);
        for (int i = 0; i < 8; ++i) {
            assertEquals("index: " + i, BYTES[i], actual[i]);
        }

        assertEquals(BYTES[10], (byte) xis.read());

        xis.read(actual, 0, 11);
        for (int i = 0; i < 11; ++i) {
            assertEquals("index: " + i, BYTES[i], actual[i]);
        }

        xis.read(actual, 0, 53);
        for (int i = 0; i < 53; ++i) {
            assertEquals("index: " + i, BYTES[i], actual[i]);
        }

        xis.read(actual, 0, 11);
        for (int i = 0; i < 11; ++i) {
            assertEquals("index: " + i, BYTES[i], actual[i]);
        }

        for (int i = 0; i < 97; ++i) {
            assertEquals("index: " + i, BYTES[i], (byte) xis.read());
        }

        xis.read(actual, 0, actual.length);
        assertArrayEquals(BYTES, actual);
    }

    private static void testFile(int preset, boolean delta) throws Exception {
        final URL testFile = XZStreamTest.class.getClassLoader().getResource("image1.dcm");

        FilterOptions[] options = delta ? new FilterOptions[] { new DeltaOptions(2), new LZMA2Options(preset) }
                : new FilterOptions[] { new LZMA2Options(preset) };

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(15 * 1024 * 1024);
                final XZOutputStream xos = new XZOutputStream(baos, options)) {
            final byte[] buffer = new byte[8 * 1024];
            final byte[] compressedBytes;
            try (final InputStream is = testFile.openStream()) {
                int read;
                while ((read = is.read(buffer)) != -1) {
                    xos.write(buffer, 0, read);
                }
                xos.finish();

                compressedBytes = baos.toByteArray();

                baos.close();
                xos.close();
            }

            try (final ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
                    final XZInputStream xis = new XZInputStream(bais);
                    final InputStream is = testFile.openStream()) {
                int index = 0;
                int read;
                final byte[] buffer2 = new byte[buffer.length];
                while ((read = is.read(buffer)) != -1) {
                    int read2 = xis.read(buffer2);
                    assertEquals("read at: " + index, read, read2);
                    int i = 0;
                    for (; i < read - 7; i += 8) {
                        assertEquals("byte at: " + index, buffer[i], buffer2[i]);
                        assertEquals("byte at: " + (index + 1), buffer[i + 1], buffer2[i + 1]);
                        assertEquals("byte at: " + (index + 2), buffer[i + 2], buffer2[i + 2]);
                        assertEquals("byte at: " + (index + 3), buffer[i + 3], buffer2[i + 3]);
                        assertEquals("byte at: " + (index + 4), buffer[i + 4], buffer2[i + 4]);
                        assertEquals("byte at: " + (index + 5), buffer[i + 5], buffer2[i + 5]);
                        assertEquals("byte at: " + (index + 6), buffer[i + 6], buffer2[i + 6]);
                        assertEquals("byte at: " + (index + 7), buffer[i + 7], buffer2[i + 7]);

                        index += 8;
                    }
                    for (; i < read; ++i) {
                        assertEquals("byte at: " + index++, buffer[i], buffer2[i]);
                    }
                }
            }
        }
    }
}
