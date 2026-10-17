package com.example.ishaan_student_app.ui.resources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.CampusResource;

import java.util.List;

public class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.ResourceViewHolder> {

    public interface OnResourceClickListener {
        void onResourceClick(CampusResource resource);
    }

    public interface OnResourceRemoveListener { void onResourceRemove(CampusResource resource); }

    private final List<CampusResource> resourceList;
    private final OnResourceClickListener listener;
    private final OnResourceRemoveListener removeListener;

    public ResourcesAdapter(List<CampusResource> resourceList, OnResourceClickListener listener) {
        this(resourceList, listener, null);
    }

    public ResourcesAdapter(List<CampusResource> resourceList, OnResourceClickListener listener, OnResourceRemoveListener removeListener) {
        this.resourceList = resourceList;
        this.listener = listener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        CampusResource item = resourceList.get(position);
        holder.bind(item, listener, removeListener);
    }

    @Override
    public int getItemCount() {
        return resourceList != null ? resourceList.size() : 0;
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvDesc;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_resource_title);
            tvCategory = itemView.findViewById(R.id.tv_resource_category);
            tvDesc = itemView.findViewById(R.id.tv_resource_desc);
        }

        public void bind(CampusResource item, OnResourceClickListener listener, OnResourceRemoveListener removeListener) {
            tvTitle.setText(item.getTitle());
            tvCategory.setText(item.getCategory());
            tvDesc.setText(item.getDescription());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onResourceClick(item);
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (item.isUserAdded() && removeListener != null) {
                    removeListener.onResourceRemove(item);
                    return true;
                }
                return false;
            });
        }
    }
}
