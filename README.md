# libdeflate-java

This project provides safe, high-performance JNI bindings to the [libdeflate](https://github.com/ebiggers/libdeflate)
library. This project grew out of an effort to improve compression performance in the _Minecraft: Java Edition_
protocol for my Minecraft proxy [Velocity](https://github.com/VelocityPowered/Velocity), but I have endeavoured to
make this library as generally useful as possible by providing a faithful Java representation of the entire
libdeflate library.

## Depending on this library

**Note**: As of September 28, 2020, I have yet to publish the library... I am working on finishing basic functionality.

### Maven

```xml
<dependency>
    <groupId>me.steinborn</groupId>
    <artifactId>libdeflate-java-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```kotlin
dependencies {
    implementation("me.steinborn.libdeflate-java-core:0.1.0-SNAPSHOT")
}
```

## Why use libdeflate over `java.util.zip`?

There are very good reasons to consider libdeflate over the `java.util.zip` bindings to zlib:

* libdeflate has significantly improved performance over any zlib variant available, often 2x as
  fast as zlib.
* Prior to Java 11, there was no API for `ByteBuffer`. This library includes a `ByteBuffer` API.

There are, of course, downsides:

* libdeflate does not support a streaming API yet. Should a streaming API be added to `libdeflate` we will add support for it.
* libdeflate is only optimized for x86, x86_64 and aarch64. This should be sufficient for the vast majority of users, and there
  are generic routines in case your platform does not have an optimized routine.

## Compatibility

Currently these bindings are well-supported only on x86_64 Linux as that is my primary development platform.
I will add support for other platforms as time and demand permits. Contributions welcome!

### Supported platforms

* Linux x86_64
* Windows x86_64

### Planned support

* macOS x86_64
* macOS aarch64
* Linux aarch64

## Building

You will need:

* a basic C toolchain
* GNU make
* a JDK

Make sure `JAVA_HOME` is defined and build this project as usual for a Gradle project. I have looked at options
for building native modules with Gradle, and unfortunately dropping down to GNU Make has wound up being the most sane
solution.

## API usage

The API revolves around two classes, `me.steinborn.libdeflate.LibdeflateCompressor` and `me.steinborn.libdeflate.LibdeflateDecompressor`.
When instantiated, a native libdeflate peer object is created. Once you are done with the (de)compressor you are expected
to call `close()` on the object, which will invalidate the Java object and free the native libdeflate object.

All APIs support working on byte arrays (`byte[]`) with arbitrary position and lengths and `ByteBuffer` instances,
whether they are direct or heap buffers. You will most likely prefer to work with the `ByteBuffer` API as it is much
cleaner.

In addition, `me.steinborn.libdeflate.LibdeflateAdler32` provides a drop-in replacement for `java.util.zip.Adler32`
and `me.steinborn.libdeflate.LibdeflateCRC32` is a drop-in replacement for `java.util.zip.CRC32`. As with the
rest of the library, the Adler-32 and CRC32 implementations are well-optimized code that uses native hardware vector operations.