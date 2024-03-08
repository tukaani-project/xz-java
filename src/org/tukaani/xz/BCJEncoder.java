// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

class BCJEncoder extends BCJCoder implements FilterEncoder {
    private final BCJOptions options;
    private final long filterID;
    private final byte[] props;

    BCJEncoder(BCJOptions options, long filterID) {
        assert isBCJFilterID(filterID);
        int startOffset = options.getStartOffset();

        if (startOffset == 0) {
            props = new byte[0];
        } else {
            props = new byte[4];
            for (int i = 0; i < 4; ++i)
                props[i] = (byte)(startOffset >>> (i * 8));
        }

        this.filterID = filterID;
        this.options = (BCJOptions)options.clone();
    }

    @Override
    public long getFilterID() {
        return filterID;
    }

    @Override
    public byte[] getFilterProps() {
        return props;
    }

    @Override
    public boolean supportsFlushing() {
        return false;
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return options.getOutputStream(out, arrayCache);
    }
}
