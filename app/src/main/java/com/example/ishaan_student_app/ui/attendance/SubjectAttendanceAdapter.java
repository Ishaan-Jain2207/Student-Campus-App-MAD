package com.example.ishaan_student_app.ui.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.SubjectAttendanceSummary;

import java.util.List;
import java.util.Locale;

public class SubjectAttendanceAdapter extends RecyclerView.Adapter<SubjectAttendanceAdapter.SubjectViewHolder> {

    public interface SessionActionListener {
        void onAddSession(SubjectAttendanceSummary subject, boolean attended);
        void onRemoveSession(SubjectAttendanceSummary subject, boolean attended);
    }

    public interface SubjectManageListener {
        void onEditSubject(SubjectAttendanceSummary subject);
        void onDeleteSubject(SubjectAttendanceSummary subject);
    }

    private final List<SubjectAttendanceSummary> subjectsList;
    private final SessionActionListener actionListener;
    private final SubjectManageListener manageListener;

    public SubjectAttendanceAdapter(List<SubjectAttendanceSummary> subjectsList,
                                    SessionActionListener actionListener,
                                    SubjectManageListener manageListener) {
        this.subjectsList = subjectsList;
        this.actionListener = actionListener;
        this.manageListener = manageListener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_attendance, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectAttendanceSummary subject = subjectsList.get(position);
        holder.bind(subject, actionListener, manageListener);
    }

    @Override
    public int getItemCount() {
        return subjectsList != null ? subjectsList.size() : 0;
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNameCode;
        private final TextView tvPercentage;
        private final TextView tvTotal;
        private final TextView tvAttended;
        private final TextView tvMissed;
        private final TextView tvThresholdStatus;
        private final ProgressBar progressBar;
        private final Button btnAttendDecrease;
        private final Button btnAttendIncrease;
        private final Button btnMissDecrease;
        private final Button btnMissIncrease;
        private final ImageView btnDeleteSubject;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameCode = itemView.findViewById(R.id.tv_subject_name_code);
            tvPercentage = itemView.findViewById(R.id.tv_subject_percentage);
            tvTotal = itemView.findViewById(R.id.tv_subject_total);
            tvAttended = itemView.findViewById(R.id.tv_subject_attended);
            tvMissed = itemView.findViewById(R.id.tv_subject_missed);
            tvThresholdStatus = itemView.findViewById(R.id.tv_subject_threshold_status);
            progressBar = itemView.findViewById(R.id.pb_subject_attendance);
            btnAttendDecrease = itemView.findViewById(R.id.btn_attend_decrease);
            btnAttendIncrease = itemView.findViewById(R.id.btn_attend_increase);
            btnMissDecrease = itemView.findViewById(R.id.btn_miss_decrease);
            btnMissIncrease = itemView.findViewById(R.id.btn_miss_increase);
            btnDeleteSubject = itemView.findViewById(R.id.btn_delete_subject);
        }

        public void bind(SubjectAttendanceSummary subject,
                         SessionActionListener actionListener,
                         SubjectManageListener manageListener) {
            Context context = itemView.getContext();

            String facultyStr = subject.getSubjectCode() != null && !subject.getSubjectCode().trim().isEmpty()
                    ? " • " + subject.getSubjectCode().trim()
                    : "";
            String titleStr = subject.getSubjectName() + facultyStr;
            tvNameCode.setText(titleStr);

            int attended = subject.getTotalAttendedSessions();
            int total = subject.getTotalConductedSessions();
            int missed = Math.max(0, total - attended);
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, R.color.accent_moss)));
            tvTotal.setText(String.valueOf(total));
            tvAttended.setText(String.valueOf(attended));
            tvMissed.setText(String.valueOf(missed));
            btnAttendDecrease.setEnabled(attended > 0);
            btnMissDecrease.setEnabled(missed > 0);

            if (actionListener != null) {
                btnAttendIncrease.setOnClickListener(v -> actionListener.onAddSession(subject, true));
                btnMissIncrease.setOnClickListener(v -> actionListener.onAddSession(subject, false));
                btnAttendDecrease.setOnClickListener(v -> actionListener.onRemoveSession(subject, true));
                btnMissDecrease.setOnClickListener(v -> actionListener.onRemoveSession(subject, false));
            } else {
                btnAttendIncrease.setOnClickListener(null);
                btnMissIncrease.setOnClickListener(null);
                btnAttendDecrease.setOnClickListener(null);
                btnMissDecrease.setOnClickListener(null);
            }

            if (btnDeleteSubject != null) {
                btnDeleteSubject.setOnClickListener(v -> {
                    if (manageListener != null) {
                        manageListener.onDeleteSubject(subject);
                    }
                });
            }

            itemView.setOnLongClickListener(v -> {
                if (manageListener != null) {
                    manageListener.onEditSubject(subject);
                    return true;
                }
                return false;
            });

            if (subject.getTotalConductedSessions() <= 0) {
                tvPercentage.setText(R.string.attendance_no_percentage);
                tvThresholdStatus.setText(R.string.attendance_add_class_hint);
                tvThresholdStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_slate));
                tvThresholdStatus.setBackgroundResource(R.drawable.bg_chip);
                progressBar.setProgress(0);
            } else {
                double pct = subject.getAttendancePercentage();
                String pctStr = String.format(Locale.US, "%.1f%%", pct);
                tvPercentage.setText(pctStr);
                progressBar.setProgress((int) Math.round(pct));

                String dept = subject.getDepartment();

                if (dept == null || dept.trim().isEmpty() || "UNKNOWN".equalsIgnoreCase(dept.trim())) {
                    tvThresholdStatus.setText(R.string.attendance_target_unavailable);
                    tvThresholdStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_slate));
                    tvThresholdStatus.setBackgroundResource(R.drawable.bg_chip);
                } else {
                    if (subject.isBelowDepartmentThreshold()) {
                        tvThresholdStatus.setText(R.string.attendance_below_target);
                        tvThresholdStatus.setTextColor(ContextCompat.getColor(context, R.color.warning_amber));
                        tvThresholdStatus.setBackgroundResource(R.drawable.bg_attendance_warning);
                        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning_amber)));
                    } else {
                        tvThresholdStatus.setText(R.string.attendance_at_target);
                        tvThresholdStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_moss));
                        tvThresholdStatus.setBackgroundResource(R.drawable.bg_attendance_safe);
                        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, R.color.accent_moss)));
                    }
                }
            }
        }
    }
}
