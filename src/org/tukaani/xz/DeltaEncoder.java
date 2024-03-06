// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

class DeltaEncoder extends DeltaCoder implements FilterEncoder {
    private final DeltaOptions options;
    private final byte[] props = new byte[1];

    DeltaEncoder(DeltaOptions options) {
        props[0] = (byte)(options.getDistance() - 1);
        this.options = (DeltaOptions)options.clone();
    }

    @Override
    public long getFilterID() {
        return FILTER_ID;
    }

    @Override
    public byte[] getFilterProps() {
        return props;
    }

    @Override
    public boolean supportsFlushing() {
        return true;
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return options.getOutputStream(out, arrayCache);
    }
}
