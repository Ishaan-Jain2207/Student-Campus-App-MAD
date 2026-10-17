package com.example.ishaan_student_app.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.Student;
import com.example.ishaan_student_app.data.repository.LocalAccountRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.ui.main.MainActivity;
import com.example.ishaan_student_app.util.AppExecutors;
import com.example.ishaan_student_app.util.RegistrationValidator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    @Inject LocalAccountRepository accountRepository;
    @Inject SessionManager sessionManager;

    private TextInputLayout tilName;
    private TextInputLayout tilSapId;
    private TextInputLayout tilEmail;
    private Spinner spinnerDept;
    private Spinner spinnerYear;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etName;
    private TextInputEditText etSapId;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private Button btnRegister;
    private TextView tvLinkSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.til_register_name);
        tilSapId = findViewById(R.id.til_register_sap_id);
        tilEmail = findViewById(R.id.til_register_email);
        spinnerDept = findViewById(R.id.spinner_register_dept);
        spinnerYear = findViewById(R.id.spinner_register_year);
        tilPassword = findViewById(R.id.til_register_password);
        tilConfirmPassword = findViewById(R.id.til_register_confirm_password);

        etName = findViewById(R.id.et_register_name);
        etSapId = findViewById(R.id.et_register_sap_id);
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        etConfirmPassword = findViewById(R.id.et_register_confirm_password);

        btnRegister = findViewById(R.id.btn_register);
        tvLinkSignIn = findViewById(R.id.tv_link_sign_in);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> deptAdapter = ArrayAdapter.createFromResource(
                this, R.array.departments_array, android.R.layout.simple_spinner_item);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDept.setAdapter(deptAdapter);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                this, R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLinkSignIn.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String sapId = etSapId.getText() != null ? etSapId.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        tilName.setError(null);
        tilSapId.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean isValid = true;

        if (!RegistrationValidator.isValidFullName(name)) {
            tilName.setError(getString(R.string.error_empty_name));
            isValid = false;
        }

        if (!RegistrationValidator.isValidSapId(sapId)) {
            tilSapId.setError(getString(R.string.error_invalid_sap_id));
            isValid = false;
        }

        if (!RegistrationValidator.isValidNmimsEmail(email)) {
            tilEmail.setError(getString(R.string.error_invalid_nmims_email));
            isValid = false;
        }

        if (spinnerDept.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_select_dept, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spinnerYear.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_select_year, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!RegistrationValidator.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.error_short_password));
            isValid = false;
        }

        if (!RegistrationValidator.doPasswordsMatch(password, confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        int year = spinnerYear.getSelectedItemPosition();
        String department = spinnerDept.getSelectedItem().toString().trim();
        Student student = new Student(0, sapId, name, email, department, year, "");

        btnRegister.setEnabled(false);
        AppExecutors.getInstance().diskIO().execute(() -> {
            LocalAccountRepository.RegistrationResult result = accountRepository.register(student, password);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (isFinishing()) {
                    return;
                }
                btnRegister.setEnabled(true);
                switch (result) {
                    case CREATED:
                        sessionManager.createSession(sapId, email);
                        Toast.makeText(this, R.string.registration_local_success, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                        break;
                    case SAP_ID_ALREADY_USED:
                        tilSapId.setError(getString(R.string.registration_sap_id_exists));
                        break;
                    case EMAIL_ALREADY_USED:
                        tilEmail.setError(getString(R.string.registration_email_exists));
                        break;
                    default:
                        Toast.makeText(this, R.string.registration_local_error, Toast.LENGTH_LONG).show();
                        break;
                }
            });
        });
    }
}
