package io.cockroachdb.test.util;

import java.util.Locale;

public abstract class TimeFormat {
    private TimeFormat() {
    }

    /**
     * Formats a given millisecond value to a string.
     *
     * @param timeMillis the time value in milliseconds
     * @return the formatted string
     * @see java.lang.System#currentTimeMillis()
     */
    public static String millisecondsToDisplayString(long timeMillis) {
        double seconds = (timeMillis / 1000.0) % 60;
        int minutes = (int) ((timeMillis / 60000) % 60);
        int hours = (int) ((timeMillis / 3600000));

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(String.format("%dh ", hours));
        }
        if (hours > 0 || minutes > 0) {
            sb.append(String.format("%dm ", minutes));
        }
        if (hours == 0) {
            sb.append(String.format(Locale.US, "%.3fs", seconds));
        }
        return sb.toString();
    }
}
