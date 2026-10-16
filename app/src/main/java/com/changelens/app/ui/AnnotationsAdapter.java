package com.changelens.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.changelens.app.R;
import com.changelens.app.data.entity.Annotation;
import com.changelens.app.databinding.ItemAnnotationBinding;

/** Lists annotations for the compared photo with delete actions. */
public class AnnotationsAdapter extends ListAdapter<Annotation, AnnotationsAdapter.AnnotationViewHolder> {

    public interface Listener {
        void onDeleteAnnotation(Annotation annotation);
    }

    private final Listener listener;

    public AnnotationsAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Annotation> DIFF =
            new DiffUtil.ItemCallback<Annotation>() {
                @Override
                public boolean areItemsTheSame(@NonNull Annotation a, @NonNull Annotation b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Annotation a, @NonNull Annotation b) {
                    return a.text.equals(b.text)
                            && a.normX == b.normX
                            && a.normY == b.normY;
                }
            };

    @NonNull
    @Override
    public AnnotationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAnnotationBinding binding = ItemAnnotationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AnnotationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnotationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class AnnotationViewHolder extends RecyclerView.ViewHolder {

        private final ItemAnnotationBinding binding;

        AnnotationViewHolder(ItemAnnotationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Annotation annotation) {
            binding.annotationText.setText(annotation.text);
            binding.deleteAnnotation.setOnClickListener(v -> listener.onDeleteAnnotation(annotation));
        }
    }
}
