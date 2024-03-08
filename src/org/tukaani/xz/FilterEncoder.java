// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

package org.tukaani.xz;

interface FilterEncoder extends FilterCoder {
    long getFilterID();
    byte[] getFilterProps();
    boolean supportsFlushing();
    FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                           ArrayCache arrayCache);
}
