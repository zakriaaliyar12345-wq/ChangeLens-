package com.changelens.app.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.changelens.app.data.dao.AnnotationDao;
import com.changelens.app.data.dao.PhotoEntryDao;
import com.changelens.app.data.dao.ProjectDao;
import com.changelens.app.data.entity.Annotation;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.data.entity.Project;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Room database (topic 10.1) built on SQLite (topic 10.0).
 * Foreign keys cascade so deleting a project removes its photos and annotations
 * transactionally; image files are deleted separately by the repository after
 * the database transaction succeeds.
 */
@Database(
        entities = {Project.class, PhotoEntry.class, Annotation.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private final AtomicInteger openCount = new AtomicInteger(0);

    public abstract ProjectDao projectDao();

    public abstract PhotoEntryDao photoEntryDao();

    public abstract AnnotationDao annotationDao();

    public static AppDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "changelens.db")
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /** For instrumentation tests. */
    public static void setInstance(AppDatabase db) {
        INSTANCE = db;
    }
}
