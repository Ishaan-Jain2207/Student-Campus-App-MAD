package com.example.ishaan_student_app.ui.resources;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.AcademicPeriod;
import com.example.ishaan_student_app.data.model.CampusResource;
import com.example.ishaan_student_app.data.model.PersonalSubject;
import com.example.ishaan_student_app.data.model.UserResource;
import com.example.ishaan_student_app.data.repository.AcademicPeriodRepository;
import com.example.ishaan_student_app.data.repository.PersonalSubjectRepository;
import com.example.ishaan_student_app.data.repository.StudentContentRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.util.AppExecutors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResourcesFragment extends Fragment {

    private static final String TAG = "ResourcesFragment";

    private final ArrayList<CampusResource> resourceList = new ArrayList<>();
    private final ArrayList<String> subjectOptions = new ArrayList<>();

    private ResourcesAdapter adapter;
    private StudentContentRepository contentRepository;
    private AcademicPeriodRepository periodRepository;
    private PersonalSubjectRepository subjectRepository;
    private SessionManager sessionManager;

    private Spinner spinnerSubjectFilter;
    private View emptyState;

    private Uri pendingSelectedUri = null;
    private String pendingSelectedMimeType = "";
    private String pendingSelectedTitle = "";
    private TextView tvDialogSelectedFile;

    private final ActivityResultLauncher<String[]> filePicker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), this::onDocumentSelected);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_resources, container, false);

        RecyclerView rvResources = root.findViewById(R.id.rv_resources);
        Button addResource = root.findViewById(R.id.btn_add_resource);
        spinnerSubjectFilter = root.findViewById(R.id.spinner_resource_subject_filter);
        emptyState = root.findViewById(R.id.tv_resources_empty);

        contentRepository = new StudentContentRepository(requireContext());
        periodRepository = new AcademicPeriodRepository(requireContext());
        subjectRepository = new PersonalSubjectRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        adapter = new ResourcesAdapter(resourceList, this::openResource, this::confirmRemoveResource);
        rvResources.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResources.setAdapter(adapter);

        addResource.setOnClickListener(v -> showAddResourceDialog());

        spinnerSubjectFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadResources();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSubjectOptionsAndResources();
    }

    private void loadSubjectOptionsAndResources() {
        String sapId = sessionManager.getSapId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            AcademicPeriod activePeriod = periodRepository.getActivePeriodForAccount(sapId);
            List<PersonalSubject> subjects = activePeriod != null
                    ? subjectRepository.getSubjectsForPeriod(sapId, activePeriod.getId())
                    : new ArrayList<>();

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;

                subjectOptions.clear();
                subjectOptions.add("All Subjects");
                for (PersonalSubject ps : subjects) {
                    subjectOptions.add(ps.getSubjectName());
                }
                subjectOptions.add("General");

                ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, subjectOptions);
                filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSubjectFilter.setAdapter(filterAdapter);

                loadResources();
            });
        });
    }

    private void loadResources() {
        String sapId = sessionManager.getSapId();
        String selectedFilter = spinnerSubjectFilter.getSelectedItem() != null
                ? spinnerSubjectFilter.getSelectedItem().toString()
                : "All Subjects";

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<UserResource> saved = contentRepository.getResourcesBySubject(sapId, selectedFilter);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                resourceList.clear();
                for (UserResource item : saved) {
                    String categoryStr = item.getSubjectName() != null && !item.getSubjectName().isEmpty()
                            ? item.getSubjectName()
                            : "General";
                    resourceList.add(new CampusResource(
                            item.getTitle(), categoryStr, getString(R.string.resource_tap_to_open),
                            item.getId(), item.getUri(), item.getMimeType()
                    ));
                }
                adapter.notifyDataSetChanged();
                emptyState.setVisibility(resourceList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showAddResourceDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_resource, null);

        Spinner spinnerSubject = dialogView.findViewById(R.id.spinner_dialog_resource_subject);
        Button btnChooseFile = dialogView.findViewById(R.id.btn_dialog_choose_file);
        tvDialogSelectedFile = dialogView.findViewById(R.id.tv_dialog_selected_file);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_dialog_save_resource);

        pendingSelectedUri = null;
        pendingSelectedMimeType = "";
        pendingSelectedTitle = "";

        List<String> dialogSubjects = new ArrayList<>(subjectOptions);
        dialogSubjects.remove("All Subjects"); // Remove filter option for adding
        if (dialogSubjects.isEmpty()) {
            dialogSubjects.add("General");
        }

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, dialogSubjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        btnChooseFile.setOnClickListener(v -> filePicker.launch(new String[]{
                "application/pdf", "text/*", "text/plain", "text/csv",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            if (pendingSelectedUri == null) {
                Toast.makeText(requireContext(), "Please choose a document file first", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedSubject = spinnerSubject.getSelectedItem() != null
                    ? spinnerSubject.getSelectedItem().toString()
                    : "General";

            String sapId = sessionManager.getSapId();
            String title = pendingSelectedTitle.isEmpty() ? getDisplayName(pendingSelectedUri) : pendingSelectedTitle;
            String mimeType = pendingSelectedMimeType.isEmpty() ? "application/octet-stream" : pendingSelectedMimeType;
            String uriStr = pendingSelectedUri.toString();

            btnSave.setEnabled(false);
            AppExecutors.getInstance().diskIO().execute(() -> {
                long id = contentRepository.addResource(sapId, title, mimeType, uriStr, selectedSubject);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) return;
                    btnSave.setEnabled(true);
                    if (id > 0) {
                        dialog.dismiss();
                        loadResources();
                    } else {
                        Toast.makeText(requireContext(), R.string.resource_save_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    private void onDocumentSelected(Uri uri) {
        if (uri == null || !isAdded()) return;
        try {
            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some document providers grant a temporary read-only URI.
        }

        pendingSelectedUri = uri;
        pendingSelectedTitle = getDisplayName(uri);
        String mime = requireContext().getContentResolver().getType(uri);
        pendingSelectedMimeType = mime != null && !mime.trim().isEmpty() ? mime : "application/octet-stream";

        if (tvDialogSelectedFile != null) {
            tvDialogSelectedFile.setText("Selected File: " + pendingSelectedTitle);
            tvDialogSelectedFile.setTextColor(requireContext().getColor(R.color.accent_moss));
        }
    }

    private String getDisplayName(Uri uri) {
        try (Cursor cursor = requireContext().getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.trim().isEmpty()) return name.trim();
            }
        } catch (Exception ignored) {
        }
        String lastSegment = uri.getLastPathSegment();
        return lastSegment == null || lastSegment.trim().isEmpty() ? getString(R.string.resource_fallback_title) : lastSegment;
    }

    private void openResource(CampusResource resource) {
        if (resource == null) return;

        String uriStr = resource.getUri();
        String mimeType = resource.getMimeType();
        boolean isTextOrCsv = (mimeType != null && (mimeType.contains("text") || mimeType.contains("csv")))
                || (resource.getTitle().toLowerCase(Locale.ROOT).endsWith(".txt") || resource.getTitle().toLowerCase(Locale.ROOT).endsWith(".csv"));

        if (isTextOrCsv && !uriStr.isEmpty()) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                String content = readTextFileContent(Uri.parse(uriStr));
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) return;
                    new AlertDialog.Builder(requireContext())
                            .setTitle(resource.getTitle() + " (" + resource.getCategory() + ")")
                            .setMessage(content.isEmpty() ? "Empty file or could not read file content." : content)
                            .setNegativeButton(R.string.action_remove, (dialog, which) -> confirmRemoveResource(resource))
                            .setNeutralButton(R.string.action_open, (dialog, which) -> openUri(Uri.parse(resource.getUri()), resource.getMimeType()))
                            .setPositiveButton(R.string.action_close, null)
                            .show();
                });
            });
        } else {
            new AlertDialog.Builder(requireContext())
                    .setTitle(resource.getTitle())
                    .setMessage("Subject Category: " + resource.getCategory() + "\n\nTap 'Open' to view this document.")
                    .setNegativeButton(R.string.action_remove, (dialog, which) -> confirmRemoveResource(resource))
                    .setPositiveButton(R.string.action_open, (dialog, which) -> openUri(Uri.parse(resource.getUri()), resource.getMimeType()))
                    .show();
        }
    }

    private String readTextFileContent(Uri uri) {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 200) {
                sb.append(line).append("\n");
                lineCount++;
            }
            if (reader.readLine() != null) {
                sb.append("\n... [Content truncated for preview]");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading text file content: " + uri, e);
            return "";
        }
        return sb.toString().trim();
    }

    private void openUri(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(uri, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.resource_open_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmRemoveResource(CampusResource resource) {
        if (resource == null || !resource.isUserAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.resource_remove_title)
                .setMessage(getString(R.string.resource_remove_message, resource.getTitle()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                    String sapId = sessionManager.getSapId();
                    ContentResolver resolver = requireContext().getContentResolver();
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        contentRepository.deleteResource(sapId, resource.getId());
                        try {
                            resolver.releasePersistableUriPermission(
                                    Uri.parse(resource.getUri()), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {
                        }
                        AppExecutors.getInstance().mainThread().execute(() -> {
                            if (isAdded()) loadResources();
                        });
                    });
                }).show();
    }
}
