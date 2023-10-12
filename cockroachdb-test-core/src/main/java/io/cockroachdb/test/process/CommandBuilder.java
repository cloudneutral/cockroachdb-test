package io.cockroachdb.test.process;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.DemoFlags;
import io.cockroachdb.test.StartFlags;
import io.cockroachdb.test.util.StringUtils;

abstract class CommandBuilder {
    private CommandBuilder() {
    }

    public static List<String> toCommand(Path executable, Cockroach cockroach) {
        return switch (cockroach.command()) {
            case demo -> CommandBuilder.toCommand(executable, cockroach.demoFlags());
            case start_single_node -> CommandBuilder.toCommand(executable, cockroach.startFlags());
        };
    }

    public static List<String> toCommand(Path executable, DemoFlags flags) {
        List<String> commands = new ArrayList<>();
        commands.add(executable.toAbsolutePath().toString());
        commands.add("demo");
        if (!flags.autoEnableRangeFeeds()) {
            commands.add("--auto-enable-rangefeeds=false");
        }
        if (!"64MiB".equals(flags.cache())) {
            commands.add("--cache=" + flags.cache());
        }
        if (StringUtils.hasLength(flags.demoLocality())) {
            commands.add("--demo-locality=" + flags.demoLocality());
        }
        if (flags.disableDemoLicense()) {
            commands.add("--disable-demo-license");
        }
        if (flags.echoSQL()) {
            commands.add("--echo-sql");
        }
        if (flags.embedded()) {
            commands.add("--embedded");
        }
        if (StringUtils.hasLength(flags.execute())) {
            commands.add("--execute=" + flags.execute());
        }
        if (StringUtils.hasLength(flags.format())) {
            commands.add("--format=" + flags.format());
        }
        if (flags.geoPartitionedReplicas()) {
            commands.add("--geo-partitioned-replicas");
        }
        if (flags.global()) {
            commands.add("--global");
        }
        if (flags.noExampleDatabase()) {
            commands.add("--no-example-database");
        }
        if (flags.httpPort() != 0) {
            commands.add("--http-port=" + flags.httpPort());
        }
        if (flags.insecure()) {
            commands.add("--insecure");
        }
        if (StringUtils.hasLength(flags.listeningURLFile())) {
            commands.add("--listening-url-file=" + flags.listeningURLFile());
        }
        if (StringUtils.hasLength(flags.maxSQLMemory())) {
            commands.add("--max-sql-memory=" + flags.maxSQLMemory());
        }
        if (flags.nodes() != 0) {
            commands.add("--nodes=" + flags.nodes());
        }
        if (flags.safeUpdates()) {
            commands.add("--safe-updates");
        }
        if (StringUtils.hasLength(flags.set())) {
            commands.add("--set=" + flags.set());
        }
        if (flags.sqlPort() != 0) {
            commands.add("--sql-port=" + flags.sqlPort());
        }
        return commands;
    }

    public static List<String> toCommand(Path executable, StartFlags flags) {
        List<String> commands = new ArrayList<>();
        commands.add(executable.toAbsolutePath().toString());
        commands.add("start-single-node");
        if (StringUtils.hasLength(flags.cache())) {
            commands.add("--cache=" + flags.cache());
        }
        if (StringUtils.hasLength(flags.externalIODir())) {
            commands.add("--external-io-dir=" + flags.externalIODir());
        }
        if (StringUtils.hasLength(flags.listeningURLFile())) {
            commands.add("--listening-url-file=" + flags.listeningURLFile());
        }
        if (StringUtils.hasLength(flags.maxDiskTempStorage())) {
            commands.add("--max-disk-temp-storage=" + flags.maxDiskTempStorage());
        }
        if (StringUtils.hasLength(flags.maxGoMemory())) {
            commands.add("--max-go-memory=" + flags.maxGoMemory());
        }
        if (StringUtils.hasLength(flags.maxSQLMemory())) {
            commands.add("--max-sql-memory=" + flags.maxSQLMemory());
        }
        if (StringUtils.hasLength(flags.maxTSDBMemory())) {
            commands.add("--max-tsdb-memory=" + flags.maxTSDBMemory());
        }
        if (StringUtils.hasLength(flags.pidFile())) {
            commands.add("--pid-file=" + flags.pidFile());
        }
        if (StringUtils.hasLength(flags.tempDir())) {
            commands.add("--temp-dir=" + flags.tempDir());
        }
        if (StringUtils.hasLength(flags.listenAddr())) {
            commands.add("--listen-addr=" + flags.listenAddr());
        }
        if (StringUtils.hasLength(flags.httpAddr())) {
            commands.add("--http-addr=" + flags.httpAddr());
        }
        if (StringUtils.hasLength(flags.socketDir())) {
            commands.add("--socket-dir=" + flags.socketDir());
        }
        if (StringUtils.hasLength(flags.certsDir())) {
            commands.add("--certs-dir=" + flags.certPrincipalMap());
        }
        if (flags.insecure()) {
            commands.add("--insecure");
        }
        if (flags.acceptSQLWithoutTLS()) {
            commands.add("--accept-sql-without-tls");
        }
        if (StringUtils.hasLength(flags.certPrincipalMap())) {
            commands.add("--cert-principal-map=" + flags.certPrincipalMap());
        }
        if (StringUtils.hasLength(flags.enterpriseEncryption())) {
            commands.add("--enterprise-encryption=" + flags.enterpriseEncryption());
        }
        if (StringUtils.hasLength(flags.store())) {
            commands.add("--store=" + flags.store());
        }
        if (StringUtils.hasLength(flags.log())) {
            commands.add("--log=" + flags.log());
        }
        if (StringUtils.hasLength(flags.logConfigFile())) {
            commands.add("--log-config-file=" + flags.logConfigFile());
        }
        if (StringUtils.hasLength(flags.logDir())) {
            commands.add("--log-dir=" + flags.logDir());
        }
        if (StringUtils.hasLength(flags.logGroupMaxSize())) {
            commands.add("--log-group-max-size=" + flags.logGroupMaxSize());
        }
        if (StringUtils.hasLength(flags.logFileVerbosity())) {
            commands.add("--log-file-verbosity=" + flags.logFileVerbosity());
        }
        if (flags.noColor()) {
            commands.add("--no-color=true");
        }
        if (flags.redactableLogs()) {
            commands.add("--redactable-logs=true");
        }

        return commands;
    }
}
