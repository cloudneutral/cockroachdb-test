package io.cockroachdb.test.util;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class OperatingSystem {
    public static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

    public static final String OS_ARCH = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);

    public static final String ARCH = System.getenv("PROCESSOR_ARCHITECTURE");

    public static final String ARCHW64 = System.getenv("PROCESSOR_ARCHITEW6432");

    private OperatingSystem() {
    }

    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }

    public static boolean isMac() {
        return (OS_NAME.contains("mac"));
    }

    public static boolean isUnix() {
        return (OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS_NAME.contains("sunos"));
    }

    public static boolean isX86_32() {
        return getArch().equals(Arch.X86_32);
    }

    public static boolean isX86_64() {
        return getArch().equals(Arch.X86_64);
    }

    public static Arch getArch() {
        // https://stackoverflow.com/questions/15240835/is-it-possible-to-detect-processor-architecture-in-java/15241209
        if (isWindows()) {
            return ARCH != null && ARCH.contains("64") || ARCHW64 != null && ARCHW64.contains("64")
                    ? Arch.X86_64 : Arch.X86_32;
        }

        return OS_ARCH.contains("64") ? Arch.X86_64 : Arch.X86_32;
    }

    public enum Arch {
        X86_32,
        X86_64
    }

    public static void print(PrintWriter out) {
        List<String> keys = Arrays.asList(
                "os.name",
                "os.arch",
                "os.version",
                "java.version",
                "java.home",
                "java.vm.vendor",
                "java.vm.name",
                "java.vm.version"
        );

        for (String k : keys) {
            out.printf("%s = %s\n", k, System.getProperty(k, "-"));
        }

        out.printf("isWindows = %s\n", isWindows());
        out.printf("isMac = %s\n", isMac());
        out.printf("isUnix = %s\n", isUnix());
        out.printf("isSolaris = %s\n", isSolaris());
        out.printf("isX86_32 = %s\n", isX86_32());
        out.printf("isX86_64 = %s\n", isX86_64());
    }
}
