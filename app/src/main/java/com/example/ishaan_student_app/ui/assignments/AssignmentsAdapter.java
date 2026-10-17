package com.example.ishaan_student_app.ui.assignments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.Assignment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.AssignmentViewHolder> {

    public interface OnAssignmentChangeListener {
        void onAssignmentCompletedToggle(Assignment assignment, boolean isCompleted);
    }

    public interface OnAssignmentLongClickListener {
        void onAssignmentLongClick(Assignment assignment);
    }

    private final List<Assignment> assignmentsList;
    private final OnAssignmentChangeListener toggleListener;
    private final OnAssignmentLongClickListener longClickListener;

    public AssignmentsAdapter(List<Assignment> assignmentsList,
                              OnAssignmentChangeListener toggleListener,
                              OnAssignmentLongClickListener longClickListener) {
        this.assignmentsList = assignmentsList;
        this.toggleListener = toggleListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignmentsList.get(position);
        holder.bind(assignment, toggleListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return assignmentsList != null ? assignmentsList.size() : 0;
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox cbCompleted;
        private final TextView tvTitle;
        private final TextView tvSubjectDate;
        private final TextView tvStatusBadge;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cb_assignment_completed);
            tvTitle = itemView.findViewById(R.id.tv_assignment_title);
            tvSubjectDate = itemView.findViewById(R.id.tv_assignment_subject_date);
            tvStatusBadge = itemView.findViewById(R.id.tv_assignment_status_badge);
        }

        public void bind(Assignment assignment,
                         OnAssignmentChangeListener toggleListener,
                         OnAssignmentLongClickListener longClickListener) {
            Context context = itemView.getContext();
            tvTitle.setText(assignment.getTitle());

            String subjectDateStr = assignment.getSubject() + " • Due: " + assignment.getDueDate();
            tvSubjectDate.setText(subjectDateStr);

            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(assignment.isCompleted());

            String today = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
            String dueDate = assignment.getDueDate() != null ? assignment.getDueDate().trim() : "";

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date due = null;
            try {
                if (!dueDate.isEmpty()) {
                    due = sdf.parse(dueDate);
                }
            } catch (Exception ignored) {
            }

            String statusText;
            int statusColor;

            if (assignment.isCompleted()) {
                statusText = context.getString(R.string.status_completed);
                statusColor = ContextCompat.getColor(context, R.color.text_secondary_slate);
            } else if (due != null && isBeforeToday(due)) {
                statusText = context.getString(R.string.status_overdue);
                statusColor = ContextCompat.getColor(context, R.color.accent_persimmon);
            } else if (!dueDate.isEmpty() && dueDate.equals(today)) {
                statusText = context.getString(R.string.status_due_today);
                statusColor = ContextCompat.getColor(context, R.color.accent_persimmon);
            } else {
                statusText = context.getString(R.string.status_upcoming);
                statusColor = ContextCompat.getColor(context, R.color.accent_moss);
            }

            String priority = assignment.getPriority();
            if (priority != null && !priority.isEmpty()) {
                statusText = statusText + " • " + priority;
            }

            tvStatusBadge.setText(statusText);
            tvStatusBadge.setTextColor(statusColor);

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (toggleListener != null) {
                    toggleListener.onAssignmentCompletedToggle(assignment, isChecked);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onAssignmentLongClick(assignment);
                    return true;
                }
                return false;
            });
        }

        private static boolean isBeforeToday(Date due) {
            Calendar calToday = Calendar.getInstance();
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            Calendar calDue = Calendar.getInstance();
            calDue.setTime(due);
            calDue.set(Calendar.HOUR_OF_DAY, 0);
            calDue.set(Calendar.MINUTE, 0);
            calDue.set(Calendar.SECOND, 0);
            calDue.set(Calendar.MILLISECOND, 0);

            return calDue.before(calToday);
        }
    }
}
