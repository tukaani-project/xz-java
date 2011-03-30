/*
 * XZDecDemo
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

import java.io.*;
import org.tukaani.xz.*;

/**
 * Decompresses .xz files to standard output. If no arguments are given,
 * reads from standard input.
 */
class XZDecDemo {
    public static void main(String[] args) {
        byte[] buf = new byte[8192];
        String name = null;

        try {
            if (args.length == 0) {
                name = "standard input";
                InputStream in = new XZInputStream(System.in);

                while (in.read(buf) != -1)
                    System.out.write(buf);

            } else {
                // Read from files given on the command line.
                for (int i = 0; i < args.length; ++i) {
                    name = args[i];

                    // Since XZInputStream does some buffering internally
                    // anyway, BufferedInputStream doesn't seem to be
                    // needed here to improve performance.
                    InputStream in = new FileInputStream(name);
                    // in = new BufferedInputStream(in);
                    in = new XZInputStream(in);

                    while (in.read(buf) != -1)
                        System.out.write(buf);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("XZDecDemo: Cannot open " + name + ": "
                               + e.getMessage());
            System.exit(1);

        } catch (EOFException e) {
            System.err.println("XZDecDemo: Unexpected end of input on "
                               + name);
            System.exit(1);

        } catch (IOException e) {
            System.err.println("XZDecDemo: Error decompressing from "
                               + name + ": " + e.getMessage());
            System.exit(1);
        }
    }
}
