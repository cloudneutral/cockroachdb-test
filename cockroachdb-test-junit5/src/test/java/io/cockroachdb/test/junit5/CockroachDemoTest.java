package io.cockroachdb.test.junit5;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.DemoFlags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;

@Cockroach(
        version = "v23.1.10",
        architecture = Cockroach.Architecture.amd64,
        command = Cockroach.Command.demo,
        demoFlags = @DemoFlags(global = true, nodes = 9)
)
@Tag("integration-test")
public class CockroachDemoTest extends AbstractCockroachTest {
    @RegisterExtension
    public static CockroachExtension cockroachExtension =
            CockroachExtension.builder()
                    .withTestClass(CockroachDemoTest.class)
                    .build();
}
