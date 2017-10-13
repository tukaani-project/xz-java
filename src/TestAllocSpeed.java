/*
 * TestAllocSpeed
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

/*
 * Usage:
 *   time java -jar build/jar/TestAllocSpeed.jar MODE ITERS THREADS < FILE
 * where
 *   MODE is "true" for compression or "false" for decompression,
 *   ITERS is the number of iterations to done by each thread,
 *   THREADS is the number of threads, and
 *   FILE is the input file (preferably tiny, but at most 1 MiB).
 *
 * Each thread has a different random seed so in compression mode each
 * thread will use different options in different order. This way the
 * ArrayCache gets more diverse load.
 *
 * Examples:
 *   time java -jar build/jar/TestAllocSpeed.jar true 1000 4 < README
 *   time java -jar build/jar/TestAllocSpeed.jar false 10000 4 < foo.xz
 */

import java.io.*;
import java.util.Random;
import org.tukaani.xz.*;

class TestAllocSpeed implements Runnable {
    private static boolean compressing;
    private static int repeats;
    private static final byte[] testdata = new byte[1 << 20];
    private static int testdataSize;
    private static volatile IOException exception = null;

    private final Random rng;

    public TestAllocSpeed(long seed) {
        rng = new Random(seed);
    }

    private void compress() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(
                testdataSize + 1024);
        LZMA2Options options = new LZMA2Options();
        options.setDictSize(1 << (16 + rng.nextInt(6)));

        for (int i = 0; i < repeats; ++i) {
            XZOutputStream out = new XZOutputStream(byteStream, options);
            out.write(testdata, 0, testdataSize);
            out.finish();
        }
    }

    private void decompress() throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(
                testdata, 0, testdataSize);
        byte[] outbuf = new byte[8192];

        for (int i = 0; i < repeats; ++i) {
            byteStream.reset();
            XZInputStream in = new XZInputStream(byteStream);
            while (in.read(outbuf) > 0) {}
        }
    }

    public void run() {
        try {
            if (compressing) {
                compress();
            } else {
                decompress();
            }
        } catch (IOException e) {
            exception = e;
        }
    }

    public static void main(String[] args) throws Exception {
        compressing = Boolean.parseBoolean(args[0]);
        repeats = Integer.parseInt(args[1]);
        final int threadCount = Integer.parseInt(args[2]);

        if (threadCount < 1 || threadCount > 64)
            throw new Exception("Thread count must be 1-64");

        testdataSize = System.in.read(testdata);

        ArrayCache.setDefaultCache(BasicArrayCache.getInstance());

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; ++i) {
            threads[i] = new Thread(new TestAllocSpeed(i));
            threads[i].start();
        }

        for (int i = 0; i < threadCount; ++i)
            threads[i].join();

        if (exception != null)
            throw exception;
    }
}
