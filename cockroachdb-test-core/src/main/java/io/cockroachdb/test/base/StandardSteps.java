package io.cockroachdb.test.base;

import java.util.List;

import io.cockroachdb.test.download.DownloadStep;
import io.cockroachdb.test.init.InitStep;
import io.cockroachdb.test.process.ProcessStep;
import io.cockroachdb.test.unpack.UnpackStep;

public abstract class StandardSteps {
    public static final List<Step> LIST
            = List.of(new DownloadStep(), new UnpackStep(), new ProcessStep(), new InitStep());

    private StandardSteps() {
    }
}
