package io.cockroachdb.test.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class StringUtils {
    private static final String PARAMS_REGEX = "[,;\n]";

    private StringUtils() {
    }

    public static Set<Path> delimitedStringToPaths(String text) {
        Set<Path> paths = new HashSet<>();
        delimitedStringToList(text).forEach(item -> paths.add(Paths.get(item)));
        return paths;
    }

    public static Set<String> delimitedStringToSet(String text) {
        return new HashSet<>(delimitedStringToList(text));
    }

    public static List<String> delimitedStringToList(String text) {
        return Arrays.asList(text != null && text.length() > 0 ? trim(text.split(PARAMS_REGEX)) : new String[] {});
    }

    private static String[] trim(String[] array) {
        Arrays.parallelSetAll(array, (i) -> array[i].trim());
        return array;
    }

    public static boolean hasLength(String text) {
        return text != null && text.length() != 0;
    }
}
