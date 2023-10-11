package io.cockroachdb.test.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit-test")
public class ByteFormatTest {
    @Test
    public void testByteCountToDisplaySize() {
        assertTrue(Paths.get("@abc").toString().startsWith("@"));

        assertEquals("0 bytes", ByteFormat.byteCountToDisplaySize(0L));
        assertEquals("1 bytes", ByteFormat.byteCountToDisplaySize(1L));
        assertEquals("99 bytes", ByteFormat.byteCountToDisplaySize(99L));
        assertEquals("999 bytes", ByteFormat.byteCountToDisplaySize(999L));
        assertEquals("1 KB", ByteFormat.byteCountToDisplaySize(1000L));
        assertEquals("999 bytes", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_KB - 1));

        assertEquals("1 KB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_KB));
        assertEquals("1 KB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_KB + 1));
        assertEquals("2 KB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_KB * 2));
        assertEquals("2 KB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_KB * 2 - 1));
        assertEquals("2 KB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_KB * 2 + 1));
        assertEquals("999 KB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_MB - ByteFormat.ONE_KB));

        assertEquals("1 MB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_MB));
        assertEquals("1 MB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_MB - 1L));
        assertEquals("1 MB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_MB + 1L));
        assertEquals("1 MB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_MB + ByteFormat.ONE_KB));
        assertEquals("1.1 MB", ByteFormat.byteCountToDisplaySize((long) (ByteFormat.ONE_MB * 1.1)));

        assertEquals("1 GB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_GB));
        assertEquals("1 GB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_GB + 1));
        assertEquals("1 GB", ByteFormat.byteCountToDisplaySize(ByteFormat.ONE_GB - 1));
        assertEquals("1.1 GB", ByteFormat.byteCountToDisplaySize((long) (ByteFormat.ONE_GB * 1.1)));
    }
}
