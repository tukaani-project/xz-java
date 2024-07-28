
XZ for Java
===========

Introduction
------------

  [XZ for Java](<https://tukaani.org/xz/java.html>) aims to be
  a complete implementation of XZ data compression in pure Java.

  Features:
   * Full support for [the .xz file format specification](
     <https://tukaani.org/xz/format.html>) version 1.2.1
   * Single-threaded streamed compression and decompression
   * Single-threaded decompression with limited random access support
   * Raw streams (no .xz headers) for advanced users, including LZMA2
     with preset dictionary

  Threading is planned but it is unknown when it will be implemented.

  The main source code is compatible with Java 8 and later but there
  are classes that are for Java 9 or later (module-info.java and
  speed optimizations). The default build options require OpenJDK 11
  or later, and create Java 8 compatible binaries.

Building with Apache Ant
------------------------

  Type `ant` to compile the classes and create the .jar files.
  Type `ant doc` to build the javadoc HTML documentation.
  Type `ant -projecthelp` to see all available targets.

  Notes about old build environments:

  * If you are using Ant older than 1.9.8:

    Edit `build.xml` and remove the release attributes from the
    `<javac>` tags, that is, remove all occurrences of these two lines:

        release="${sourcever}"

        release="${sourcever9}"

    The downside of the above is that then the `-source` and `-target`
    options will be used instead of `--release`.

  * If you are using OpenJDK version older than 9:

    Comment the `sourcever9` line in the file `build.properties`.
    When it is commented, `module-info.java` and other files
    requiring Java 9 won't be built. `xz.jar` won't be a modular JAR.

  * If you are using OpenJDK version older than 8:

    These versions are no longer supported. Try XZ for Java 1.9
    which is Java 5 compatible and only requires editing
    `build.properties` to build.

Building without Apache Ant
---------------------------

  If you cannot or don't want to use Ant, just compile all .java files
  under the `src` directory (possibly skip the demo files `src/*.java`).
  For module support and speed optimizations (Java >= 9), compile also
  all .java files under the `src9` directory.

Demo programs
-------------

  You can test compression with XZEncDemo, which compresses from
  standard input to standard output:

    java -jar build/jar/XZEncDemo.jar < foo.txt > foo.txt.xz

  You can test decompression with XZDecDemo, which decompresses to
  standard output:

    java -jar build/jar/XZDecDemo.jar foo.txt.xz

Contact information
-------------------

  * Home page: <https://tukaani.org/xz/java.html>
  * Email: Lasse Collin <lasse.collin@tukaani.org>
  * IRC: Larhzu on #tukaani on Libera Chat
  * GitHub: <https://github.com/tukaani-project/xz-java>

