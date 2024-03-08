// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz.index;

class IndexRecord {
    final long unpadded;
    final long uncompressed;

    IndexRecord(long unpadded, long uncompressed) {
        this.unpadded = unpadded;
        this.uncompressed = uncompressed;
    }
}
