// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Wraps a {@link java.io.RandomAccessFile RandomAccessFile}
 * in a SeekableInputStream.
 */
public class SeekableFileInputStream extends SeekableInputStream {
    /**
     * The RandomAccessFile that has been wrapped
     * into a SeekableFileInputStream.
     */
    protected RandomAccessFile randomAccessFile;

    /**
     * Creates a new seekable input stream that reads from the specified file.
     */
    public SeekableFileInputStream(File file) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(file, "r");
    }

    /**
     * Creates a new seekable input stream that reads from a file with
     * the specified name.
     */
    public SeekableFileInputStream(String name) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(name, "r");
    }

    /**
     * Creates a new seekable input stream from an existing
     * {@code RandomAccessFile} object.
     */
    public SeekableFileInputStream(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
    }

    /**
     * Calls {@link RandomAccessFile#read() randomAccessFile.read()}.
     */
    @Override
    public int read() throws IOException {
        return randomAccessFile.read();
    }

    /**
     * Calls {@link RandomAccessFile#read(byte[]) randomAccessFile.read(buf)}.
     */
    @Override
    public int read(byte[] buf) throws IOException {
        return randomAccessFile.read(buf);
    }

    /**
     * Calls
     * {@link RandomAccessFile#read(byte[],int,int)
     *        randomAccessFile.read(buf, off, len)}.
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return randomAccessFile.read(buf, off, len);
    }

    /**
     * Calls {@link RandomAccessFile#close() randomAccessFile.close()}.
     */
    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }

    /**
     * Calls {@link RandomAccessFile#length() randomAccessFile.length()}.
     */
    @Override
    public long length() throws IOException {
        return randomAccessFile.length();
    }

    /**
     * Calls {@link RandomAccessFile#getFilePointer()
                    randomAccessFile.getFilePointer()}.
     */
    @Override
    public long position() throws IOException {
        return randomAccessFile.getFilePointer();
    }

    /**
     * Calls {@link RandomAccessFile#seek(long) randomAccessFile.seek(long)}.
     */
    @Override
    public void seek(long pos) throws IOException {
        randomAccessFile.seek(pos);
    }
}
