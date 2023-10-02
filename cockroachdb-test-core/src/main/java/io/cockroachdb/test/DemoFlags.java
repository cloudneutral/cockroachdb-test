package io.cockroachdb.test;

import java.lang.annotation.*;

/**
 * Annotation for integration tests using CockroachDB demo command.
 * https://www.cockroachlabs.com/docs/stable/cockroach-demo#flags
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DemoFlags {
    // --auto-enable-rangefeeds
    boolean autoEnableRangeFeeds() default true;

    // --cache
    String cache() default "64MiB";

    // --demo-locality
    String demoLocality() default "";

    // --disable-demo-license
    boolean disableDemoLicense() default false;

    // --echo-sql
    boolean echoSQL() default false;

    // --embedded
    boolean embedded() default true;

    // --execute
    // -e
    String execute() default "";

    // --format
    String format() default "";

    // --geo-partitioned-replicas
    boolean geoPartitionedReplicas() default false;

    // --global
    boolean global() default false;

    // --no-example-database
    boolean noExampleDatabase() default false;

    // --http-port
    int httpPort() default 0;

    // --insecure
    boolean insecure() default false;

    // --listening-url-file
    String listeningURLFile() default "listening-url.txt";

    // --max-sql-memory
    String maxSQLMemory() default "";

    // --nodes
    int nodes() default 1;

    // --safe-updates
    boolean safeUpdates() default true;

    // --set
    String set() default "";

    // --sql-port
    int sqlPort() default 0;
}
