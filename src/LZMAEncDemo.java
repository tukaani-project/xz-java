// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

import java.io.*;
import org.tukaani.xz.*;

/**
 * Compresses a single file from standard input to standard output into
 * the .lzma file format.
 * <p>
 * NOTE: For most purposes, .lzma is a legacy format and usually you should
 * use .xz instead.
 * <p>
 * Two optional arguments are supported:
 * <ol>
 *   <li>LZMA preset level which is an integer in the range [0, 9].
 *       The default is 6.</li>
 *   <li>Uncompressed size of the input as bytes.<li>
 * </ol>
 */
class LZMAEncDemo {
    public static void main(String[] args) throws Exception {
        LZMA2Options options = new LZMA2Options();
        long inputSize = -1;

        if (args.length >= 1)
            options.setPreset(Integer.parseInt(args[0]));

        if (args.length >= 2)
            inputSize = Long.parseLong(args[1]);

        System.err.println("Encoder memory usage: "
                           + options.getEncoderMemoryUsage() + " KiB");
        System.err.println("Decoder memory usage: "
                           + options.getDecoderMemoryUsage() + " KiB");

        // LZMAOutputStream writes one byte at a time. It helps a little,
        // especially in the fastest presets, to use BufferedOutputStream.
        OutputStream out = new BufferedOutputStream(System.out);
        LZMAOutputStream encoder = new LZMAOutputStream(out, options,
                                                        inputSize);

        byte[] buf = new byte[8192];
        int size;
        while ((size = System.in.read(buf)) != -1)
            encoder.write(buf, 0, size);

        encoder.finish();
        out.flush();
    }
}
