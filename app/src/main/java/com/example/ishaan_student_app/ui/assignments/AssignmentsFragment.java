package com.example.ishaan_student_app.ui.assignments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.AcademicPeriod;
import com.example.ishaan_student_app.data.model.Assignment;
import com.example.ishaan_student_app.data.model.PersonalSubject;
import com.example.ishaan_student_app.data.repository.AcademicPeriodRepository;
import com.example.ishaan_student_app.data.repository.AssignmentRepository;
import com.example.ishaan_student_app.data.repository.PersonalSubjectRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.util.AppExecutors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AssignmentsFragment extends Fragment {

    private Button btnAddAssignment;
    private TextView tvEmpty;
    private RecyclerView rvAssignments;
    private Spinner spinnerPeriods;

    private final ArrayList<Assignment> assignmentsList = new ArrayList<>();
    private AssignmentsAdapter adapter;
    private AssignmentRepository repository;
    private AcademicPeriodRepository periodRepository;
    private PersonalSubjectRepository subjectRepository;
    private SessionManager sessionManager;
    private final ArrayList<AcademicPeriod> periods = new ArrayList<>();
    private AcademicPeriod selectedPeriod;
    private boolean bindingPeriods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_assignments, container, false);

        repository = new AssignmentRepository(requireContext());
        periodRepository = new AcademicPeriodRepository(requireContext());
        subjectRepository = new PersonalSubjectRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        btnAddAssignment = root.findViewById(R.id.btn_add_assignment_dialog);
        tvEmpty = root.findViewById(R.id.tv_assignments_empty);
        rvAssignments = root.findViewById(R.id.rv_assignments);
        spinnerPeriods = root.findViewById(R.id.spinner_assignment_periods);

        setupRecyclerView();
        setupListeners();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPeriods();
    }

    private void setupRecyclerView() {
        adapter = new AssignmentsAdapter(
                assignmentsList,
                this::toggleAssignmentCompletion,
                this::confirmDeleteAssignment
        );

        rvAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAssignments.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddAssignment.setOnClickListener(v -> showAddAssignmentDialog());
        spinnerPeriods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (bindingPeriods || position < 0 || position >= periods.size()) return;
                selectedPeriod = periods.get(position);
                String sapId = sessionManager.getSapId();
                long periodId = selectedPeriod.getId();
                AppExecutors.getInstance().diskIO().execute(() -> {
                    periodRepository.setActivePeriod(sapId, periodId);
                    AppExecutors.getInstance().mainThread().execute(() -> { if (isAdded()) loadAssignments(); });
                });
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadPeriods() {
        String sapId = sessionManager.getSapId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<AcademicPeriod> rows = periodRepository.getPeriodsForAccount(sapId);
            AcademicPeriod active = periodRepository.getActivePeriodForAccount(sapId);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                periods.clear();
                if (rows != null) periods.addAll(rows);
                List<String> names = new ArrayList<>();
                int selected = 0;
                for (int i = 0; i < periods.size(); i++) {
                    AcademicPeriod period = periods.get(i);
                    names.add(period.getDisplayName());
                    if (active != null && period.getId() == active.getId()) selected = i;
                }
                if (names.isEmpty()) names.add(getString(R.string.attendance_no_period_selected));
                selectedPeriod = periods.isEmpty() ? null : periods.get(selected);
                ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, names);
                periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                bindingPeriods = true;
                spinnerPeriods.setAdapter(periodAdapter);
                if (!periods.isEmpty()) spinnerPeriods.setSelection(selected, false);
                bindingPeriods = false;
                loadAssignments();
            });
        });
    }

    private void loadAssignments() {
        String sapId = sessionManager.getSapId();
        long periodId = selectedPeriod == null ? 0 : selectedPeriod.getId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Assignment> list = periodId > 0 ? repository.getAssignmentsForPeriod(sapId, periodId) : new ArrayList<>();
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }
                assignmentsList.clear();
                if (list != null) {
                    assignmentsList.addAll(list);
                }
                adapter.notifyDataSetChanged();
                checkEmptyState();
            });
        });
    }

    private void toggleAssignmentCompletion(Assignment assignment, boolean isCompleted) {
        if (assignment == null) {
            return;
        }

        String sapId = sessionManager.getSapId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean updated = repository.updateAssignmentCompletion(sapId, assignment.getId(), isCompleted);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }
                if (updated) {
                    loadAssignments();
                } else {
                    Toast.makeText(requireContext(), "Failed to update completion status", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showAddAssignmentDialog() {
        if (selectedPeriod == null) {
            Toast.makeText(requireContext(), R.string.assignment_create_semester_first, Toast.LENGTH_SHORT).show();
            return;
        }
        long assignmentPeriodId = selectedPeriod.getId();
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_assignment, null);

        EditText etTitle = dialogView.findViewById(R.id.et_dialog_assignment_title);
        Spinner spinnerSubject = dialogView.findViewById(R.id.spinner_dialog_assignment_subject);
        TextView subjectsHint = dialogView.findViewById(R.id.tv_dialog_subjects_hint);
        Button btnDueDate = dialogView.findViewById(R.id.btn_dialog_due_date_picker);
        CheckBox cbCompleted = dialogView.findViewById(R.id.cb_dialog_completed);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_dialog_save_assignment);

        String sapId = sessionManager.getSapId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<PersonalSubject> periodSubjects = subjectRepository.getSubjectsForPeriod(sapId, assignmentPeriodId);

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }

                List<String> subjectOptions = new ArrayList<>();
                subjectOptions.add(getString(R.string.select_subject_prompt));
                if (periodSubjects != null) {
                    for (PersonalSubject subject : periodSubjects) {
                        if (!subject.isArchived()) {
                            subjectOptions.add(subject.getDisplayName());
                        }
                    }
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, subjectOptions);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSubject.setAdapter(spinnerAdapter);
                boolean hasSubjects = subjectOptions.size() > 1;
                spinnerSubject.setEnabled(hasSubjects);
                subjectsHint.setVisibility(hasSubjects ? View.GONE : View.VISIBLE);
            });
        });

        final String[] dialogSelectedDate = {""};

        btnDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        dialogSelectedDate[0] = String.format(Locale.US, "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
                        btnDueDate.setText(dialogSelectedDate[0]);
                    }, year, month, day);
            dialog.show();
        });

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("Title required");
                return;
            }

            if (spinnerSubject.getSelectedItemPosition() == 0 || spinnerSubject.getSelectedItem() == null) {
                Toast.makeText(requireContext(), "Please select a subject or add subjects first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dialogSelectedDate[0].isEmpty()) {
                Toast.makeText(requireContext(), "Please select a due date", Toast.LENGTH_SHORT).show();
                return;
            }

            String subject = spinnerSubject.getSelectedItem().toString();
            boolean isCompleted = cbCompleted.isChecked();

            RadioGroup rgPriority = dialogView.findViewById(R.id.rg_assignment_priority);
            String priority = "MEDIUM";
            if (rgPriority != null) {
                int checkedPriorityId = rgPriority.getCheckedRadioButtonId();
                if (checkedPriorityId == R.id.rb_priority_low) {
                    priority = "LOW";
                } else if (checkedPriorityId == R.id.rb_priority_high) {
                    priority = "HIGH";
                }
            }

            final String finalPriority = priority;
            btnSave.setEnabled(false);

            AppExecutors.getInstance().diskIO().execute(() -> {
                Assignment newAssignment = new Assignment(0, title, subject, dialogSelectedDate[0], isCompleted, finalPriority, assignmentPeriodId, 0, 0);
                long rowId = repository.saveAssignment(sapId, newAssignment, assignmentPeriodId);

                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    btnSave.setEnabled(true);

                    if (rowId > 0) {
                        Toast.makeText(requireContext(), "Assignment saved", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        loadAssignments();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save assignment. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        alertDialog.show();
    }

    private void confirmDeleteAssignment(Assignment assignment) {
        if (assignment == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Assignment")
                .setMessage("Delete assignment '" + assignment.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAssignment(assignment.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAssignment(long id) {
        String sapId = sessionManager.getSapId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean deleted = repository.deleteAssignment(sapId, id);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }
                if (deleted) {
                    Toast.makeText(requireContext(), "Assignment deleted", Toast.LENGTH_SHORT).show();
                    loadAssignments();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete assignment", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void checkEmptyState() {
        if (assignmentsList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvAssignments.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvAssignments.setVisibility(View.VISIBLE);
        }
    }
}
