package io.cockroachdb.test;

/**
 * Value object with details around the local CockroachDB process that
 * follows the test lifecycle. It's either injected via reflection to the
 * test instance (if there's a setProcessDetails method) or it can
 * be located through the test context.
 */
public class ProcessDetails {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final ProcessDetails instance = new ProcessDetails();

        public Builder withConnectionURL(String connectionURL) {
            instance.connectionURL = connectionURL;
            return this;
        }

        public Builder withUser(String user) {
            instance.user = user;
            return this;
        }

        public Builder withPassword(String password) {
            instance.password = password;
            return this;
        }

        public Builder withHost(String host) {
            instance.host = host;
            return this;
        }

        public Builder withPort(int port) {
            instance.port = port;
            return this;
        }

        public Builder withUri(String uri) {
            instance.uri = uri;
            return this;
        }

        public Builder withJdbcURL(String jdbcURL) {
            instance.jdbcURL = jdbcURL;
            return this;
        }

        public ProcessDetails build() {
            return instance;
        }
    }

    private String user;

    private String password;

    private String host;

    private int port;

    private String uri;

    private String jdbcURL;

    private String connectionURL;

    public String getConnectionURL() {
        return connectionURL;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUri() {
        return uri;
    }

    public String getJdbcURL() {
        return jdbcURL;
    }

    @Override
    public String toString() {
        return "CockroachDetails {" +
                "\nuser='" + user + '\'' +
                "\n, password='" + password + '\'' +
                "\n, host='" + host + '\'' +
                "\n, port=" + port +
                "\n, uri='" + uri + '\'' +
                "\n, jdbcURL='" + jdbcURL + '\'' +
                "\n, connectionURL='" + connectionURL + '\'' +
                '}';
    }
}
