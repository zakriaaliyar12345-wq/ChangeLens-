package com.changelens.app.ui;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.changelens.app.R;
import com.changelens.app.data.entity.Project;
import com.changelens.app.databinding.ItemProjectBinding;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.Formatters;
import com.changelens.app.util.ImageStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RecyclerView adapter for dashboard project cards (topic 04.5). */
public class ProjectsAdapter extends ListAdapter<Project, ProjectsAdapter.ProjectViewHolder> {

    public interface Listener {
        void onProjectClicked(Project project);

        void onEditProject(Project project);

        void onToggleArchive(Project project);

        void onDeleteProject(Project project);
    }

    private final Listener listener;
    private final Map<String, Bitmap> thumbCache = new HashMap<>();

    public ProjectsAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Project> DIFF =
            new DiffUtil.ItemCallback<Project>() {
                @Override
                public boolean areItemsTheSame(@NonNull Project a, @NonNull Project b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Project a, @NonNull Project b) {
                    return a.updatedAt == b.updatedAt
                            && a.archived == b.archived
                            && a.title.equals(b.title)
                            && a.description.equals(b.description)
                            && a.category.equals(b.category)
                            && java.util.Objects.equals(a.coverImage, b.coverImage)
                            && a.startDate == b.startDate
                            && java.util.Objects.equals(a.targetDate, b.targetDate);
                }
            };

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectBinding binding = ItemProjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {

        private final ItemProjectBinding binding;

        ProjectViewHolder(ItemProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Project project) {
            android.content.Context context = binding.getRoot().getContext();
            binding.projectTitle.setText(project.title);
            binding.projectCategory.setText(project.category.isEmpty()
                    ? context.getString(R.string.filter_all) : project.category);

            String cover = project.coverImage;
            if (cover != null && !cover.isEmpty()) {
                Bitmap cached = thumbCache.get(cover);
                if (cached != null && !cached.isRecycled()) {
                    binding.coverImage.setImageBitmap(cached);
                    binding.coverImage.setContentDescription(
                            context.getString(R.string.content_description_project_cover));
                } else {
                    binding.coverImage.setImageResource(R.drawable.ic_picture);
                    AppExecutors.get().diskIO().execute(() -> {
                        Bitmap bm = ImageStore.decodeSampled(cover,
                                ImageStore.THUMBNAIL_MAX, ImageStore.THUMBNAIL_MAX);
                        if (bm != null) {
                            AppExecutors.get().main(() -> {
                                thumbCache.put(cover, bm);
                                List<Project> current = getCurrentList();
                                int index = current.indexOf(project);
                                if (index >= 0) {
                                    notifyItemChanged(index);
                                }
                            });
                        }
                    });
                }
            } else {
                binding.coverImage.setImageResource(R.drawable.ic_picture);
                binding.coverImage.setContentDescription(
                        context.getString(R.string.content_description_no_cover));
            }

            binding.getRoot().setOnClickListener(v -> listener.onProjectClicked(project));
            binding.overflowButton.setOnClickListener(v -> showMenu(v, project));
        }

        private void showMenu(View anchor, Project project) {
            android.content.Context context = anchor.getContext();
            PopupMenu menu = new PopupMenu(context, anchor);
            menu.getMenu().add(0, 1, 0, context.getString(R.string.edit));
            menu.getMenu().add(0, 2, 1, project.archived
                    ? context.getString(R.string.unarchive_project)
                    : context.getString(R.string.archive_project));
            menu.getMenu().add(0, 3, 2, context.getString(R.string.delete));
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        listener.onEditProject(project);
                        return true;
                    case 2:
                        listener.onToggleArchive(project);
                        return true;
                    case 3:
                        listener.onDeleteProject(project);
                        return true;
                    default:
                        return false;
                }
            });
            menu.show();
        }
    }
}
