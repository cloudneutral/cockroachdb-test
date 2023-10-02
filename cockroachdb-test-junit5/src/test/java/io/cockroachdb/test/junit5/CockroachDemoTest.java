package io.cockroachdb.test.junit5;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.DemoFlags;
import org.junit.jupiter.api.extension.RegisterExtension;

@Cockroach(
        version = "v23.1.10",
        architecture = Cockroach.Architecture.amd64,
        command = Cockroach.Command.demo,
        demoFlags = @DemoFlags(global = true, nodes = 9)
)
public class CockroachDemoTest extends CockroachJunit5Test {
    @RegisterExtension
    public static CockroachExtension cockroachExtension =
            CockroachExtension.builder()
                    .withTestClass(CockroachDemoTest.class)
                    .build();
}
