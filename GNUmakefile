#
# GNUmakefile
#
# Author: Lasse Collin <lasse.collin@tukaani.org>
#
# This file has been put into the public domain.
# You can do whatever you want with this file.
#

xz_SOURCES = \
	src/org/tukaani/xz/BlockInputStream.java \
	src/org/tukaani/xz/BlockOutputStream.java \
	src/org/tukaani/xz/CorruptedInputException.java \
	src/org/tukaani/xz/CountingInputStream.java \
	src/org/tukaani/xz/CountingOutputStream.java \
	src/org/tukaani/xz/DeltaCoder.java \
	src/org/tukaani/xz/DeltaDecoder.java \
	src/org/tukaani/xz/DeltaInputStream.java \
	src/org/tukaani/xz/FilterCoder.java \
	src/org/tukaani/xz/FilterDecoder.java \
	src/org/tukaani/xz/FilterEncoder.java \
	src/org/tukaani/xz/FilterOptions.java \
	src/org/tukaani/xz/FinishableOutputStream.java \
	src/org/tukaani/xz/IndexIndicatorException.java \
	src/org/tukaani/xz/LZMA2Coder.java \
	src/org/tukaani/xz/LZMA2Decoder.java \
	src/org/tukaani/xz/LZMA2Encoder.java \
	src/org/tukaani/xz/LZMA2InputStream.java \
	src/org/tukaani/xz/LZMA2Options.java \
	src/org/tukaani/xz/LZMA2OutputStream.java \
	src/org/tukaani/xz/MemoryLimitException.java \
	src/org/tukaani/xz/RawCoder.java \
	src/org/tukaani/xz/SingleXZInputStream.java \
	src/org/tukaani/xz/UncompressedLZMA2OutputStream.java \
	src/org/tukaani/xz/UnsupportedOptionsException.java \
	src/org/tukaani/xz/XZ.java \
	src/org/tukaani/xz/XZFormatException.java \
	src/org/tukaani/xz/XZIOException.java \
	src/org/tukaani/xz/XZInputStream.java \
	src/org/tukaani/xz/XZOutputStream.java \
	src/org/tukaani/xz/check/CRC32.java \
	src/org/tukaani/xz/check/CRC64.java \
	src/org/tukaani/xz/check/Check.java \
	src/org/tukaani/xz/check/None.java \
	src/org/tukaani/xz/check/SHA256.java \
	src/org/tukaani/xz/common/DecoderUtil.java \
	src/org/tukaani/xz/common/EncoderUtil.java \
	src/org/tukaani/xz/common/StreamFlags.java \
	src/org/tukaani/xz/common/Util.java \
	src/org/tukaani/xz/delta/DeltaCoder.java \
	src/org/tukaani/xz/delta/DeltaDecoder.java \
	src/org/tukaani/xz/index/IndexBase.java \
	src/org/tukaani/xz/index/IndexEncoder.java \
	src/org/tukaani/xz/index/IndexHash.java \
	src/org/tukaani/xz/index/IndexRecord.java \
	src/org/tukaani/xz/lz/BT4.java \
	src/org/tukaani/xz/lz/CRC32Hash.java \
	src/org/tukaani/xz/lz/HC4.java \
	src/org/tukaani/xz/lz/Hash234.java \
	src/org/tukaani/xz/lz/LZDecoder.java \
	src/org/tukaani/xz/lz/LZEncoder.java \
	src/org/tukaani/xz/lz/Matches.java \
	src/org/tukaani/xz/lzma/LZMACoder.java \
	src/org/tukaani/xz/lzma/LZMADecoder.java \
	src/org/tukaani/xz/lzma/LZMAEncoder.java \
	src/org/tukaani/xz/lzma/LZMAEncoderFast.java \
	src/org/tukaani/xz/lzma/LZMAEncoderNormal.java \
	src/org/tukaani/xz/lzma/Optimum.java \
	src/org/tukaani/xz/lzma/State.java \
	src/org/tukaani/xz/package-info.java \
	src/org/tukaani/xz/rangecoder/RangeCoder.java \
	src/org/tukaani/xz/rangecoder/RangeDecoder.java \
	src/org/tukaani/xz/rangecoder/RangeEncoder.java
XZEncDemo_SOURCES = src/XZEncDemo.java
XZDecDemo_SOURCES = src/XZDecDemo.java
ALL_SOURCES = $(xz_SOURCES) $(XZEncDemo_SOURCES) $(XZDecDemo_SOURCES)

SOURCE_VERSION = 1.4
TARGET_VERSION = 1.6
DOC_URL = http://download.oracle.com/javase/6/docs/api

.PHONY: all class jar doc gcj clean distclean dist

all: class jar doc

class:
	mkdir -p class
	javac -source $(SOURCE_VERSION) -target $(TARGET_VERSION) \
		-sourcepath src -d class -Xlint $(ALL_SOURCES)

jar: class
	mkdir -p jar
	jar cf jar/xz.jar -C class org/tukaani/xz
	jar cfe jar/XZEncDemo.jar XZEncDemo \
		-C class XZEncDemo.class -C class org/tukaani/xz
	jar cfe jar/XZDecDemo.jar XZDecDemo \
		-C class XZDecDemo.class -C class org/tukaani/xz

# Download package-list once so that javadoc doesn't download it everytime.
extdoc:
	mkdir -p extdoc
	wget -nv -O extdoc/package-list "$(DOC_URL)/package-list" \
		|| { rm -rf extdoc; false; }

doc: extdoc
	mkdir -p doc
	javadoc -quiet -source $(SOURCE_VERSION) -sourcepath src -d doc \
		-linkoffline $(DOC_URL) extdoc org.tukaani.xz

gcj:
	mkdir -p bin
	gcj -Wall -O2 -I src $(xz_SOURCES) $(XZEncDemo_SOURCES) \
		--main=XZEncDemo -o bin/XZEncDemo
	gcj -Wall -O2 -I src $(xz_SOURCES) $(XZDecDemo_SOURCES) \
		--main=XZDecDemo -o bin/XZDecDemo

clean:
	rm -rf class jar bin doc xz-java.zip

distclean: clean
	rm -rf extdoc

dist:
	rm -f xz-java.zip
	zip -9 -X xz-java.zip README COPYING THANKS GNUmakefile $(ALL_SOURCES)
