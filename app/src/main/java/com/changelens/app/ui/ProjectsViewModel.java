package com.changelens.app.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.changelens.app.data.ProjectRepository;
import com.changelens.app.data.SettingsRepository;
import com.changelens.app.data.entity.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard ViewModel: exposes projects filtered by search text and
 * archive state, surviving configuration changes (topic 02.2).
 */
public class ProjectsViewModel extends AndroidViewModel {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_ACTIVE = 1;
    public static final int FILTER_ARCHIVED = 2;

    private final ProjectRepository repository;
    private final SettingsRepository settings;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> filter = new MutableLiveData<>(FILTER_ALL);
    private final MutableLiveData<Integer> sortMode;
    private final MediatorLiveData<List<Project>> visibleProjects = new MediatorLiveData<>();
    private LiveData<List<Project>> source;

    public ProjectsViewModel(@NonNull Application application) {
        super(application);
        repository = ProjectRepository.getInstance(application);
        settings = new SettingsRepository(application);
        sortMode = new MutableLiveData<>(settings.getProjectSort());

        LiveData<Integer> sort = sortMode;
        LiveData<List<Project>> currentSource = repository.observeProjects(settings.getProjectSort());
        source = currentSource;
        visibleProjects.addSource(currentSource, this::recompute);
        visibleProjects.addSource(searchQuery, q -> recompute(currentSource.getValue()));
        visibleProjects.addSource(filter, f -> recompute(currentSource.getValue()));
        sort.observeForever(s -> {
            LiveData<List<Project>> next = repository.observeProjects(s);
            visibleProjects.removeSource(source);
            source = next;
            visibleProjects.addSource(next, this::recompute);
        });
    }

    public LiveData<List<Project>> getVisibleProjects() {
        return visibleProjects;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query);
    }

    public void setFilter(int filterMode) {
        filter.setValue(filterMode);
    }

    public void setSortMode(int mode) {
        settings.setProjectSort(mode);
        sortMode.setValue(mode);
    }

    public int getSortMode() {
        Integer value = sortMode.getValue();
        return value == null ? SettingsRepository.SORT_RECENT : value;
    }

    private void recompute(List<Project> all) {
        if (all == null) {
            visibleProjects.setValue(new ArrayList<>());
            return;
        }
        String query = searchQuery.getValue() == null
                ? "" : searchQuery.getValue().trim().toLowerCase(Locale.ROOT);
        Integer filterValue = filter.getValue();
        int filterMode = filterValue == null ? FILTER_ALL : filterValue;
        List<Project> result = new ArrayList<>();
        for (Project p : all) {
            if (filterMode == FILTER_ACTIVE && p.archived) {
                continue;
            }
            if (filterMode == FILTER_ARCHIVED && !p.archived) {
                continue;
            }
            if (!query.isEmpty()) {
                String haystack = (p.title + " " + p.description + " " + p.category)
                        .toLowerCase(Locale.ROOT);
                if (!haystack.contains(query)) {
                    continue;
                }
            }
            result.add(p);
        }
        visibleProjects.setValue(result);
    }
}
