package io.cockroachdb.test;

import java.net.URL;

@FunctionalInterface
public interface URLResolver {
    /**
     * Resolve download URL for CockroachDB binary.
     *
     * @param cockroach the test annotation with parameters
     * @return download URL
     */
    URL resolveBinaryURL(Cockroach cockroach);
}
