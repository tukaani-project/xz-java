// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Jia Tan <jiat0218@gmail.com>

package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.simple.ARM64;

/**
 * BCJ filter for ARM64 instructions.
 */
public class ARM64Options extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public ARM64Options() {
        super(ALIGNMENT);
    }

    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new ARM64(true, startOffset));
    }

    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new ARM64(false, startOffset));
    }

    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.ARM64_FILTER_ID);
    }
}
