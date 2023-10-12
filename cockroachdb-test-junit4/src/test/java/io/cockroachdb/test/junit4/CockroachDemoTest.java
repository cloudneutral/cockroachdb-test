package io.cockroachdb.test.junit4;

import org.junit.ClassRule;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.DemoFlags;

@Cockroach(
        version = "v23.1.10",
        architecture = Cockroach.Architecture.amd64,
        command = Cockroach.Command.demo,
        demoFlags = @DemoFlags(global = true, nodes = 9)
)
public class CockroachDemoTest extends AbstractCockroachTest {
    @ClassRule
    public static CockroachExtension extension =
            CockroachExtension.builder()
                    .withTestClass(CockroachDemoTest.class)
                    .build();
}
