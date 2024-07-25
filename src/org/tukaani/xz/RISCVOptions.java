// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.simple.RISCVEncoder;
import org.tukaani.xz.simple.RISCVDecoder;

/**
 * BCJ filter for RISC-V instructions.
 *
 * @since 1.10
 */
public final class RISCVOptions extends BCJOptions {
    private static final int ALIGNMENT = 2;

    public RISCVOptions() {
        super(ALIGNMENT);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new RISCVEncoder(startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new RISCVDecoder(startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.RISCV_FILTER_ID);
    }
}
