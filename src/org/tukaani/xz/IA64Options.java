// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.simple.IA64;

/**
 * BCJ filter for Itanium (IA-64) instructions.
 */
public final class IA64Options extends BCJOptions {
    private static final int ALIGNMENT = 16;

    public IA64Options() {
        super(ALIGNMENT);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new IA64(true, startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new IA64(false, startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.IA64_FILTER_ID);
    }
}
