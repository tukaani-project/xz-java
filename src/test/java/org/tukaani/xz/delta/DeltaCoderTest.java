/*
 * DeltaCoderTest
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.delta;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DeltaCoderTest {

    private static final byte[] DATA;

    static {
        DATA = new byte[17491];
        for (int i = 0; i < DATA.length; ++i) {
            DATA[i] = (byte) i;
        }
    }

    @Parameter
    public int delta;

    @Parameters(name = "delta={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][] { { 1 }, { 3 }, { 18 }, { 123 }, { 197 }, { 255 } });
    }

    @Test
    public void test() {
        byte[] buffer = new byte[1473];

        final Random random = new Random(0);

        final ByteArrayOutputStream encodedStream = new ByteArrayOutputStream(DATA.length);

        final DeltaEncoder encoder = new DeltaEncoder(delta);
        int index = 0;
        do {
            int remaining = DATA.length - index;
            int length = remaining > buffer.length ? random.nextInt(buffer.length - 1) + 1 : remaining;
            encoder.encode(DATA, index, length, buffer);
            encodedStream.write(buffer, 0, length);
            index += length;
        } while (index < DATA.length);

        final byte[] encodedData = encodedStream.toByteArray();

        index = 0;
        for (; index < delta; ++index) {
            assertEquals("encoded at: " + index, DATA[index], encodedData[index]);
        }

        for (; index < encodedData.length; ++index) {
            assertEquals("encoded at: " + index, delta, encodedData[index] & 0xFF);
        }

        final ByteArrayInputStream decoderStream = new ByteArrayInputStream(encodedData);
        final DeltaDecoder decoder = new DeltaDecoder(delta);

        index = 0;
        do {
            int length = Math.min(random.nextInt(buffer.length - 1), DATA.length - index - 1) + 1;

            decoderStream.read(buffer, 0, length);
            decoder.decode(buffer, 0, length);

            assertArraysEquals("decoded at index " + index, DATA, index, buffer, 0, length);

            index += length;
        } while (index < DATA.length);
    }

    private static void assertArraysEquals(String message, byte[] expected, int expOffset, byte[] actual, int actOffset,
            int length) {
        for (int i = 0; i < length; ++i) {
            assertEquals(message + " at offset " + i, expected[expOffset + i], actual[actOffset + i]);
        }
    }
}
