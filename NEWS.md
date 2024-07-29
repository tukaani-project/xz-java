
XZ for Java release notes
=========================

1.10 (2024-07-29)
-----------------

  * Licensing change: From version 1.10 onwards, XZ for Java is under
    the BSD Zero Clause License (0BSD). 1.9 and older are in the
    public domain and obviously remain so; the change only affects
    the new releases.

    0BSD is an extremely permissive license which doesn't require
    retaining or reproducing copyright or license notices when
    distributing the code, thus in practice there is extremely
    little difference to public domain.

  * Mark copyright and license information in the source package so
    that it is compliant to the [REUSE Specification version 3.2](
    <https://reuse.software/spec-3.2/>).

  * Improve LZMAInputStream.enableRelaxedEndCondition():

      - Error detection is slightly better.

      - The input position will always be at the end of the stream
        after successful decompression.

  * Support .lzma files that have both a known uncompressed size and
    the end marker. Such files are uncommon but valid. The same issue
    was fixed in XZ Utils 5.2.6 in 2022.

  * Add ARM64 and RISC-V BCJ filters.

  * Speed optimizations:
      - Delta filter
      - LZMA/LZMA2 decoder
      - LZMA/LZMA2 encoder (partially Java >= 9 only)
      - CRC64 (Java >= 9 only)

  * Changes that affect API/ABI compatibility:

      - Change XZOutputStream constructors to not call the method
        `public void updateFilters(FilterOptions[] filterOptions)`.

      - In SeekableXZInputStream, change the method
        `public void seekToBlock(int blockNumber)` to not call
        the method `public long getBlockPos(int blockNumber)`.

      - Make the filter options classes `final`:
          * ARM64Options
          * ARMOptions
          * ARMThumbOptions
          * DeltaOptions
          * IA64Options
          * LZMA2Options
          * PowerPCOptions
          * RISCVOptions
          * SPARCOptions
          * X86Options

  * Add new system properties:

      - `org.tukaani.xz.ArrayCache` sets the default ArrayCache:
        `Dummy` (default) or `Basic`. See the documentation of
        ArrayCache and BasicArrayCache.

      - `org.tukaani.xz.MatchLengthFinder` (Java >= 9 only) sets the
        byte array comparison method used for finding match lengths in
        LZMA/LZMA2 encoder: `UnalignedLongLE` (default on x86-64 and
        ARM64) or `Basic` (default on other systems). The former could
        be worth testing on other 64-bit little endian systems that
        support fast unaligned memory access.

  * Build system (Apache Ant):

      - Building the documentation no longer downloads `element-list`
        or `package-list` file; the build is now fully offline. Such
        files aren't needed with OpenJDK >= 16 whose `javadoc` can
        auto-link to platform documentation on docs.oracle.com. With
        older OpenJDK versions, links to platform documentation aren't
        generated anymore.

      - Don't require editing of build.properties to build with
        OpenJDK 8. Now it's enough to use `ant -Djava8only=true`.
        Older OpenJDK versions are no longer supported because
        the main source tree uses Java 8 features.

      - Support reproducible builds. See the notes in README.md.

      - Add a new Ant target `pom` that only creates xz.pom.

      - Change `ant dist` to use `git archive` to create a .zip file.

  * Convert the plain text documentation in the source tree to
    Markdown (CommonMark).

  * The binaries of 1.10 in the Maven Central require Java 8 and
    contain optimized classes for Java >= 9 as multi-release JAR.
    They were built with OpenJDK 21.0.4 on GNU/Linux using the
    following command:

        SOURCE_DATE_EPOCH=1722262226 TZ=UTC0 ant maven

1.9 (2021-03-12)
----------------

  * Add LZMAInputStream.enableRelaxedEndCondition(). It allows
    decompression of LZMA streams whose uncompressed size is known
    but it is unknown if the end of stream marker is present. This
    method is meant to be useful in Apache Commons Compress to
    support .7z files created by certain very old 7-Zip versions.
    Such files have the end of stream marker in the LZMA data even
    though the uncompressed size is known. 7-Zip supports such files
    and thus other implementations of the .7z format should support
    them too.

  * Make LZMA/LZMA2 decompression faster. With files that compress
    extremely well the performance can be a lot better but with
    more typical files the improvement is minor.

  * Make the CRC64 code faster.

  * Add module-info.java as multi-release JAR. The attribute
    `Automatic-Module-Name` was removed.

  * The binaries for XZ for Java 1.9 in the Maven Central now
    require Java 7. Building the package requires at least Java 9
    for module-info support but otherwise the code should still be
    Java 5 compatible (see README and comments in build.properties).

1.8 (2018-01-04)
----------------

  * Fix a binary compatibility regression: XZ for Java 1.7 binaries
    in the Maven Central require Java 9 which is too new. XZ for
    Java 1.8 binaries require Java 5. (XZ for Java 1.6 and older
    binaries require Java 1.4.)

    If you are using OpenJDK 9 or later, you will need to edit the
    `sourcever = 1.5` line in the file build.properties before
    running `ant`. Set it to `1.6` or higher. The default value `1.5`
    isn't supported by OpenJDK 9 or later.

  * Add `Automatic-Module-Name` = `org.tukaani.xz`.

1.7 (2017-12-29)
----------------

  * Fix LZMA2InputStream.available() which could return a too high
    value in case of uncompressed LZMA2 chunks. This incorrect
    value was visible via other available() methods too, for example,
    XZInputStream.available().

  * Add the ArrayCache API. It's a pool-like API to reuse large byte
    and int arrays between compressor and decompressor instances.
    If you are (de)compressing many tiny files in a row, taking
    advantage of this API can improve performance significantly.

1.6 (2016-11-27)
----------------

  * Fix LZMA2Options.getInputStream to work with a preset dictionary.

  * Make it possible to disable verification of integrity checks in
    XZ decompression. It should almost never be used but may be useful
    in some rare situations. This feature is available via new
    constructors in XZInputStream, SingleXZInputStream, and
    SeekableXZInputStream.

  * Add LZMAOutputStream for encoding to raw LZMA (i.e. LZMA1) streams
    and to the legacy .lzma format.

1.5 (2014-03-08)
----------------

  * Fix a wrong assertion in BCJ decoders.

  * Use a field instead of reallocating a temporary one-byte buffer
    in read() and write() implementations in several classes.

1.4 (2013-09-22)
----------------

  * Add LZMAInputStream for decoding .lzma files and raw LZMA streams.

1.3 (2013-05-12)
----------------

  * Fix a data corruption bug when flushing the LZMA2 encoder or
    when using a preset dictionary.

  * Make information about the XZ Block positions and sizes available
    in SeekableXZInputStream by adding the following public functions:
      - int getStreamCount()
      - int getBlockCount()
      - long getBlockPos(int blockNumber)
      - long getBlockSize(int blockNumber)
      - long getBlockCompPos(int blockNumber)
      - long getBlockCompSize(int blockNumber)
      - int getBlockCheckType(int blockNumber)
      - int getBlockNumber(long pos)
      - void seekToBlock(int blockNumber)

  * Minor improvements to javadoc comments were made.

1.2 (2013-01-29)
----------------

  * Use fields instead of reallocating frequently-needed temporary
    objects in the LZMA encoder.

  * Fix the contents of xz-${version}-sources.jar.

  * Add OSGi attributes to xz.jar.

1.1 (2012-07-05)
----------------

  * The depthLimit argument in the LZMA2Options constructor is
    no longer ignored.

  * LZMA2Options() can no longer throw UnsupportedOptionsException.

  * Fix bugs in the preset dictionary support in the LZMA2 encoder.

1.0 (2011-10-22)
----------------

  * The first stable release

