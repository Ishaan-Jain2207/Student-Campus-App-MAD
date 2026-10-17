package com.example.ishaan_student_app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.repository.LocalAccountRepository;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.ui.main.MainActivity;
import com.example.ishaan_student_app.ui.register.RegisterActivity;
import com.example.ishaan_student_app.util.AppExecutors;
import com.example.ishaan_student_app.util.RegistrationValidator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

/**
 * LoginActivity for student authentication in SVKM's NMIMS Chandigarh.
 */
@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Inject LocalAccountRepository accountRepository;
    @Inject SessionManager sessionManager;

    private TextInputLayout tilSapId;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etSapId;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvLinkRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilSapId = findViewById(R.id.til_login_sap_id);
        tilEmail = findViewById(R.id.til_login_email);
        tilPassword = findViewById(R.id.til_login_password);
        etSapId = findViewById(R.id.et_login_sap_id);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        tvLinkRegister = findViewById(R.id.tv_link_register);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvLinkRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String sapId = etSapId.getText() != null ? etSapId.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        tilSapId.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        if (!RegistrationValidator.isValidSapId(sapId)) {
            tilSapId.setError(getString(R.string.error_invalid_sap_id));
            isValid = false;
        }

        if (!RegistrationValidator.isValidNmimsEmail(email)) {
            tilEmail.setError(getString(R.string.error_invalid_nmims_email));
            isValid = false;
        }

        if (!RegistrationValidator.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.error_short_password));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        btnLogin.setEnabled(false);
        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean authenticated = accountRepository.authenticate(sapId, email, password);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (isFinishing()) {
                    return;
                }
                btnLogin.setEnabled(true);
                if (authenticated) {
                    sessionManager.createSession(sapId, email);
                    etPassword.setText("");
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    tilPassword.setError(getString(R.string.error_login_credentials));
                }
            });
        });
    }
}
