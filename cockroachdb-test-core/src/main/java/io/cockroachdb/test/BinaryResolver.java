package io.cockroachdb.test;

import java.net.URL;

public interface BinaryResolver {
    /**
     * Resolve download URL for CockroachDB binary.
     *
     * @param cockroach the test annotation
     * @return download URL
     */
    URL resolveBinaryURL(Cockroach cockroach);
}
