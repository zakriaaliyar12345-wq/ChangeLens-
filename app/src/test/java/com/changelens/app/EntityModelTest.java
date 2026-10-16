package com.changelens.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.changelens.app.data.entity.Annotation;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.data.entity.Project;

import org.junit.Test;

public class EntityModelTest {

    @Test
    public void project_defaults() {
        Project p = new Project();
        assertEquals(0L, p.id);
        assertEquals("", p.title);
        assertEquals("", p.description);
        assertEquals("", p.category);
        assertFalse(p.archived);
        assertNull(p.coverImage);
    }

    @Test
    public void project_fieldsRoundTrip() {
        Project p = new Project();
        p.id = 42L;
        p.title = "Renovation";
        p.description = "Full house";
        p.category = "Home";
        p.createdAt = 100L;
        p.startDate = 200L;
        p.targetDate = 999L;
        p.coverImage = "/data/img.jpg";
        p.archived = true;
        p.updatedAt = 50L;

        assertEquals(42L, p.id);
        assertEquals("Renovation", p.title);
        assertEquals("Full house", p.description);
        assertEquals("Home", p.category);
        assertEquals(100L, p.createdAt);
        assertEquals(200L, p.startDate);
        assertEquals(Long.valueOf(999L), p.targetDate);
        assertEquals("/data/img.jpg", p.coverImage);
        assertTrue(p.archived);
        assertEquals(50L, p.updatedAt);
    }

    @Test
    public void photoEntry_defaults() {
        PhotoEntry pe = new PhotoEntry();
        assertEquals(0L, pe.id);
        assertEquals(0L, pe.projectId);
        assertEquals("", pe.imagePath);
        assertNull(pe.thumbnailPath);
        assertEquals("", pe.caption);
        assertEquals("", pe.notes);
        assertEquals(0, pe.displayOrder);
    }

    @Test
    public void photoEntry_fieldsRoundTrip() {
        PhotoEntry pe = new PhotoEntry();
        pe.id = 7L;
        pe.projectId = 42L;
        pe.imagePath = "/data/img.jpg";
        pe.thumbnailPath = "/data/thumb.jpg";
        pe.capturedAt = 500L;
        pe.createdAt = 600L;
        pe.caption = "Day 1";
        pe.notes = "Demo";
        pe.displayOrder = 3;

        assertEquals(7L, pe.id);
        assertEquals(42L, pe.projectId);
        assertEquals("/data/img.jpg", pe.imagePath);
        assertEquals("/data/thumb.jpg", pe.thumbnailPath);
        assertEquals(500L, pe.capturedAt);
        assertEquals(600L, pe.createdAt);
        assertEquals("Day 1", pe.caption);
        assertEquals("Demo", pe.notes);
        assertEquals(3, pe.displayOrder);
    }

    @Test
    public void annotation_defaults() {
        Annotation a = new Annotation();
        assertEquals(0L, a.id);
        assertEquals(0L, a.photoId);
        assertEquals(Annotation.TYPE_TEXT, a.type);
        assertEquals("", a.text);
        assertEquals(0f, a.normX, 0.001f);
        assertEquals(0f, a.normY, 0.001f);
    }

    @Test
    public void annotation_fieldsRoundTrip() {
        Annotation a = new Annotation();
        a.id = 9L;
        a.photoId = 7L;
        a.type = Annotation.TYPE_TEXT;
        a.text = "Crack here";
        a.normX = 0.5f;
        a.normY = 0.75f;
        a.createdAt = 800L;

        assertEquals(9L, a.id);
        assertEquals(7L, a.photoId);
        assertEquals(Annotation.TYPE_TEXT, a.type);
        assertEquals("Crack here", a.text);
        assertEquals(0.5f, a.normX, 0.001f);
        assertEquals(0.75f, a.normY, 0.001f);
        assertEquals(800L, a.createdAt);
    }

    @Test
    public void annotation_typeConstant() {
        assertEquals(0, Annotation.TYPE_TEXT);
    }
}
