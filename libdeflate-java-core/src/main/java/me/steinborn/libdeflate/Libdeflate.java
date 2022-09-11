package me.steinborn.libdeflate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class Libdeflate {
    private static final String OS_SYSTEM_PROPERTY = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final String OS;
    private static final String ARCH = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
    private static final String NATIVE_LIB_PATH = System.getProperty("libdeflate_jni_path", "");
    private static Throwable unavailabilityCause;

    static {
        if (OS_SYSTEM_PROPERTY.startsWith("mac")) {
            OS = "darwin";
        } else if (OS_SYSTEM_PROPERTY.startsWith("win")) {
            OS = "windows";
        } else {
            OS = OS_SYSTEM_PROPERTY;
        }

        String path = NATIVE_LIB_PATH.isEmpty() ? "/" + determineLoadPath() : NATIVE_LIB_PATH;

        try {
            copyAndLoadNative(path);
            // It is available
            unavailabilityCause = null;
        } catch (Throwable e) {
            unavailabilityCause = e;
        }
    }

    private static void copyAndLoadNative(String path) {
        try {
            InputStream nativeLib = Libdeflate.class.getResourceAsStream(path);
            if (nativeLib == null) {
                // in case the user is trying to load native library from an absolute path
                Path libPath = Paths.get(path);
                if (Files.exists(libPath) && Files.isRegularFile(libPath)) {
                    nativeLib = new FileInputStream(path);
                } else {
                    throw new IllegalStateException("Native library " + path + " not found.");
                }
            }

            Path tempFile = createTemporaryNativeFilename(path.substring(path.lastIndexOf('.')));
            Files.copy(nativeLib, tempFile, StandardCopyOption.REPLACE_EXISTING);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Well, it doesn't matter...
                }
            }));

            try {
                System.load(tempFile.toAbsolutePath().toString());
            } catch (UnsatisfiedLinkError e) {
                throw new RuntimeException("Unable to load native " + tempFile.toAbsolutePath(), e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy natives", e);
        }
    }

    private static Path createTemporaryNativeFilename(String ext) throws IOException {
        return Files.createTempFile("native-", ext);
    }

    private static String determineLoadPath() {
        return OS + "/" + ARCH + "/libdeflate_jni" + determineDylibSuffix();
    }

    private static String determineDylibSuffix() {
        if (OS.startsWith("darwin")) {
            return ".dylib";
        } else if (OS.startsWith("win")) {
            return ".dll";
        } else {
            return ".so";
        }
    }

    public static boolean isAvailable() {
        return unavailabilityCause == null;
    }

    public static Throwable unavailabilityCause() {
        return unavailabilityCause;
    }

    public static void ensureAvailable() {
        if (unavailabilityCause != null) {
            throw new RuntimeException("libdeflate JNI library unavailable", unavailabilityCause);
        }
    }
}
