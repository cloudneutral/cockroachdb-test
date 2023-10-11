package io.cockroachdb.test.junit4;

import java.util.HashMap;
import java.util.Map;

import io.cockroachdb.test.TestContext;

public class ExtensionStoreContext implements TestContext {
    public static ExtensionStoreContext of() {
        return new ExtensionStoreContext();
    }

    private final Map<String,Object> extensionContext = new HashMap<>();

    public ExtensionStoreContext() {
    }

    @Override
    public void put(String key, Object value) {
        extensionContext.put(key,value);
    }

    @Override
    public <T> T get(String key, Class<T> expectedType) {
        return expectedType.cast(extensionContext.get(key));
    }
}
