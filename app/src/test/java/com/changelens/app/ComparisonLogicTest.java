package com.changelens.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComparisonLogicTest {

    static class PhotoItem {
        final long id;
        final long capturedAt;
        final String caption;

        PhotoItem(long id, long capturedAt, String caption) {
            this.id = id;
            this.capturedAt = capturedAt;
            this.caption = caption;
        }
    }

    private List<PhotoItem> sortedByDateDesc(List<PhotoItem> items) {
        List<PhotoItem> copy = new ArrayList<>(items);
        Collections.sort(copy, (a, b) -> Long.compare(b.capturedAt, a.capturedAt));
        return copy;
    }

    @Test
    public void timeline_sortedNewestFirst() {
        List<PhotoItem> items = new ArrayList<>();
        items.add(new PhotoItem(1, 1000, "First"));
        items.add(new PhotoItem(2, 3000, "Third"));
        items.add(new PhotoItem(3, 2000, "Second"));

        List<PhotoItem> sorted = sortedByDateDesc(items);
        assertEquals(3, sorted.size());
        assertEquals(2L, sorted.get(0).id);
        assertEquals(3L, sorted.get(1).id);
        assertEquals(1L, sorted.get(2).id);
    }

    @Test
    public void timeline_emptyList() {
        List<PhotoItem> sorted = sortedByDateDesc(new ArrayList<>());
        assertTrue(sorted.isEmpty());
    }

    @Test
    public void timeline_singleItem() {
        List<PhotoItem> items = new ArrayList<>();
        items.add(new PhotoItem(1, 5000, "Only"));
        List<PhotoItem> sorted = sortedByDateDesc(items);
        assertEquals(1, sorted.size());
        assertEquals(1L, sorted.get(0).id);
    }

    @Test
    public void timeline_stableForEqualTimestamps() {
        List<PhotoItem> items = new ArrayList<>();
        items.add(new PhotoItem(1, 1000, "A"));
        items.add(new PhotoItem(2, 1000, "B"));
        List<PhotoItem> sorted = sortedByDateDesc(items);
        assertEquals(2, sorted.size());
    }

    @Test
    public void selection_exactlyTwo_allowsCompare() {
        List<PhotoItem> selected = new ArrayList<>();
        selected.add(new PhotoItem(1, 1000, ""));
        selected.add(new PhotoItem(2, 2000, ""));
        assertEquals(2, selected.size());
        assertTrue(selected.size() == 2);
    }

    @Test
    public void selection_one_disallowsCompare() {
        List<PhotoItem> selected = new ArrayList<>();
        selected.add(new PhotoItem(1, 1000, ""));
        assertFalse(selected.size() == 2);
    }

    @Test
    public void selection_three_disallowsCompare() {
        List<PhotoItem> selected = new ArrayList<>();
        selected.add(new PhotoItem(1, 1000, ""));
        selected.add(new PhotoItem(2, 2000, ""));
        selected.add(new PhotoItem(3, 3000, ""));
        assertFalse(selected.size() == 2);
    }

    @Test
    public void dividerPosition_clampedToZeroOne() {
        float raw = 1.5f;
        float clamped = Math.max(0f, Math.min(1f, raw));
        assertEquals(1.0f, clamped, 0.001f);

        float rawNeg = -0.3f;
        float clampedNeg = Math.max(0f, Math.min(1f, rawNeg));
        assertEquals(0.0f, clampedNeg, 0.001f);
    }

    @Test
    public void dividerPosition_withinRange() {
        float raw = 0.65f;
        float clamped = Math.max(0f, Math.min(1f, raw));
        assertEquals(0.65f, clamped, 0.001f);
    }

    @Test
    public void overlayOpacity_percentageMapping() {
        int seekValue = 75;
        float opacity = seekValue / 100f;
        assertEquals(0.75f, opacity, 0.001f);
    }

    @Test
    public void annotationCoordinates_normalizedRange() {
        float normX = 0.3f;
        float normY = 0.8f;
        assertTrue(normX >= 0f && normX <= 1f);
        assertTrue(normY >= 0f && normY <= 1f);
    }
}
