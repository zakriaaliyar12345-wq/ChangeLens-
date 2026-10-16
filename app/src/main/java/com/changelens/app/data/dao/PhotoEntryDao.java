package com.changelens.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.changelens.app.data.entity.PhotoEntry;

import java.util.List;

@Dao
public interface PhotoEntryDao {

    @Insert
    long insert(PhotoEntry entry);

    @Update
    void update(PhotoEntry entry);

    @Query("DELETE FROM photo_entries WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM photo_entries WHERE id = :id")
    PhotoEntry getByIdBlocking(long id);

    @Query("SELECT * FROM photo_entries WHERE id = :id")
    LiveData<PhotoEntry> getById(long id);

    @Query("SELECT * FROM photo_entries WHERE project_id = :projectId "
            + "ORDER BY captured_at ASC, display_order ASC")
    LiveData<List<PhotoEntry>> getForProjectOrdered(long projectId);

    @Query("SELECT * FROM photo_entries WHERE project_id = :projectId "
            + "ORDER BY captured_at ASC, display_order ASC")
    List<PhotoEntry> getForProjectOrderedBlocking(long projectId);

    @Query("SELECT COUNT(*) FROM photo_entries WHERE project_id = :projectId")
    LiveData<Integer> countForProject(long projectId);

    @Query("SELECT COALESCE(MAX(display_order), 0) FROM photo_entries WHERE project_id = :projectId")
    int maxDisplayOrder(long projectId);

    @Query("SELECT COUNT(*) FROM photo_entries")
    int countBlocking();

    @Query("SELECT image_path FROM photo_entries")
    List<String> allImagePathsBlocking();

    @Query("SELECT thumbnail_path FROM photo_entries WHERE thumbnail_path IS NOT NULL")
    List<String> allThumbnailPathsBlocking();

    @Query("UPDATE photo_entries SET caption = :caption, notes = :notes, captured_at = :capturedAt "
            + "WHERE id = :id")
    void updateDetails(long id, String caption, String notes, long capturedAt);
}
