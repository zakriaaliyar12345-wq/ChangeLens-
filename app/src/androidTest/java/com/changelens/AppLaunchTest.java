package com.changelens.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.changelens.app.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppLaunchTest {

    @Test
    public void mainActivity_launches() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_showsAppName() {
        ActivityScenario.launch(MainActivity.class);
        onView(withText("ChangeLens")).check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_emptyStateVisible() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.emptyState)).check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_fabVisible() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.fabCreate)).check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_searchVisible() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.searchInput)).check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_filterChipsVisible() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.chipAll)).check(matches(isDisplayed()));
        onView(withId(R.id.chipActive)).check(matches(isDisplayed()));
        onView(withId(R.id.chipArchived)).check(matches(isDisplayed()));
    }
}
