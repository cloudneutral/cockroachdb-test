package io.cockroachdb.test.download;

import java.net.MalformedURLException;
import java.net.URL;

import io.cockroachdb.test.URLResolver;
import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.util.OperatingSystem;

public class DefaultURLResolver implements URLResolver {
    @Override
    public URL resolveBinaryURL(Cockroach cockroach) {
        StringBuilder sb = new StringBuilder();
        sb.append(cockroach.baseURL());
        if (!cockroach.baseURL().endsWith("/")) {
            sb.append("/");
        }
        sb.append("cockroach-");
        if (!cockroach.version().startsWith("v")) {
            sb.append("v");
        }
        sb.append(cockroach.version());
        sb.append(".");
        sb.append(osBinaryName(cockroach.experimental()));
        sb.append("-");
        sb.append(cockroach.architecture());
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

    private static String osBinaryName(boolean experimental) {
        if (OperatingSystem.isMac()) {
            return experimental ? "darwin-11.0" : "darwin-10.9";
        } else if (OperatingSystem.isWindows()) {
            return "windows-6.2";
        } else if (OperatingSystem.isUnix()) {
            return "linux";
        } else {
            throw new IllegalStateException("No CockroachDB binary for O/S: " + OperatingSystem.OS_NAME);
        }
    }

}
