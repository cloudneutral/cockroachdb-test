package io.cockroachdb.test.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import io.cockroachdb.test.StepIOException;

class ProcessOutputReader implements Runnable {
    private final InputStream is;

    private final Consumer<String> callback;

    public ProcessOutputReader(InputStream is, Consumer<String> callback) {
        this.is = is;
        this.callback = callback;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line = reader.readLine();
            while (line != null) {
                callback.accept(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new StepIOException(e);
        }
    }
}
