// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>
// SPDX-FileContributor: Igor Pavlov <https://7-zip.org/>

package org.tukaani.xz.rangecoder;

import java.io.OutputStream;
import java.io.IOException;

public final class RangeEncoderToStream extends RangeEncoder {
    private final OutputStream out;

    public RangeEncoderToStream(OutputStream out) {
        this.out = out;
        reset();
    }

    @Override
    void writeByte(int b) throws IOException {
        out.write(b);
    }
}
