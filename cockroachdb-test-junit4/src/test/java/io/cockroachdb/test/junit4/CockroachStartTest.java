package io.cockroachdb.test.junit4;

import org.junit.ClassRule;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.StartFlags;

@Cockroach(
        version = "v23.2.1",
        architecture = Cockroach.Architecture.amd64,
        command = Cockroach.Command.start_single_node,
        startFlags = @StartFlags(listenAddr = "localhost"),
        initSQL = {
//                "SET CLUSTER SETTING kv.raft_log.disable_synchronization_unsafe = true",
                        "SET CLUSTER SETTING kv.range_merge.queue_interval = '50ms'",
                        "SET CLUSTER SETTING jobs.registry.interval.gc = '30s'",
                        "SET CLUSTER SETTING jobs.registry.interval.cancel = '180s'",
                        "SET CLUSTER SETTING jobs.retention_time = '15s'",
                        "SET CLUSTER SETTING sql.stats.automatic_collection.enabled = false",
                        "SET CLUSTER SETTING kv.range_split.by_load_merge_delay = '5s'",
                        "ALTER RANGE default CONFIGURE ZONE USING \"gc.ttlseconds\" = 600",
                        "ALTER DATABASE system CONFIGURE ZONE USING \"gc.ttlseconds\" = 600"
        }
)
public class CockroachStartTest extends AbstractCockroachTest {
    @ClassRule
    public static CockroachExtension extension =
            CockroachExtension.builder()
                    .withTestClass(CockroachStartTest.class)
                    .build();
}
