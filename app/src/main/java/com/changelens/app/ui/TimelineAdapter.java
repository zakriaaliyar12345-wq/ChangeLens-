package com.changelens.app.ui;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.changelens.app.R;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.databinding.ItemPhotoBinding;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.Formatters;
import com.changelens.app.util.ImageStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Chronological photo timeline with multi-select support (topic 04.5). */
public class TimelineAdapter extends ListAdapter<PhotoEntry, TimelineAdapter.PhotoViewHolder> {

    public interface Listener {
        void onPhotoClicked(PhotoEntry entry, int position);

        void onPhotoLongClicked(PhotoEntry entry);

        void onSelectionChanged(int selectedCount);
    }

    private final Listener listener;
    private final Set<Long> selectedIds = new HashSet<>();
    private boolean selectionMode = false;
    private final Map<String, Bitmap> thumbCache = new HashMap<>();

    public TimelineAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PhotoEntry> DIFF =
            new DiffUtil.ItemCallback<PhotoEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull PhotoEntry a, @NonNull PhotoEntry b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull PhotoEntry a, @NonNull PhotoEntry b) {
                    return a.capturedAt == b.capturedAt
                            && a.displayOrder == b.displayOrder
                            && a.caption.equals(b.caption)
                            && a.notes.equals(b.notes)
                            && a.imagePath.equals(b.imagePath)
                            && a.thumbnailPath != null
                            && a.thumbnailPath.equals(b.thumbnailPath);
                }
            };

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhotoBinding binding = ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PhotoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        if (this.selectionMode != selectionMode) {
            this.selectionMode = selectionMode;
            if (!selectionMode) {
                selectedIds.clear();
            }
            notifyDataSetChanged();
            listener.onSelectionChanged(selectedCount());
        }
    }

    public int selectedCount() {
        return selectedIds.size();
    }

    /** Returns the two selected entries in timeline order, or null. */
    public java.util.List<PhotoEntry> getSelectedPhotos() {
        java.util.List<PhotoEntry> result = new java.util.ArrayList<>();
        for (PhotoEntry entry : getCurrentList()) {
            if (selectedIds.contains(entry.id)) {
                result.add(entry);
            }
        }
        return result;
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(0);
    }

    private void toggleSelection(PhotoEntry entry) {
        if (!selectionMode) {
            selectionMode = true;
        }
        if (selectedIds.contains(entry.id)) {
            selectedIds.remove(entry.id);
        } else {
            selectedIds.add(entry.id);
        }
        int index = getCurrentList().indexOf(entry);
        if (index >= 0) {
            notifyItemChanged(index);
        }
        listener.onSelectionChanged(selectedCount());
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        private final ItemPhotoBinding binding;

        PhotoViewHolder(ItemPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PhotoEntry entry) {
            android.content.Context context = binding.getRoot().getContext();
            binding.photoDate.setText(Formatters.formatDate(context, entry.capturedAt));
            binding.photoCaption.setText(entry.caption.isEmpty()
                    ? context.getString(R.string.notes_label) + ": " + entry.notes
                    : entry.caption);
            if (binding.photoCaption.getText().length() == 0) {
                binding.photoCaption.setVisibility(View.GONE);
            } else {
                binding.photoCaption.setVisibility(View.VISIBLE);
            }

            binding.selectCheck.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
            binding.selectCheck.setChecked(selectedIds.contains(entry.id));

            String path = entry.thumbnailPath != null ? entry.thumbnailPath : entry.imagePath;
            Bitmap cached = thumbCache.get(path);
            if (cached != null && !cached.isRecycled()) {
                binding.photoThumb.setImageBitmap(cached);
            } else {
                binding.photoThumb.setImageResource(R.drawable.ic_picture);
                final String cacheKey = path;
                AppExecutors.get().diskIO().execute(() -> {
                    Bitmap bm = ImageStore.decodeSampled(cacheKey,
                            ImageStore.THUMBNAIL_MAX, ImageStore.THUMBNAIL_MAX);
                    if (bm != null) {
                        AppExecutors.get().main(() -> {
                            thumbCache.put(cacheKey, bm);
                            int index = getCurrentList().indexOf(entry);
                            if (index >= 0) {
                                notifyItemChanged(index);
                            }
                        });
                    }
                });
            }

            binding.getRoot().setOnClickListener(v -> {
                if (selectionMode) {
                    toggleSelection(entry);
                } else {
                    listener.onPhotoClicked(entry, getAdapterPosition());
                }
            });
            binding.getRoot().setOnLongClickListener(v -> {
                if (!selectionMode) {
                    toggleSelection(entry);
                } else {
                    toggleSelection(entry);
                }
                return true;
            });
            binding.selectCheck.setOnClickListener(v -> toggleSelection(entry));
        }
    }
}
