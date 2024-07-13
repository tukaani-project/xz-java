// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Output stream that supports finishing without closing
 * the underlying stream.
 */
public abstract class FinishableOutputStream extends OutputStream {
    /**
     * Finish the stream without closing the underlying stream.
     * No more data may be written to the stream after finishing.
     * <p>
     * The {@code finish} method of {@code FinishableOutputStream} does
     * nothing. Subclasses should override it if they need finishing
     * support, which is the case, for example, with compressors.
     *
     * @throws      IOException
     */
    public void finish() throws IOException {}
}
