package com.example.ishaan_student_app.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.Fragment;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.Student;
import com.example.ishaan_student_app.data.repository.StudentRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.util.AppExecutors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ImageView imgAvatar;
    private TextView tvName;
    private TextView tvDeptYear;
    private TextView tvProfileUnavailable;
    private EditText etName;
    private EditText etSapId;
    private EditText etEmail;
    private EditText etDepartment;
    private EditText etYear;
    private Button btnEdit;
    private Button btnChoosePhoto;

    private StudentRepository repository;
    private SessionManager sessionManager;
    private Student currentStudent;
    private String selectedPhotoUri = "";
    private boolean isEditing = false;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        String localFilePath = savePhotoToInternalStorage(uri);
                        AppExecutors.getInstance().mainThread().execute(() -> {
                            if (!isAdded()) {
                                return;
                            }
                            selectedPhotoUri = localFilePath;
                            displayAvatarImage(selectedPhotoUri);
                        });
                    });
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        repository = new StudentRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        initViews(root);
        setupListeners();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStudentProfile();
    }

    private void initViews(View root) {
        imgAvatar = root.findViewById(R.id.img_profile_avatar);
        tvName = root.findViewById(R.id.tv_profile_name);
        tvDeptYear = root.findViewById(R.id.tv_profile_dept_year);
        tvProfileUnavailable = root.findViewById(R.id.tv_profile_unavailable);
        etName = root.findViewById(R.id.et_profile_name);
        etSapId = root.findViewById(R.id.et_profile_sap_id);
        etEmail = root.findViewById(R.id.et_profile_email);
        etDepartment = root.findViewById(R.id.et_profile_department);
        etYear = root.findViewById(R.id.et_profile_year);
        btnEdit = root.findViewById(R.id.btn_edit_profile);
        btnChoosePhoto = root.findViewById(R.id.btn_choose_profile_photo);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> toggleEditState());

        imgAvatar.setOnClickListener(v -> {
            if (isEditing) {
                launchPhotoPicker();
            }
        });
        btnChoosePhoto.setOnClickListener(v -> launchPhotoPicker());
    }

    private void launchPhotoPicker() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }

    private void loadStudentProfile() {
        String activeSapId = sessionManager.getSapId();
        String activeEmail = sessionManager.getEmail();

        AppExecutors.getInstance().diskIO().execute(() -> {
            Student student = repository.getStudentBySapIdOrEmail(activeSapId, activeEmail);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) {
                    return;
                }

                if (student != null) {
                    currentStudent = student;
                    tvProfileUnavailable.setVisibility(View.GONE);
                    btnEdit.setEnabled(true);
                    btnChoosePhoto.setVisibility(isEditing ? View.VISIBLE : View.GONE);
                    tvName.setText(student.getName());
                    String deptStr = student.getDepartment() != null && !student.getDepartment().isEmpty() ? student.getDepartment() : "STME";
                    String yearStr = student.getYear() > 0 ? "Year " + student.getYear() : "Year 1";
                    String deptYearStr = deptStr + " • " + yearStr;
                    tvDeptYear.setText(deptYearStr);

                    etName.setText(student.getName());
                    etSapId.setText(student.getSapId());
                    etEmail.setText(student.getEmail());
                    etDepartment.setText(deptStr);
                    etYear.setText(yearStr);

                    selectedPhotoUri = student.getPhotoUri() != null ? student.getPhotoUri() : "";
                    displayAvatarImage(selectedPhotoUri);
                } else {
                    currentStudent = new Student(0, activeSapId, "", activeEmail, "STME", 1, "");
                    tvProfileUnavailable.setVisibility(View.GONE);
                    btnEdit.setEnabled(true);
                    btnChoosePhoto.setVisibility(isEditing ? View.VISIBLE : View.GONE);
                    tvName.setText(activeSapId.isEmpty() ? (activeEmail.isEmpty() ? "Student Account" : activeEmail) : "SAP ID: " + activeSapId);
                    tvDeptYear.setText("NMIMS Chandigarh Student");

                    etName.setText("");
                    etSapId.setText(activeSapId);
                    etEmail.setText(activeEmail);
                    etDepartment.setText("STME");
                    etYear.setText("Year 1");
                    selectedPhotoUri = "";
                    displayAvatarImage("");
                }
            });
        });
    }

    private void toggleEditState() {
        if (!isEditing) {
            isEditing = true;
            etName.setEnabled(true);
            btnChoosePhoto.setVisibility(View.VISIBLE);
            btnEdit.setText(R.string.btn_save_profile);
            Toast.makeText(requireContext(), R.string.profile_edit_hint, Toast.LENGTH_SHORT).show();
        } else {
            String name = etName.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }

            long id = currentStudent != null ? currentStudent.getId() : 0;
            String sapId = currentStudent != null && !currentStudent.getSapId().isEmpty() ? currentStudent.getSapId() : sessionManager.getSapId();
            String email = currentStudent != null && !currentStudent.getEmail().isEmpty() ? currentStudent.getEmail() : sessionManager.getEmail();
            String dept = currentStudent != null && currentStudent.getDepartment() != null && !currentStudent.getDepartment().isEmpty() ? currentStudent.getDepartment() : "STME";
            int year = currentStudent != null && currentStudent.getYear() > 0 ? currentStudent.getYear() : 1;

            Student updatedStudent = new Student(id, sapId, name, email, dept, year, selectedPhotoUri);

            btnEdit.setEnabled(false);

            AppExecutors.getInstance().diskIO().execute(() -> {
                boolean success = repository.updateStudent(updatedStudent);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    btnEdit.setEnabled(true);

                    if (success) {
                        isEditing = false;
                        etName.setEnabled(false);
                        btnChoosePhoto.setVisibility(View.GONE);
                        btnEdit.setText(R.string.btn_edit_profile);
                        tvName.setText(name);
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        loadStudentProfile();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save profile. Please check name and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    private String savePhotoToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return "";
            }
            File dir = new File(requireContext().getFilesDir(), "profile_photos");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String sapId = sessionManager.getSapId().isEmpty() ? "user" : sessionManager.getSapId();
            File file = new File(dir, "avatar_" + sapId + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 4];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            Log.e(TAG, "Error saving avatar photo to internal storage", e);
            return uri.toString();
        }
    }

    private void displayAvatarImage(String uriString) {
        imgAvatar.setImageURI(null); // Force clear previous URI cache in ImageView
        if (uriString != null && !uriString.trim().isEmpty()) {
            try {
                Uri uri = Uri.parse(uriString.trim());
                if ("file".equalsIgnoreCase(uri.getScheme()) && uri.getPath() != null) {
                    File file = new File(uri.getPath());
                    if (file.exists() && file.length() > 0) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            imgAvatar.setImageBitmap(bitmap);
                            return;
                        }
                    }
                }
                imgAvatar.setImageURI(uri);
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error setting image URI: " + uriString, e);
            }
        }
        imgAvatar.setImageResource(R.drawable.ic_avatar);
    }
}
