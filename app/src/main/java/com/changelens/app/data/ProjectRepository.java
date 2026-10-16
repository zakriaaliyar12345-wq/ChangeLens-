package com.changelens.app.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.changelens.app.data.dao.AnnotationDao;
import com.changelens.app.data.dao.PhotoEntryDao;
import com.changelens.app.data.dao.ProjectDao;
import com.changelens.app.data.entity.Annotation;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.data.entity.Project;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.ImageStore;

import java.util.List;
import java.util.function.Consumer;

/**
 * Single source of truth for data access. UI talks to the repository;
 * the repository serializes disk/database work on a background executor.
 */
public class ProjectRepository {

    public interface Callback<T> {
        void onResult(T result);
    }

    private static volatile ProjectRepository instance;

    private final AppDatabase db;
    private final ProjectDao projects;
    private final PhotoEntryDao photos;
    private final AnnotationDao annotations;
    private final ImageStore imageStore;
    private final AppExecutors executors = AppExecutors.get();

    private ProjectRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.projects = db.projectDao();
        this.photos = db.photoEntryDao();
        this.annotations = db.annotationDao();
        this.imageStore = new ImageStore(context.getApplicationContext());
    }

    public static ProjectRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (ProjectRepository.class) {
                if (instance == null) {
                    instance = new ProjectRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    // ---- Projects ----

    public LiveData<List<Project>> observeProjects(int sortMode) {
        switch (sortMode) {
            case SettingsRepository.SORT_OLDEST:
                return projects.getAllSortedByStartDate();
            case SettingsRepository.SORT_TITLE:
                return projects.getAllSortedByTitle();
            case SettingsRepository.SORT_CREATED:
                return projects.getAllSortedByCreated();
            case SettingsRepository.SORT_RECENT:
            default:
                return projects.getAllSortedByUpdated();
        }
    }

    public LiveData<Project> observeProject(long id) {
        return projects.getById(id);
    }

    public void getProjectBlocking(long id, @NonNull Callback<Project> callback) {
        executors.diskIO().execute(() -> {
            Project p = projects.getByIdBlocking(id);
            executors.main(() -> callback.onResult(p));
        });
    }

    public void insertProject(@NonNull Project project, @NonNull Callback<Long> callback) {
        executors.diskIO().execute(() -> {
            long id = projects.insert(project);
            executors.main(() -> callback.onResult(id));
        });
    }

    public void updateProject(@NonNull Project project, @NonNull Runnable onDone) {
        executors.diskIO().execute(() -> {
            project.updatedAt = System.currentTimeMillis();
            projects.update(project);
            executors.main(onDone);
        });
    }

    public void deleteProjectDeep(long projectId, @NonNull Runnable onDone) {
        executors.diskIO().execute(() -> {
            Project p = projects.getByIdBlocking(projectId);
            List<PhotoEntry> entries = photos.getForProjectOrderedBlocking(projectId);
            projects.delete(p); // cascades photos + annotations in one transaction
            for (PhotoEntry e : entries) {
                imageStore.deleteManagedFiles(e.imagePath, e.thumbnailPath);
            }
            executors.main(onDone);
        });
    }

    // ---- Photos ----

    public LiveData<List<PhotoEntry>> observePhotos(long projectId) {
        return photos.getForProjectOrdered(projectId);
    }

    public LiveData<Integer> observePhotoCount(long projectId) {
        return photos.countForProject(projectId);
    }

    public void getPhotoBlocking(long id, @NonNull Callback<PhotoEntry> callback) {
        executors.diskIO().execute(() -> {
            PhotoEntry e = photos.getByIdBlocking(id);
            executors.main(() -> callback.onResult(e));
        });
    }

    /**
     * Imports an image URI into managed storage and records the entry.
     * Never mutates the original file.
     */
    public void importPhoto(long projectId, @NonNull android.net.Uri sourceUri,
                            @NonNull String caption, @NonNull String notes,
                            long capturedAt, @NonNull Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            boolean ok = false;
            try {
                ImageStore.ImportResult result = imageStore.importFromUri(sourceUri);
                if (result != null) {
                    PhotoEntry entry = new PhotoEntry();
                    entry.projectId = projectId;
                    entry.imagePath = result.imagePath;
                    entry.thumbnailPath = result.thumbnailPath;
                    entry.capturedAt = capturedAt;
                    entry.createdAt = System.currentTimeMillis();
                    entry.caption = caption;
                    entry.notes = notes;
                    entry.displayOrder = photos.maxDisplayOrder(projectId) + 1;
                    photos.insert(entry);
                    Project p = projects.getByIdBlocking(projectId);
                    if (p != null) {
                        if (p.coverImage == null) {
                            p.coverImage = result.thumbnailPath != null
                                    ? result.thumbnailPath : result.imagePath;
                        }
                        p.updatedAt = System.currentTimeMillis();
                        projects.update(p);
                    }
                    ok = true;
                }
            } catch (Exception ignored) {
                ok = false;
            }
            final boolean success = ok;
            executors.main(() -> callback.onResult(success));
        });
    }

    public void updatePhotoDetails(long photoId, @NonNull String caption,
                                   @NonNull String notes, long capturedAt,
                                   @NonNull Runnable onDone) {
        executors.diskIO().execute(() -> {
            photos.updateDetails(photoId, caption, notes, capturedAt);
            executors.main(onDone);
        });
    }

    public void deletePhoto(long photoId, @NonNull Runnable onDone) {
        executors.diskIO().execute(() -> {
            PhotoEntry e = photos.getByIdBlocking(photoId);
            if (e != null) {
                photos.deleteById(photoId);
                imageStore.deleteManagedFiles(e.imagePath, e.thumbnailPath);
                Project p = projects.getByIdBlocking(e.projectId);
                if (p != null) {
                    p.updatedAt = System.currentTimeMillis();
                    projects.update(p);
                }
            }
            executors.main(onDone);
        });
    }

    // ---- Annotations ----

    public LiveData<List<Annotation>> observeAnnotations(long photoId) {
        return annotations.getForPhoto(photoId);
    }

    public void addAnnotation(@NonNull Annotation annotation, @NonNull Callback<Long> callback) {
        executors.diskIO().execute(() -> {
            long id = annotations.insert(annotation);
            executors.main(() -> callback.onResult(id));
        });
    }

    public void deleteAnnotation(long id, @NonNull Runnable onDone) {
        executors.diskIO().execute(() -> {
            annotations.deleteById(id);
            executors.main(onDone);
        });
    }

    public void getAnnotationsBlocking(long photoId, @NonNull Callback<List<Annotation>> cb) {
        executors.diskIO().execute(() -> {
            List<Annotation> list = annotations.getForPhotoBlocking(photoId);
            executors.main(() -> cb.onResult(list));
        });
    }

    // ---- Counts (storage screen / tests) ----

    public void getCounts(@NonNull Consumer<int[]> callback) {
        executors.diskIO().execute(() -> {
            int[] counts = {
                    projects.countBlocking(),
                    photos.countBlocking(),
                    annotations.countBlocking()
            };
            executors.main(() -> callback.accept(counts));
        });
    }

    public void getNewestActiveProject(@NonNull Callback<Project> callback) {
        executors.diskIO().execute(() -> {
            Project p = projects.getNewestActiveBlocking();
            executors.main(() -> callback.onResult(p));
        });
    }
}
