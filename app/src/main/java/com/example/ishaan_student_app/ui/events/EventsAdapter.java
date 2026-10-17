package com.example.ishaan_student_app.ui.events;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.StudentEvent;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {
    public interface Listener { void onDetails(StudentEvent event); void onDelete(StudentEvent event); }
    private final List<StudentEvent> events;
    private final Listener listener;

    public EventsAdapter(List<StudentEvent> events, Listener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull @Override public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        StudentEvent event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.meta.setText(holder.itemView.getContext().getString(R.string.event_meta_format, event.getDate(), event.getVenue()));
        if (event.getImageUri().isEmpty()) holder.image.setImageResource(R.drawable.ic_logo);
        else {
            try { holder.image.setImageURI(Uri.parse(event.getImageUri())); }
            catch (Exception ignored) { holder.image.setImageResource(R.drawable.ic_logo); }
        }
        holder.details.setOnClickListener(v -> listener.onDetails(event));
        holder.itemView.setOnLongClickListener(v -> { listener.onDelete(event); return true; });
        holder.itemView.setContentDescription(holder.itemView.getContext().getString(
                R.string.event_card_description, event.getTitle(), event.getDate(), event.getVenue()));
    }

    @Override public int getItemCount() { return events.size(); }

    static class EventHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView meta;
        final MaterialButton details;
        EventHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_event);
            title = itemView.findViewById(R.id.tv_event_title);
            meta = itemView.findViewById(R.id.tv_event_meta);
            details = itemView.findViewById(R.id.btn_event_details);
        }
    }
}
