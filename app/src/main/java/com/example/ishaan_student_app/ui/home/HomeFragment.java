package com.example.ishaan_student_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.AcademicPeriod;
import com.example.ishaan_student_app.data.model.Assignment;
import com.example.ishaan_student_app.data.model.PersonalSubject;
import com.example.ishaan_student_app.data.model.Student;
import com.example.ishaan_student_app.data.repository.AcademicPeriodRepository;
import com.example.ishaan_student_app.data.repository.AssignmentRepository;
import com.example.ishaan_student_app.data.repository.AttendanceEntryRepository;
import com.example.ishaan_student_app.data.repository.PersonalSubjectRepository;
import com.example.ishaan_student_app.data.repository.StudentRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.ui.main.MainActivity;
import com.example.ishaan_student_app.util.AppExecutors;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private TextView tvNextUpTitle;
    private TextView tvNextUpDetail;
    private TextView tvNextUpEmpty;
    private TextView tvAttendanceCount;
    private TextView tvAssignmentsCount;

    private StudentRepository studentRepository;
    private AcademicPeriodRepository periodRepository;
    private PersonalSubjectRepository subjectRepository;
    private AttendanceEntryRepository attendanceRepository;
    private AssignmentRepository assignmentRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        studentRepository = new StudentRepository(requireContext());
        periodRepository = new AcademicPeriodRepository(requireContext());
        subjectRepository = new PersonalSubjectRepository(requireContext());
        attendanceRepository = new AttendanceEntryRepository(requireContext());
        assignmentRepository = new AssignmentRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        tvGreeting = root.findViewById(R.id.tv_home_greeting);
        tvNextUpTitle = root.findViewById(R.id.tv_home_next_up_title);
        tvNextUpDetail = root.findViewById(R.id.tv_home_next_up_detail);
        tvNextUpEmpty = root.findViewById(R.id.tv_home_next_up_empty);
        tvAttendanceCount = root.findViewById(R.id.tv_home_attendance_count);
        tvAssignmentsCount = root.findViewById(R.id.tv_home_assignments_count);

        root.findViewById(R.id.card_home_attendance_summary).setOnClickListener(v -> openTab(R.id.nav_attendance));
        root.findViewById(R.id.card_home_assignments_summary).setOnClickListener(v -> openTab(R.id.nav_assignments));
        root.findViewById(R.id.btn_home_events).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).openEvents();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void openTab(int tabId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToTab(tabId);
        }
    }

    private void loadDashboardData() {
        String sapId = sessionManager.getSapId();
        if (sapId == null || sapId.trim().isEmpty()) {
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            Student student = studentRepository.getStudentBySapId(sapId);
            AcademicPeriod period = periodRepository.getActivePeriodForAccount(sapId);
            List<PersonalSubject> subjects = period == null
                    ? null : subjectRepository.getSubjectsForPeriod(sapId, period.getId());
            long periodId = period == null ? 0 : period.getId();
            List<Assignment> pendingAssignments = periodId > 0
                    ? assignmentRepository.getIncompleteAssignmentsForPeriod(sapId, periodId) : new java.util.ArrayList<>();
            int openAssignments = periodId > 0 ? assignmentRepository.getOpenAssignmentCountForPeriod(sapId, periodId) : 0;

            int totalConducted = 0;
            int totalAttended = 0;
            int belowThresholdCount = 0;
            String department = student != null && student.getDepartment() != null
                    ? student.getDepartment().trim().toUpperCase(Locale.ROOT) : "";
            double threshold = "SOL".equals(department) ? 70.0
                    : ("STME".equals(department) || "SOC".equals(department) ? 80.0 : 0.0);

            if (subjects != null) {
                for (PersonalSubject subject : subjects) {
                    if (subject.isArchived()) {
                        continue;
                    }
                    int present = attendanceRepository.getPresentCountForSubject(sapId, subject.getId());
                    int absent = attendanceRepository.getAbsentCountForSubject(sapId, subject.getId());
                    int conducted = present + absent;
                    totalAttended += present;
                    totalConducted += conducted;
                    if (threshold > 0 && conducted > 0 && (present * 100.0 / conducted) < threshold) {
                        belowThresholdCount++;
                    }
                }
            }

            int finalTotalConducted = totalConducted;
            int finalTotalAttended = totalAttended;
            int finalBelowThresholdCount = belowThresholdCount;
            double finalThreshold = threshold;
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }

                String name = student != null ? student.getName() : null;
                tvGreeting.setText(name != null && !name.trim().isEmpty()
                        ? getString(R.string.welcome_named_student, name.trim())
                        : getString(R.string.welcome_student));

                if (pendingAssignments != null && !pendingAssignments.isEmpty()) {
                    Assignment nextUp = pendingAssignments.get(0);
                    tvNextUpTitle.setText(nextUp.getTitle());
                    tvNextUpDetail.setText(getString(R.string.assignment_due_summary,
                            nextUp.getDueDate(), nextUp.getSubject()));
                    tvNextUpTitle.setVisibility(View.VISIBLE);
                    tvNextUpDetail.setVisibility(View.VISIBLE);
                    tvNextUpEmpty.setVisibility(View.GONE);
                } else {
                    tvNextUpTitle.setVisibility(View.GONE);
                    tvNextUpDetail.setVisibility(View.GONE);
                    tvNextUpEmpty.setVisibility(View.VISIBLE);
                }

                if (period == null) {
                    tvAttendanceCount.setText(R.string.attendance_setup_prompt);
                } else if (finalTotalConducted == 0) {
                    tvAttendanceCount.setText(R.string.attendance_no_sessions_yet);
                } else {
                    double percentage = finalTotalAttended * 100.0 / finalTotalConducted;
                    String text = String.format(Locale.US, "%.1f%%", percentage);
                    if (finalBelowThresholdCount > 0 && finalThreshold > 0) {
                        text += getResources().getQuantityString(
                                R.plurals.attendance_below_threshold_count,
                                finalBelowThresholdCount, finalBelowThresholdCount);
                    }
                    tvAttendanceCount.setText(text);
                }
                tvAssignmentsCount.setText(getResources().getQuantityString(
                        R.plurals.open_assignment_count, openAssignments, openAssignments));
            });
        });
    }
}
