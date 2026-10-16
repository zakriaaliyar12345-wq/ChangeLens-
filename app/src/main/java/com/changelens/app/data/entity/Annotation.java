package com.changelens.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** A text marker pinned onto a photo; coordinates are normalized 0..1. */
@Entity(
        tableName = "annotations",
        foreignKeys = @ForeignKey(
                entity = PhotoEntry.class,
                parentColumns = "id",
                childColumns = "photo_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("photo_id")}
)
public class Annotation {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "photo_id")
    public long photoId;

    @ColumnInfo(name = "type")
    public int type = TYPE_TEXT;

    public static final int TYPE_TEXT = 0;

    @NonNull
    @ColumnInfo(name = "text")
    public String text = "";

    /** Normalized X coordinate in [0, 1]. */
    @ColumnInfo(name = "norm_x")
    public float normX;

    /** Normalized Y coordinate in [0, 1]. */
    @ColumnInfo(name = "norm_y")
    public float normY;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
