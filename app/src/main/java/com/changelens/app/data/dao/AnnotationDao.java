package com.changelens.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.changelens.app.data.entity.Annotation;

import java.util.List;

@Dao
public interface AnnotationDao {

    @Insert
    long insert(Annotation annotation);

    @Query("DELETE FROM annotations WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM annotations WHERE photo_id = :photoId ORDER BY created_at ASC")
    LiveData<List<Annotation>> getForPhoto(long photoId);

    @Query("SELECT * FROM annotations WHERE photo_id = :photoId ORDER BY created_at ASC")
    List<Annotation> getForPhotoBlocking(long photoId);

    @Query("SELECT COUNT(*) FROM annotations")
    int countBlocking();
}
