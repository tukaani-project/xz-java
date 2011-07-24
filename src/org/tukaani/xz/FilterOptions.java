/*
 * FilterOptions
 *
 * Authors: Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Base class for filter-specific options classes.
 */
public abstract class FilterOptions implements Cloneable {
    /**
     * Gets how much memory the encoder will need with these options.
     */
    public abstract int getEncoderMemoryUsage();

    /**
     * Gets a raw encoder output stream using these options.
     */
    public abstract FinishableOutputStream getOutputStream(
            FinishableOutputStream out);

    /**
     * Gets how much memory the decoder will need to decompress the data
     * that was encoded with these options.
     */
    public abstract int getDecoderMemoryUsage();

    /**
     * Gets a raw decoder input stream using these options.
     */
    public abstract InputStream getInputStream(InputStream in)
            throws IOException;

    abstract FilterEncoder getFilterEncoder();

    FilterOptions() {}
}
