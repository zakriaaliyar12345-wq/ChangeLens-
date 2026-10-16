package com.changelens.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.changelens.app.util.ProjectValidator;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ProjectValidatorTest {

    private static final long START = 1_000_000L;

    @Test
    public void validProject_returnsNoErrors() {
        ProjectValidator.Result r = ProjectValidator.validate("Kitchen", "Desc", START, null);
        assertTrue(r.valid);
        assertNull(r.titleError);
        assertNull(r.descriptionError);
        assertNull(r.dateError);
    }

    @Test
    public void emptyTitle_returnsRequiredError() {
        ProjectValidator.Result r = ProjectValidator.validate("", "Desc", START, null);
        assertFalse(r.valid);
        assertEquals(ProjectValidator.ERR_TITLE_REQUIRED, r.titleError);
    }

    @Test
    public void nullTitle_returnsRequiredError() {
        ProjectValidator.Result r = ProjectValidator.validate(null, null, START, null);
        assertFalse(r.valid);
        assertEquals(ProjectValidator.ERR_TITLE_REQUIRED, r.titleError);
    }

    @Test
    public void whitespaceTitle_returnsRequiredError() {
        ProjectValidator.Result r = ProjectValidator.validate("   ", null, START, null);
        assertFalse(r.valid);
        assertEquals(ProjectValidator.ERR_TITLE_REQUIRED, r.titleError);
    }

    @Test
    public void longTitle_returnsTooLongError() {
        String longTitle = "x".repeat(ProjectValidator.MAX_TITLE + 1);
        ProjectValidator.Result r = ProjectValidator.validate(longTitle, null, START, null);
        assertFalse(r.valid);
        assertEquals(ProjectValidator.ERR_TITLE_LONG, r.titleError);
    }

    @Test
    public void exactMaxTitle_isValid() {
        String title = "x".repeat(ProjectValidator.MAX_TITLE);
        ProjectValidator.Result r = ProjectValidator.validate(title, null, START, null);
        assertTrue(r.valid);
    }

    @Test
    public void longDescription_returnsError() {
        String longDesc = "x".repeat(ProjectValidator.MAX_DESCRIPTION + 1);
        ProjectValidator.Result r = ProjectValidator.validate("Title", longDesc, START, null);
        assertFalse(r.valid);
        assertEquals(ProjectValidator.ERR_DESC_LONG, r.descriptionError);
    }

    @Test
    public void exactMaxDescription_isValid() {
        String desc = "x".repeat(ProjectValidator.MAX_DESCRIPTION);
        ProjectValidator.Result r = ProjectValidator.validate("Title", desc, START, null);
        assertTrue(r.valid);
    }

    @Test
    public void targetBeforeStart_returnsDateError() {
        ProjectValidator.Result r = ProjectValidator.validate("Title", null, START, START - 1);
        assertFalse(r.valid);
        assertEquals(ProjectValidator.ERR_DATE_ORDER, r.dateError);
    }

    @Test
    public void targetEqualsStart_isValid() {
        ProjectValidator.Result r = ProjectValidator.validate("Title", null, START, START);
        assertTrue(r.valid);
    }

    @Test
    public void targetAfterStart_isValid() {
        ProjectValidator.Result r = ProjectValidator.validate("Title", null, START, START + 1000);
        assertTrue(r.valid);
    }

    @Test
    public void pickCategory_nullCurrent_returnsFirst() {
        List<String> cats = Arrays.asList("A", "B", "C");
        assertEquals("A", ProjectValidator.pickCategory(cats, null));
    }

    @Test
    public void pickCategory_emptyCurrent_returnsFirst() {
        List<String> cats = Arrays.asList("A", "B");
        assertEquals("A", ProjectValidator.pickCategory(cats, ""));
    }

    @Test
    public void pickCategory_validCurrent_returnsCurrent() {
        List<String> cats = Arrays.asList("A", "B", "C");
        assertEquals("B", ProjectValidator.pickCategory(cats, "B"));
    }

    @Test
    public void indexOfCategory_found() {
        List<String> cats = Arrays.asList("A", "B", "C");
        assertEquals(1, ProjectValidator.indexOfCategory(cats, "B"));
    }

    @Test
    public void indexOfCategory_null_returnsZero() {
        List<String> cats = Arrays.asList("A", "B");
        assertEquals(0, ProjectValidator.indexOfCategory(cats, null));
    }

    @Test
    public void indexOfCategory_notFound_returnsZero() {
        List<String> cats = Arrays.asList("A", "B");
        assertEquals(0, ProjectValidator.indexOfCategory(cats, "Z"));
    }
}
