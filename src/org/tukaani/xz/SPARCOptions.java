// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.simple.SPARC;

/**
 * BCJ filter for SPARC.
 */
public final class SPARCOptions extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public SPARCOptions() {
        super(ALIGNMENT);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new SPARC(true, startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new SPARC(false, startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.SPARC_FILTER_ID);
    }
}
