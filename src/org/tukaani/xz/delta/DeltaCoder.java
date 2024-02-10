/*
 * DeltaCoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.delta;

abstract class DeltaCoder {
    static final int DISTANCE_MIN = 1;
    static final int DISTANCE_MAX = 256;

    final int distance;
    final byte[] history;

    DeltaCoder(int distance) {
        if (distance < DISTANCE_MIN || distance > DISTANCE_MAX)
            throw new IllegalArgumentException("invalid distance: " + 
                    distance);

        this.distance = distance;
        this.history = new byte[distance];
    }
}
