package com.changelens.app.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** A documentable project (renovation, plant growth, restoration, …). */
@Entity(
        tableName = "projects",
        indices = {@Index("created_at"), @Index("archived")}
)
public class Project {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title = "";

    @NonNull
    @ColumnInfo(name = "description")
    public String description = "";

    @NonNull
    @ColumnInfo(name = "category")
    public String category = "";

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "start_date")
    public long startDate;

    @ColumnInfo(name = "target_date")
    public Long targetDate;

    @ColumnInfo(name = "cover_image")
    @Nullable
    public String coverImage;

    @ColumnInfo(name = "archived")
    public boolean archived;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
