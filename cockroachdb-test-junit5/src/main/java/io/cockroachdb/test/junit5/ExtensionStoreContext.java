package io.cockroachdb.test.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.cockroachdb.test.TestContext;

public class ExtensionStoreContext implements TestContext {
    public static ExtensionStoreContext of(ExtensionContext context) {
        return new ExtensionStoreContext(context);
    }

    private final ExtensionContext extensionContext;

    public ExtensionStoreContext(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    @Override
    public void put(String key, Object value) {
        extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).put(key,value);
    }

    @Override
    public <T> T get(String key, Class<T> expectedType) {
        return extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(key,expectedType);
    }
}
