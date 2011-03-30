/*
 * CorruptedInputException
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

/**
 * Compressed input data is corrupt.
 */
public class CorruptedInputException extends java.io.IOException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new CorruptedInputException with
     * the default error detail message.
     */
    public CorruptedInputException() {
        super("Compressed data is corrupt");
    }

    /**
     * Creates a new CorruptedInputException with
     * the specified error detail message.
     *
     * @param       s           error detail message
     */
    public CorruptedInputException(String s) {
        super(s);
    }
}
