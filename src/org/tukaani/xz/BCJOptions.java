// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

abstract class BCJOptions extends FilterOptions {
    private final int alignment;
    int startOffset = 0;

    BCJOptions(int alignment) {
        this.alignment = alignment;
    }

    /**
     * Sets the start offset for the address conversions.
     * Normally this is useless so you shouldn't use this function.
     * The default value is {@code 0}.
     */
    public void setStartOffset(int startOffset)
            throws UnsupportedOptionsException {
        if ((startOffset & (alignment - 1)) != 0)
            throw new UnsupportedOptionsException(
                    "Start offset must be a multiple of " + alignment);

        this.startOffset = startOffset;
    }

    /**
     * Gets the start offset.
     */
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public int getEncoderMemoryUsage() {
        return SimpleOutputStream.getMemoryUsage();
    }

    @Override
    public int getDecoderMemoryUsage() {
        return SimpleInputStream.getMemoryUsage();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            assert false;
            throw new RuntimeException();
        }
    }
}
