package io.cockroachdb.test.download.http;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.cockroachdb.test.download.FileEntityReader;


public class HttpClientTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void whenQueryingCatFact_thenPrintResult() throws IOException {
        HttpRequest request = HttpClient
                .createDefault()
                .createRequest("https://catfact.ninja/fact", HttpMethod.GET);
        try (HttpResponse response = request.execute()) {
            logger.info("status: {}", response.getStatus());
            logger.info("content-type: {}", response.getContentType());
            logger.info("modified: {}", response.getLastModified());

            for (Map.Entry<String, String> e : response.getHeaders()) {
                logger.info("{} =  {}", e.getKey(), e.getValue());
            }

            logger.info(response.getBodyAsString());
        }
    }

    @Test
    public void whenDownloadingDogImage_thenWriteToTempFile() throws IOException {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();

        HttpRequest request = HttpClient.createDefault()
                .createRequest("https://dog.ceo/api/breeds/image/random", HttpMethod.GET);
        try (HttpResponse response = request.execute()) {
            Map map = gson.fromJson(response.getBodyAsString(), Map.class);

            String fileURI = map.get("message").toString();

            Path fileName = Paths.get(new URL(map.get("message").toString()).getFile()).getFileName();
            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir")).resolve(fileName);

            HttpClient client = HttpClient.createDefault()
                    .withRequestInterceptor(new GzipEncodingInterceptor());
            client.execute(fileURI, HttpMethod.GET, new FileEntityReader(tempFile), null);

            logger.info("Downloaded {} => {}", fileURI, tempFile);
        }
    }

}
