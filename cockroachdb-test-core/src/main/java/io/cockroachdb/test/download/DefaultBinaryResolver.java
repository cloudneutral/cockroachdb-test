package io.cockroachdb.test.download;

import io.cockroachdb.test.BinaryResolver;
import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.util.OperatingSystem;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultBinaryResolver implements BinaryResolver {
    @Override
    public URL resolveBinaryURL(Cockroach setup) {
        StringBuilder sb = new StringBuilder();
        sb.append(setup.baseURL());
        if (!setup.baseURL().endsWith("/")) {
            sb.append("/");
        }
        sb.append("cockroach-");
        if (!setup.version().startsWith("v")) {
            sb.append("v");
        }
        sb.append(setup.version());
        sb.append(".");
        sb.append(osBinaryName());
        sb.append("-");
        sb.append(setup.architecture());
        if (OperatingSystem.isWindows()) {
            sb.append(".zip");
        } else {
            sb.append(".tgz");
        }

        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String osBinaryName() {
        if (OperatingSystem.isMac()) {
            return "darwin-10.9";
        } else if (OperatingSystem.isWindows()) {
            return "windows-6.2";
        } else if (OperatingSystem.isUnix()) {
            return "linux";
        } else {
            throw new IllegalStateException("No CockroachDB binary for O/S: " + OperatingSystem.OS_NAME);
        }
    }

}
