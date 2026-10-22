// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableXZInputStream;
import org.tukaani.xz.XZOutputStream;

public class SeekableXZInputStreamTest {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("seekable-xz-eof", ".xz");
        try {
            try (XZOutputStream out = new XZOutputStream(
                    Files.newOutputStream(file), new LZMA2Options(0))) {
                out.write(new byte[] { 1, 2, 3 });
                out.endBlock();
                out.write(new byte[] { 4, 5, 6 });
            }

            try (SeekableXZInputStream in = open(file)) {
                byte[] buf = new byte[6];
                check(6, in.read(buf));
                check(-1, in.read(buf));
                check(-1, in.read(buf));
                check(0, in.read(buf, 0, 0));
                in.seek(0);
                check(1, in.read());
            }

            try (SeekableXZInputStream in = open(file)) {
                for (int i = 1; i <= 6; ++i)
                    check(i, in.read());
                check(-1, in.read());
                check(-1, in.read());
            }

            try (SeekableXZInputStream in = open(file)) {
                check(6, in.read(new byte[10]));
                check(-1, in.read(new byte[10]));
            }

            try (XZOutputStream out = new XZOutputStream(
                    Files.newOutputStream(file), new LZMA2Options(0))) {
                // Write an empty stream.
            }
            try (SeekableXZInputStream in = open(file)) {
                check(-1, in.read());
                check(-1, in.read(new byte[1]));
            }
        } finally {
            Files.delete(file);
        }
    }

    private static SeekableXZInputStream open(Path file) throws IOException {
        return new SeekableXZInputStream(
                new SeekableFileInputStream(file.toFile()));
    }

    private static void check(int expected, int actual) {
        if (actual != expected)
            throw new AssertionError(
                    "Expected " + expected + ", got " + actual);
    }
}
