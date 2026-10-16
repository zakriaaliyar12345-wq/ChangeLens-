package com.changelens.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.changelens.app.data.entity.Project;

import java.util.List;

@Dao
public interface ProjectDao {

    @Insert
    long insert(Project project);

    @Update
    void update(Project project);

    @Delete
    void delete(Project project);

    @Query("SELECT * FROM projects WHERE id = :id")
    LiveData<Project> getById(long id);

    @Query("SELECT * FROM projects WHERE id = :id")
    Project getByIdBlocking(long id);

    @Query("SELECT * FROM projects ORDER BY updated_at DESC")
    LiveData<List<Project>> getAllSortedByUpdated();

    @Query("SELECT * FROM projects ORDER BY start_date ASC")
    LiveData<List<Project>> getAllSortedByStartDate();

    @Query("SELECT * FROM projects ORDER BY title COLLATE NOCASE ASC")
    LiveData<List<Project>> getAllSortedByTitle();

    @Query("SELECT * FROM projects ORDER BY created_at DESC")
    LiveData<List<Project>> getAllSortedByCreated();

    @Query("SELECT COUNT(*) FROM projects")
    int countBlocking();

    @Query("SELECT * FROM projects WHERE archived = 0 ORDER BY updated_at DESC LIMIT 1")
    Project getNewestActiveBlocking();

    @Query("UPDATE projects SET cover_image = :cover WHERE id = :projectId")
    void updateCover(long projectId, String cover);
}
