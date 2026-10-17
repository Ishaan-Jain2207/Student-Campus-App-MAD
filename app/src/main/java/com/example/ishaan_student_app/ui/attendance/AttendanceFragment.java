package com.example.ishaan_student_app.ui.attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.AcademicPeriod;
import com.example.ishaan_student_app.data.model.AttendanceEntry;
import com.example.ishaan_student_app.data.model.PersonalSubject;
import com.example.ishaan_student_app.data.model.Student;
import com.example.ishaan_student_app.data.model.SubjectAttendanceSummary;
import com.example.ishaan_student_app.data.repository.AcademicPeriodRepository;
import com.example.ishaan_student_app.data.repository.AttendanceEntryRepository;
import com.example.ishaan_student_app.data.repository.PersonalSubjectRepository;
import com.example.ishaan_student_app.data.repository.StudentRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.util.AppExecutors;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@AndroidEntryPoint
public class AttendanceFragment extends Fragment {

    private Spinner spinnerPeriods;
    private Button btnManagePeriods;
    private Button btnAddSubject;
    private TextView tvOverallPct;
    private TextView tvOverallSessions;
    private TextView tvEmpty;
    private RecyclerView rvSubjects;

    private final ArrayList<SubjectAttendanceSummary> subjectsList = new ArrayList<>();
    private final ArrayList<AcademicPeriod> periodsList = new ArrayList<>();

    private SubjectAttendanceAdapter adapter;

    @Inject StudentRepository studentRepository;
    @Inject AcademicPeriodRepository periodRepository;
    @Inject PersonalSubjectRepository personalSubjectRepository;
    @Inject AttendanceEntryRepository entryRepository;
    @Inject SessionManager sessionManager;

    private AcademicPeriod selectedPeriod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_attendance, container, false);

        initViews(root);
        setupRecyclerView();
        setupListeners();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPeriodsAndAttendance();
    }

    private void initViews(View root) {
        spinnerPeriods = root.findViewById(R.id.spinner_attendance_periods);
        btnManagePeriods = root.findViewById(R.id.btn_manage_periods);
        btnAddSubject = root.findViewById(R.id.btn_add_personal_subject);
        tvOverallPct = root.findViewById(R.id.tv_overall_attendance_pct);
        tvOverallSessions = root.findViewById(R.id.tv_overall_sessions_count);
        tvEmpty = root.findViewById(R.id.tv_attendance_empty);
        rvSubjects = root.findViewById(R.id.rv_attendance_subjects);
    }

    private void setupRecyclerView() {
        adapter = new SubjectAttendanceAdapter(subjectsList, new SubjectAttendanceAdapter.SessionActionListener() {
            @Override
            public void onAddSession(SubjectAttendanceSummary subject, boolean attended) {
                addQuickSession(subject, attended);
            }

            @Override
            public void onRemoveSession(SubjectAttendanceSummary subject, boolean attended) {
                confirmRemoveLatestSession(subject, attended);
            }
        });
        rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubjects.setAdapter(adapter);
    }

    private void setupListeners() {
        btnManagePeriods.setOnClickListener(v -> showManagePeriodDialog());
        btnAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        spinnerPeriods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < periodsList.size()) {
                    selectedPeriod = periodsList.get(position);
                    String sapId = sessionManager.getSapId();
                    long periodId = selectedPeriod.getId();
                    AppExecutors.getInstance().diskIO().execute(() -> periodRepository.setActivePeriod(sapId, periodId));
                    loadAttendanceForSelectedPeriod();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadPeriodsAndAttendance() {
        String sapId = sessionManager.getSapId();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<AcademicPeriod> periods = periodRepository.getPeriodsForAccount(sapId);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }

                periodsList.clear();
                List<String> periodNames = new ArrayList<>();

                if (periods != null && !periods.isEmpty()) {
                    periodsList.addAll(periods);
                    for (AcademicPeriod p : periods) {
                        periodNames.add(p.getDisplayName());
                    }
                    selectedPeriod = periodsList.get(0);
                } else {
                    periodNames.add(getString(R.string.attendance_no_period_selected));
                    selectedPeriod = null;
                }

                ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, periodNames);
                periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPeriods.setAdapter(periodAdapter);

                loadAttendanceForSelectedPeriod();
            });
        });
    }

    private void loadAttendanceForSelectedPeriod() {
        String sapId = sessionManager.getSapId();
        long selectedPeriodId = selectedPeriod != null ? selectedPeriod.getId() : -1;
        AppExecutors.getInstance().diskIO().execute(() -> {
            Student student = studentRepository.getStudentBySapId(sapId);
            String dept = student != null && student.getDepartment() != null ? student.getDepartment().trim() : "";
            ArrayList<SubjectAttendanceSummary> loadedSubjects = new ArrayList<>();

            if (selectedPeriodId > 0) {
                List<PersonalSubject> personalSubjects = personalSubjectRepository.getSubjectsForPeriod(sapId, selectedPeriodId);
                if (personalSubjects != null) {
                    for (PersonalSubject ps : personalSubjects) {
                        int present = entryRepository.getPresentCountForSubject(sapId, ps.getId());
                        int absent = entryRepository.getAbsentCountForSubject(sapId, ps.getId());
                        int conducted = present + absent;

                        loadedSubjects.add(new SubjectAttendanceSummary(
                                String.valueOf(ps.getId()), ps.getSubjectName(), ps.getSubjectCode(), dept, 1, conducted, present));
                    }
                }
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }

                subjectsList.clear();
                subjectsList.addAll(loadedSubjects);

                adapter.notifyDataSetChanged();

                if (subjectsList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvSubjects.setVisibility(View.GONE);
                    tvOverallPct.setText(R.string.attendance_no_percentage);
                    tvOverallSessions.setText(R.string.attendance_zero_sessions);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvSubjects.setVisibility(View.VISIBLE);

                    int totalConducted = 0;
                    int totalAttended = 0;

                    for (SubjectAttendanceSummary sub : subjectsList) {
                        totalConducted += sub.getTotalConductedSessions();
                        totalAttended += sub.getTotalAttendedSessions();
                    }

                    if (totalConducted > 0) {
                        double overallPct = ((double) totalAttended * 100.0) / totalConducted;
                        tvOverallPct.setText(String.format(Locale.US, "%.1f%%", overallPct));
                        tvOverallSessions.setText(getResources().getQuantityString(
                                R.plurals.attendance_sessions_count, totalConducted,
                                totalAttended, totalConducted));
                    } else {
                        tvOverallPct.setText(R.string.attendance_no_percentage);
                        tvOverallSessions.setText(R.string.attendance_zero_sessions);
                    }
                }
            });
        });
    }

    private void addQuickSession(SubjectAttendanceSummary subject, boolean attended) {
        if (!isAdded() || subject == null || selectedPeriod == null) {
            return;
        }
        String sapId = sessionManager.getSapId();
        if (sapId == null || sapId.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.attendance_sign_in_again, Toast.LENGTH_SHORT).show();
            return;
        }
        long subjectId;
        try {
            subjectId = Long.parseLong(subject.getSubjectId());
        } catch (NumberFormatException exception) {
            Toast.makeText(requireContext(), "This subject could not be opened", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new java.util.Date());
        AttendanceEntry entry = new AttendanceEntry(0, sapId, subjectId, date, "", "", attended ? "PRESENT" : "ABSENT", "", 0, 0);

        AppExecutors.getInstance().diskIO().execute(() -> {
            long entryId = entryRepository.saveOrUpdateEntry(entry);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                if (entryId <= 0) {
                    Toast.makeText(requireContext(), "Could not save this class", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadAttendanceForSelectedPeriod();
                String label = attended ? "Attended class added" : "Missed class added";
                Snackbar.make(rvSubjects, label, Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> AppExecutors.getInstance().diskIO().execute(() -> {
                            boolean removed = entryRepository.deleteEntry(sapId, entryId);
                            AppExecutors.getInstance().mainThread().execute(() -> {
                                if (!isAdded()) return;
                                loadAttendanceForSelectedPeriod();
                                if (!removed) Toast.makeText(requireContext(), "Could not undo that entry", Toast.LENGTH_SHORT).show();
                            });
                        }))
                        .show();
            });
        });
    }

    private void confirmRemoveLatestSession(SubjectAttendanceSummary subject, boolean attended) {
        if (!isAdded() || subject == null) return;
        String sapId = sessionManager.getSapId();
        long subjectId;
        try {
            subjectId = Long.parseLong(subject.getSubjectId());
        } catch (NumberFormatException exception) {
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<AttendanceEntry> entries = entryRepository.getEntriesForSubject(sapId, subjectId);
            AttendanceEntry latest = null;
            for (AttendanceEntry entry : entries) {
                if ((attended && entry.isPresent()) || (!attended && entry.isAbsent())) {
                    latest = entry;
                    break;
                }
            }
            AttendanceEntry selectedEntry = latest;
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                if (selectedEntry == null) {
                    Toast.makeText(requireContext(), attended ? "No attended class to remove" : "No missed class to remove", Toast.LENGTH_SHORT).show();
                    return;
                }
                String kind = attended ? "attended" : "missed";
                String message = "Remove the latest " + kind + " class you logged on " + selectedEntry.getClassDate()
                        + "? This changes only your personal record.";
                new AlertDialog.Builder(requireContext())
                        .setTitle("Remove class record?")
                        .setMessage(message)
                        .setNegativeButton("Keep", null)
                        .setPositiveButton("Remove", (dialog, which) -> AppExecutors.getInstance().diskIO().execute(() -> {
                            boolean removed = entryRepository.deleteEntry(sapId, selectedEntry.getId());
                            AppExecutors.getInstance().mainThread().execute(() -> {
                                if (!isAdded()) return;
                                loadAttendanceForSelectedPeriod();
                                if (!removed) Toast.makeText(requireContext(), "That class could not be removed", Toast.LENGTH_SHORT).show();
                            });
                        }))
                        .show();
            });
        });
    }

    private void showManagePeriodDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_period, null);

        EditText etYear = dialogView.findViewById(R.id.et_period_year);
        EditText etSemester = dialogView.findViewById(R.id.et_period_semester);
        Button btnCancel = dialogView.findViewById(R.id.btn_period_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_period_save);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String year = etYear.getText().toString().trim();
            String semester = etSemester.getText().toString().trim();

            if (year.isEmpty()) {
                etYear.setError("Academic year required (e.g. 2026-27)");
                return;
            }
            if (semester.isEmpty()) {
                etSemester.setError("Semester required");
                return;
            }

            String sapId = sessionManager.getSapId();
            AcademicPeriod newPeriod = new AcademicPeriod(0, sapId, year, semester, "", "", true, false, 0, 0);

            btnSave.setEnabled(false);

            AppExecutors.getInstance().diskIO().execute(() -> {
                long periodId = periodRepository.saveOrUpdatePeriod(newPeriod);
                if (periodId > 0) {
                    periodRepository.setActivePeriod(sapId, periodId);
                }

                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    btnSave.setEnabled(true);
                    if (periodId > 0) {
                        Toast.makeText(requireContext(), "Semester saved", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        loadPeriodsAndAttendance();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save semester", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        alertDialog.show();
    }

    private void showAddSubjectDialog() {
        if (selectedPeriod == null) {
            Toast.makeText(requireContext(), "Please create a semester period first", Toast.LENGTH_SHORT).show();
            showManagePeriodDialog();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_personal_subject, null);

        EditText etName = dialogView.findViewById(R.id.et_subject_name);
        EditText etCode = dialogView.findViewById(R.id.et_subject_code);
        Button btnCancel = dialogView.findViewById(R.id.btn_subject_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_subject_save);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String code = etCode.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Subject name required");
                return;
            }

            String sapId = sessionManager.getSapId();
            PersonalSubject newSubject = new PersonalSubject(0, sapId, selectedPeriod.getId(), name, code, false, 0, 0);

            btnSave.setEnabled(false);

            AppExecutors.getInstance().diskIO().execute(() -> {
                long subjectId = personalSubjectRepository.saveOrUpdateSubject(newSubject);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    btnSave.setEnabled(true);
                    if (subjectId > 0) {
                        Toast.makeText(requireContext(), "Subject added", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        loadAttendanceForSelectedPeriod();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add subject", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        alertDialog.show();
    }

}
