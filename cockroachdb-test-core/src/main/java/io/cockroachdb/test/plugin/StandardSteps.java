package io.cockroachdb.test.plugin;

import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.download.DownloadStep;
import io.cockroachdb.test.init.InitStep;
import io.cockroachdb.test.process.ProcessStep;
import io.cockroachdb.test.unpack.UnpackStep;

import java.util.List;

public abstract class StandardSteps {
    public static final List<Step> LIST
            = List.of(new DownloadStep(), new UnpackStep(), new ProcessStep(), new InitStep());

    private StandardSteps() {
    }
}
