# XZ for Java

## Introduction

This aims to be a complete implementation of [XZ](https://github.com/tukaani-project/xz) data compression
in pure **Java**. Features:

- Full support for the .xz file format specification version 1.2.0
- Single-threaded streamed compression and decompression
- Single-threaded decompression with limited random access support
- Raw streams (no .xz headers) for advanced users, including LZMA2 with preset dictionary

Threading is planned but it is unknown when it will be implemented.

For the latest source code, see the project home page:

        https://tukaani.org/xz/java.html

The source code is compatible with Java 8 and later (except
`module-info.java` which is Java 9 or later). However, the default
build options require **OpenJDK 11** or later, and create Java 8
compatible binaries.

## Building with Apache Ant

- Type "ant" to compile the classes and create the .jar files.
- Type "ant doc" to build the javadoc HTML documentation. Note
that building the documentation will download a small file named
"element-list" or "package-list" from Oracle to enable linking to
the documentation of the standard Java classes.

### 1. If you are using Ant older than 1.9.8

Edit build.xml and remove the release attributes from <javac>
tags, that is, remove all occurrences of these two lines:

```dtd
release="${sourcever}"
release="${sourcever9}"
```

The downside of the above is that then `-source` and `-target`
options will be used instead of `--release`.

### 2. If you are using OpenJDK version older than 11

Adjust extdoc_url and extdoc_file to point to an older URL
and to use "package-list" instead of "element-list". This
modification isn't required if the documentation won't be
built.

### 3. If you are using OpenJDK version older than 9

Comment the sourcever9 line in the file build.properties.
When it is commented, `module-info.java` won't be built and
xz.jar won't be a modular JAR.

### 4. If you are using OpenJDK version older than 8

These versions are no longer supported. Try XZ for Java 1.9
which is Java 5 compatible and only requires editing
build.properties to build.

## Building without Apache Ant

If you cannot or don't want to use Ant, just compile all .java files
under the "src" directory (possibly skip the demo files src/*.java).
For module support (Java >= 9) compile also src9/module-info.java.

## Demo programs

You can test compression with XZEncDemo, which compresses from
standard input to standard output:

```shell
java -jar build/jar/XZEncDemo.jar < foo.txt > foo.txt.xz
```

You can test decompression with XZDecDemo, which decompresses to
standard output:

```shell
java -jar build/jar/XZDecDemo.jar foo.txt.xz
```        

## Contact information

- Email: Lasse Collin <lasse.collin@tukaani.org>
- IRC: Larhzu on #tukaani on Libera Chat
- GitHub: https://github.com/tukaani-project/xz-java