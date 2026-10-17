package com.example.ishaan_student_app.ui.events;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.StudentEvent;
import com.example.ishaan_student_app.data.repository.StudentContentRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.util.AppExecutors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsFragment extends Fragment {
    private final ArrayList<StudentEvent> events = new ArrayList<>();
    private EventsAdapter adapter;
    private StudentContentRepository repository;
    private SessionManager sessionManager;
    private TextView emptyState;
    private ImageView dialogImage;
    private Button imageButton;
    private Uri chosenImageUri;
    private String chosenDate = "";
    private AlertDialog addDialog;

    private final ActivityResultLauncher<PickVisualMediaRequest> imagePicker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null || !isAdded()) return;
                try { requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (SecurityException ignored) { }
                chosenImageUri = uri;
                if (dialogImage != null) {
                    dialogImage.setImageURI(uri);
                    dialogImage.setVisibility(View.VISIBLE);
                }
                if (imageButton != null) imageButton.setText(R.string.event_image_selected);
            });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                                  @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        repository = new StudentContentRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        RecyclerView list = root.findViewById(R.id.rv_events);
        emptyState = root.findViewById(R.id.tv_events_empty);
        root.findViewById(R.id.btn_add_event).setOnClickListener(v -> showAddEventDialog());
        adapter = new EventsAdapter(events, new EventsAdapter.Listener() {
            @Override public void onDetails(StudentEvent event) { showEventDetails(event); }
            @Override public void onDelete(StudentEvent event) { confirmDeleteEvent(event); }
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        return root;
    }

    @Override public void onResume() { super.onResume(); loadEvents(); }

    private void loadEvents() {
        String sapId = sessionManager.getSapId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<StudentEvent> rows = repository.getEvents(sapId);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                events.clear();
                events.addAll(rows);
                adapter.notifyDataSetChanged();
                emptyState.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showAddEventDialog() {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_event, null);
        EditText title = content.findViewById(R.id.et_event_title);
        EditText venue = content.findViewById(R.id.et_event_venue);
        EditText details = content.findViewById(R.id.et_event_details);
        Button dateButton = content.findViewById(R.id.btn_event_date);
        imageButton = content.findViewById(R.id.btn_event_image);
        dialogImage = content.findViewById(R.id.img_event_preview);
        chosenDate = "";
        chosenImageUri = null;

        dateButton.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                chosenDate = String.format(Locale.US, "%02d-%02d-%04d", day, month + 1, year);
                dateButton.setText(chosenDate);
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
        imageButton.setOnClickListener(v -> imagePicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        addDialog = new AlertDialog.Builder(requireContext()).setView(content).create();
        content.findViewById(R.id.btn_event_cancel).setOnClickListener(v -> addDialog.dismiss());
        content.findViewById(R.id.btn_event_save).setOnClickListener(v -> {
            String eventTitle = title.getText().toString().trim();
            String eventVenue = venue.getText().toString().trim();
            String eventDetails = details.getText().toString().trim();
            if (eventTitle.isEmpty()) { title.setError(getString(R.string.event_name_required)); return; }
            if (chosenDate.isEmpty()) { Toast.makeText(requireContext(), R.string.event_date_required, Toast.LENGTH_SHORT).show(); return; }
            if (isDateInPast(chosenDate)) { Toast.makeText(requireContext(), "Event date cannot be in the past.", Toast.LENGTH_SHORT).show(); return; }
            if (eventVenue.isEmpty()) { venue.setError(getString(R.string.event_venue_required)); return; }
            String imageUri = chosenImageUri == null ? "" : chosenImageUri.toString();
            String selectedEventDate = chosenDate;
            String sapId = sessionManager.getSapId();
            View saveButton = content.findViewById(R.id.btn_event_save);
            saveButton.setEnabled(false);
            AppExecutors.getInstance().diskIO().execute(() -> {
                long id = repository.addEvent(sapId, eventTitle, selectedEventDate, eventVenue, eventDetails, imageUri);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) return;
                    saveButton.setEnabled(true);
                    if (id > 0) { addDialog.dismiss(); loadEvents(); }
                    else Toast.makeText(requireContext(), R.string.event_save_failed, Toast.LENGTH_SHORT).show();
                });
            });
        });
        addDialog.show();
    }

    private boolean isDateInPast(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) return false;

            Calendar calToday = Calendar.getInstance();
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            Calendar calEvent = Calendar.getInstance();
            calEvent.setTime(date);
            calEvent.set(Calendar.HOUR_OF_DAY, 0);
            calEvent.set(Calendar.MINUTE, 0);
            calEvent.set(Calendar.SECOND, 0);
            calEvent.set(Calendar.MILLISECOND, 0);

            return calEvent.before(calToday);
        } catch (Exception e) {
            return false;
        }
    }

    private void showEventDetails(StudentEvent event) {
        String message = getString(R.string.event_details_format, event.getDate(), event.getVenue(),
                event.getDetails().isEmpty() ? getString(R.string.event_no_extra_details) : event.getDetails());
        new AlertDialog.Builder(requireContext()).setTitle(event.getTitle()).setMessage(message)
                .setNeutralButton(R.string.action_remove, (dialog, which) -> confirmDeleteEvent(event))
                .setPositiveButton(R.string.action_close, null).show();
    }

    private void confirmDeleteEvent(StudentEvent event) {
        new AlertDialog.Builder(requireContext()).setTitle(R.string.event_remove_title)
                .setMessage(getString(R.string.event_remove_message, event.getTitle()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                    String sapId = sessionManager.getSapId();
                    android.content.ContentResolver resolver = requireContext().getContentResolver();
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        boolean removed = repository.deleteEvent(sapId, event.getId());
                        if (removed && !event.getImageUri().isEmpty()) {
                            try {
                                resolver.releasePersistableUriPermission(
                                        Uri.parse(event.getImageUri()), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) { }
                        }
                        AppExecutors.getInstance().mainThread().execute(() -> {
                            if (!isAdded()) return;
                            if (removed) loadEvents();
                            else Toast.makeText(requireContext(), R.string.event_remove_failed, Toast.LENGTH_SHORT).show();
                        });
                    });
                }).show();
    }
}
