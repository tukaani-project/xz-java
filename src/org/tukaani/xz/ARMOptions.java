// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.simple.ARM;

/**
 * BCJ filter for little endian ARM instructions.
 */
public final class ARMOptions extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public ARMOptions() {
        super(ALIGNMENT);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new ARM(true, startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new ARM(false, startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.ARM_FILTER_ID);
    }
}
