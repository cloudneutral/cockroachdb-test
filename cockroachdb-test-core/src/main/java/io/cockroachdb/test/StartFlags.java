package io.cockroachdb.test;

import java.lang.annotation.*;

/**
 * Annotation for integration tests using CockroachDB start or start-single-node commands.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface StartFlags {
    // --background
    boolean background() default true;

    // --cache
    String cache() default "64MiB";

    // --external-io-dir
    String externalIODir() default "";

    // --listening-url-file
    String listeningURLFile() default "listening-url.txt";

    // --max-disk-temp-storage
    String maxDiskTempStorage() default "";

    // --max-go-memory
    String maxGoMemory() default "";

    // --max-sql-memory
    String maxSQLMemory() default "";

    // --max-tsdb-memory
    String maxTSDBMemory() default "";

    // --pid-file
    String pidFile() default "";

    // --temp-dir
    String tempDir() default "";

    // --listen-addr
    String listenAddr() default "";

    // --http-addr
    String httpAddr() default "";

    // --socket-dir
    String socketDir() default "";

    // --certs-dir
    String certsDir() default "";

    // --insecure
    boolean insecure() default true;

    // --accept-sql-without-tls
    boolean acceptSQLWithoutTLS() default false;

    // --cert-principal-map
    String certPrincipalMap() default "";

    // --enterprise-encryption
    String enterpriseEncryption() default "";

    // --store
    String store() default "type=mem,size=75%";

    // --log
    String log() default "";

    // --log-config-file
    String logConfigFile() default "";

    // --log-dir
    String logDir() default "";

    // --log-group-max-size
    String logGroupMaxSize() default "";

    // --log-file-verbosity
    String logFileVerbosity() default "";

    // --no-color
    boolean noColor() default false;

    // --redactable-logs
    boolean redactableLogs() default false;
}
