// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.check;

public class None extends Check {
    public None() {
        size = 0;
        name = "None";
    }

    public void update(byte[] buf, int off, int len) {}

    public byte[] finish() {
        byte[] empty = new byte[0];
        return empty;
    }
}
