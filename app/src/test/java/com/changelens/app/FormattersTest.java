package com.changelens.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.changelens.app.util.Formatters;

import org.junit.Test;

import java.util.Locale;

public class FormattersTest {

    @Test
    public void formatFileSize_bytes() {
        assertEquals("512 B", Formatters.formatFileSize(512));
    }

    @Test
    public void formatFileSize_exactKB() {
        assertEquals("1.0 KB", Formatters.formatFileSize(1024));
    }

    @Test
    public void formatFileSize_fractionalKB() {
        String result = Formatters.formatFileSize(1536);
        assertTrue(result.contains("KB"));
        assertTrue(result.startsWith("1.5"));
    }

    @Test
    public void formatFileSize_mb() {
        assertEquals("1.0 MB", Formatters.formatFileSize(1024 * 1024));
    }

    @Test
    public void formatFileSize_gb() {
        String result = Formatters.formatFileSize(1024L * 1024 * 1024);
        assertTrue(result.contains("GB"));
    }

    @Test
    public void formatFileSize_zero() {
        assertEquals("0 B", Formatters.formatFileSize(0));
    }

    @Test
    public void isoDate_format() {
        String iso = Formatters.isoDate(0L);
        assertEquals("1970-01-01", iso);
    }

    @Test
    public void isoDate_containsDashes() {
        String iso = Formatters.isoDate(1700000000000L);
        assertTrue(iso.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void formatFileSize_usesUSLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("fa", "IR"));
            assertEquals("1.0 MB", Formatters.formatFileSize(1024 * 1024));
        } finally {
            Locale.setDefault(original);
        }
    }
}
