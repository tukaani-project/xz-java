/*
 * LegacyArrayMismatch
 *
 * Author: Brett Okken <brett.okken.os@gmail.com>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.common;

/**
 * Default implementation of {@code ArrayMismatch} which performs byte-by-byte
 * comparisons.
 * 
 * @author Brett Okken
 */
final class LegacyArrayMismatch implements ArrayMismatch {

    /**
     * {@inheritDoc}
     */
    @Override
    public int mismatch(byte[] bytes, int aFromIndex, int bFromIndex, int length) {
        int i = 0;
        for (int j = length - 7; i < j; i += 8) {
            if (bytes[aFromIndex + i] != bytes[bFromIndex + i])
                return i;
            if (bytes[aFromIndex + i + 1] != bytes[bFromIndex + i + 1])
                return i + 1;
            if (bytes[aFromIndex + i + 2] != bytes[bFromIndex + i + 2])
                return i + 2;
            if (bytes[aFromIndex + i + 3] != bytes[bFromIndex + i + 3])
                return i + 3;
            if (bytes[aFromIndex + i + 4] != bytes[bFromIndex + i + 4])
                return i + 4;
            if (bytes[aFromIndex + i + 5] != bytes[bFromIndex + i + 5])
                return i + 5;
            if (bytes[aFromIndex + i + 6] != bytes[bFromIndex + i + 6])
                return i + 6;
            if (bytes[aFromIndex + i + 7] != bytes[bFromIndex + i + 7])
                return i + 7;
        }

        int result = length;
        switch (length & 7) {
        case 7:
            if (bytes[aFromIndex + i + 6] != bytes[bFromIndex + i + 6])
                result = i + 6;
        case 6:
            if (bytes[aFromIndex + i + 5] != bytes[bFromIndex + i + 5])
                result = i + 5;
        case 5:
            if (bytes[aFromIndex + i + 4] != bytes[bFromIndex + i + 4])
                result = i + 4;
        case 4:
            if (bytes[aFromIndex + i + 3] != bytes[bFromIndex + i + 3])
                result = i + 3;
        case 3:
            if (bytes[aFromIndex + i + 2] != bytes[bFromIndex + i + 2])
                result = i + 2;
        case 2:
            if (bytes[aFromIndex + i + 1] != bytes[bFromIndex + i + 1])
                result = i + 1;
        case 1:
            if (bytes[aFromIndex + i] != bytes[bFromIndex + i])
                result = i;
        }
        return result;
    }
}