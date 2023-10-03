package io.cockroachdb.test;

import io.cockroachdb.test.download.DefaultBinaryResolver;

import java.lang.annotation.*;

/**
 * Describes the CockroachDB version to download, install and launch
 * as part of the test lifecycle.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Cockroach {
    /**
     * Defines the version to download.
     */
    String version() default "v23.1.10";

    /**
     * Defines the base URL of the binaries http repository.
     */
    String baseURL() default "https://binaries.cockroachdb.com";

    /**
     * Defines the CPU architecture for the binary.
     */
    Architecture architecture() default Architecture.amd64;

    enum Architecture {
        amd64,
        arm64;
    }

    /**
     * Defines the download URL resolver class.
     */
    Class<? extends BinaryResolver> binaryResolver() default DefaultBinaryResolver.class;

    /**
     * Determines if downloaded binaries should be cached locally or not between test runs.
     * Uses conditional HTTP cache header directives if retained.
     */
    boolean cacheBinary() default true;

    /**
     * Defines the CockroachDB command to use when running the process.
     */
    Command command() default Command.demo;

    enum Command {
        start_single_node,
        demo
    }

    /**
     * Defines flags for the start and start_single_node commands.
     */
    StartFlags startFlags() default @StartFlags();

    /**
     * Defines flags for the demo command.
     */
    DemoFlags demoFlags() default @DemoFlags(global = true, nodes = 9);

    /**
     * Defines a semicolon separated list of SQL statements to execute for initialization.
     */
    String initSQL() default "";
}
