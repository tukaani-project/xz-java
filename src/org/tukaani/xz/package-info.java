// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

/**
 * XZ data compression support.
 *
 * <h2>Introduction</h2>
 * <p>
 * This aims to be a complete implementation of XZ data compression
 * in pure Java. Features:
 * <ul>
 * <li>Full support for the .xz file format specification version 1.2.0</li>
 * <li>Single-threaded streamed compression and decompression</li>
 * <li>Single-threaded decompression with limited random access support</li>
 * <li>Raw streams (no .xz headers) for advanced users, including LZMA2
 *     with preset dictionary</li>
 * </ul>
 * <p>
 * Threading is planned but it is unknown when it will be implemented.
 * <p>
 * For the latest source code, see the
 * <a href="https://tukaani.org/xz/java.html">home page of XZ for Java</a>.
 *
 * <h2>Getting started</h2>
 * <p>
 * Start by reading the documentation of {@link org.tukaani.xz.XZOutputStream}
 * and {@link org.tukaani.xz.XZInputStream}.
 * If you use XZ inside another file format or protocol,
 * see also {@link org.tukaani.xz.SingleXZInputStream}.
 *
 * <h2>Authors</h2>
 * XZ for Java is developed and maintained by Lasse Collin.
 * <p>
 * Major parts of XZ for Java are based on code written by
 * Igor Pavlov in the <a href="https://7-zip.org/sdk.html">LZMA SDK</a>.
 * <p>
 * Other authors:
 * <ul>
 * <li>Brett Okken
 * </ul>
 * <p>
 * Many people have contributed improvements or reported bugs.
 * Most of these people are mentioned in the file THANKS.md
 * in the source package.
 *
 * <h2>License</h2>
 * <p>
 * Copyright &copy; The XZ for Java authors and contributors
 * <p>
 * Permission to use, copy, modify, and/or distribute this
 * software for any purpose with or without fee is hereby granted.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.tukaani.xz;
