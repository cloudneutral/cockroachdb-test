package io.cockroachdb.test.util;

import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public abstract class FileUtils {
    public static final Set<PosixFilePermission> PERMISSIONS = new HashSet<>() {{
        add(PosixFilePermission.OWNER_READ);
        add(PosixFilePermission.OWNER_WRITE);
        add(PosixFilePermission.OWNER_EXECUTE);
    }};

    private FileUtils() {
    }
}
