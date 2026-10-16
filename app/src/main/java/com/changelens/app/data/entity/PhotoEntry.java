package com.changelens.app.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** One photographed moment in a project's timeline. */
@Entity(
        tableName = "photo_entries",
        foreignKeys = @ForeignKey(
                entity = Project.class,
                parentColumns = "id",
                childColumns = "project_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("project_id"), @Index(value = {"project_id", "display_order"})}
)
public class PhotoEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "project_id")
    public long projectId;

    /** Absolute path of the managed original image copy. */
    @NonNull
    @ColumnInfo(name = "image_path")
    public String imagePath = "";

    /** Absolute path of the generated thumbnail, if any. */
    @ColumnInfo(name = "thumbnail_path")
    @Nullable
    public String thumbnailPath;

    @ColumnInfo(name = "captured_at")
    public long capturedAt;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @NonNull
    @ColumnInfo(name = "caption")
    public String caption = "";

    @NonNull
    @ColumnInfo(name = "notes")
    public String notes = "";

    @ColumnInfo(name = "display_order")
    public int displayOrder;
}
