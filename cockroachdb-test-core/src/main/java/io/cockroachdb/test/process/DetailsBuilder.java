package io.cockroachdb.test.process;

import io.cockroachdb.test.ProcessDetails;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class DetailsBuilder {
    // 1: user
    // 2: pwd
    // 3: host
    // 4: port
    // 5: uri
    private static final Pattern CONNECTION_URL_PATTERN
            = Pattern.compile(
            "postgresql:(?=.)(?:/$|//(?:(?<user>[^:\\n\\r]+):(?<pass>[^@\\n\\r]+)@)?(?<host>[^:/\\r\\n]+)(?::(?<port>\\d+))?/)(?<uri>.*)?",
            Pattern.CASE_INSENSITIVE);

    public static ProcessDetails fromConnectionURL(String connectionURL) {
        ProcessDetails.Builder builder = ProcessDetails.builder();
        builder.withConnectionURL(connectionURL);

        Matcher m = CONNECTION_URL_PATTERN.matcher(connectionURL);
        if (m.find()) {
            String host = m.group("host");
            String user = m.group("user");
            String pass = m.group("pass");
            String uri = m.group("uri");
            int port = Integer.parseInt(m.group("port"));

            int sep = host.indexOf("@");
            if (sep >= 0) {
                user = host.substring(0, sep);
                host = host.substring(sep + 1);
            }

            builder.withHost(host);
            builder.withUser(user);
            builder.withPassword(pass);
            builder.withPort(port);
            builder.withUri(uri);
            builder.withJdbcURL("jdbc:postgresql://"
                    + host + ":"
                    + port + "/"
                    + uri);
        } else {
            throw new IllegalStateException("Unrecognized connection URL: " + connectionURL);
        }

        return builder.build();
    }

    private DetailsBuilder() {
    }
}
