/**
 * XZ data compression support.
 *
 * <h4>Introduction</h4>
 * <p>
 * This aims to be a complete implementation of XZ data compression
 * in pure Java. Single-threaded streamed compression and decompression
 * have been fully implemented.
 * Planned features include threaded compression, threaded decompression,
 * and random access decompression. It is unknown when these will be
 * implemented.
 * <p>
 * For the latest source code, see the
 * <a href="http://tukaani.org/xz/java.html">home page of XZ in Java</a>.
 *
 * <h4>Getting started</h4>
 * <p>
 * Start by reading the documentation of {@link org.tukaani.xz.XZOutputStream}
 * and {@link org.tukaani.xz.XZInputStream}.
 * If you use XZ inside another file format or protocol,
 * see also {@link org.tukaani.xz.SingleXZInputStream}.
 *
 * <h4>Licensing</h4>
 * This Java implementation of XZ has been put into the public domain,
 * thus you can do whatever you want with it. All the files in the package
 * have been written by Lasse Collin, but some files are heavily based on
 * public domain code written by Igor Pavlov.
 */
package org.tukaani.xz;
