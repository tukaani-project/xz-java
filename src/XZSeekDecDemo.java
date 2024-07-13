// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>

import java.io.*;
import org.tukaani.xz.*;

/**
 * Decompresses a .xz file in random access mode to standard output.
 * <p>
 * Arguments: filename [offset1 length1] [offset2 length2] ...
 * <p>
 * If only the filename is given, the whole file is decompressed. The only
 * difference to XZDecDemo is that this will still use the random access code.
 * <p>
 * If one or more of the offset-length pairs are given,
 * for each pair, {@code length} number of bytes are
 * decompressed from {@code offset}.
 */
class XZSeekDecDemo {
    public static void main(String[] args) throws Exception {
        SeekableFileInputStream file = new SeekableFileInputStream(args[0]);
        SeekableXZInputStream in = new SeekableXZInputStream(file);

        System.err.println("Number of XZ Streams: " + in.getStreamCount());
        System.err.println("Number of XZ Blocks: " + in.getBlockCount());

        System.err.println("Uncompressed size: " + in.length() + " B");

        System.err.println("Largest XZ Block size: "
                           + in.getLargestBlockSize() + " B");

        System.err.print("List of Check IDs:");
        int checkTypes = in.getCheckTypes();
        for (int i = 0; i < 16; ++i)
            if ((checkTypes & (1 << i)) != 0)
                System.err.print(" " + i);
        System.err.println();

        System.err.println("Index memory usage: "
                           + in.getIndexMemoryUsage() + " KiB");

        byte[] buf = new byte[8192];
        if (args.length == 1) {
            int size;
            while ((size = in.read(buf)) != -1)
                System.out.write(buf, 0, size);
        } else {
            for (int i = 1; i < args.length; i += 2) {
                long pos = Long.parseLong(args[i]);
                long len = Long.parseLong(args[i + 1]);

                in.seek(pos);

                while (len > 0) {
                    int size = (int)Math.min(len, buf.length);
                    size = in.read(buf, 0, size);

                    if (size == -1) {
                        System.err.println("Error: End of file reached");
                        System.exit(1);
                    }

                    System.out.write(buf, 0, size);
                    len -= size;
                }
            }
        }
    }
}
