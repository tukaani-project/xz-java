/*
 * CRC64Test
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.check;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class CRC64Test {
    private static final byte[] DATA = new byte[256];

    private static final byte[] EXPECTED = new byte[] { -80, 58, -37, 101, 47, 75, 65, 114 };

    static {
	for (int i = 0; i < DATA.length; ++i) {
	    DATA[i] = (byte) i;
	}
    }

    @Test
    public void test() {
	CRC64 check = new CRC64();
	check.update(DATA);
	byte[] actual = check.finish();
	assertArrayEquals(EXPECTED, actual);
    }

    @Test
    public void test_1() {
	CRC64 check = new CRC64();
	for (int i = 0; i < DATA.length; ++i) {
	    check.update(DATA, i, 1);
	}
	byte[] actual = check.finish();
	assertArrayEquals(EXPECTED, actual);
    }

    @Test
    public void test_7() {
	CRC64 check = new CRC64();
	int i = 0;
	for (int j = DATA.length - 6; i < j; i += 7) {
	    check.update(DATA, i, 7);
	}
	check.update(DATA, i, DATA.length - i);
	byte[] actual = check.finish();
	assertArrayEquals(EXPECTED, actual);
    }

    @Test
    public void test_17() {
	CRC64 check = new CRC64();
	int i = 0;
	for (int j = DATA.length - 16; i < j; i += 17) {
	    check.update(DATA, i, 17);
	}
	check.update(DATA, i, DATA.length - i);
	byte[] actual = check.finish();
	assertArrayEquals(EXPECTED, actual);
    }
}
