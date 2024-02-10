/*
 * SHA256Test
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.check;

import static org.junit.Assert.assertArrayEquals;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

public class SHA256Test {
    private static final byte[] DATA = new byte[256];

    private static final byte[] EXPECTED = new byte[] { 64, -81, -14, -23, -46, -40, -110, 46, 71, -81, -44, 100, -114,
            105, 103, 73, 113, 88, 120, 95, -67, 29, -88, 112, -25, 17, 2, 102, -65, -108, 72, -128 };

    static {
        for (int i = 0; i < DATA.length; ++i) {
            DATA[i] = (byte) i;
        }
    }

    @Test
    public void test() throws NoSuchAlgorithmException {
        SHA256 check = new SHA256();
        check.update(DATA);
        byte[] actual = check.finish();
        assertArrayEquals(EXPECTED, actual);
    }

    @Test
    public void test_1() throws NoSuchAlgorithmException {
        SHA256 check = new SHA256();
        for (int i = 0; i < DATA.length; ++i) {
            check.update(DATA, i, 1);
        }
        byte[] actual = check.finish();
        assertArrayEquals(EXPECTED, actual);
    }

    @Test
    public void test_7() throws NoSuchAlgorithmException {
        SHA256 check = new SHA256();
        int i = 0;
        for (int j = DATA.length - 6; i < j; i += 7) {
            check.update(DATA, i, 7);
        }
        check.update(DATA, i, DATA.length - i);
        byte[] actual = check.finish();
        assertArrayEquals(EXPECTED, actual);
    }

    @Test
    public void test_17() throws NoSuchAlgorithmException {
        SHA256 check = new SHA256();
        int i = 0;
        for (int j = DATA.length - 16; i < j; i += 17) {
            check.update(DATA, i, 17);
        }
        check.update(DATA, i, DATA.length - i);
        byte[] actual = check.finish();
        assertArrayEquals(EXPECTED, actual);
    }
}
