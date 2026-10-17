package com.example.ishaan_student_app.data.session;

import android.content.Context;
import android.content.SharedPreferences;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

/**
 * Stores the signed-in state for a verified device-local CampusMate account.
 * It does not verify an NMIMS identity or synchronize with a server.
 */
public class SessionManager {

    private static final String PREF_NAME = "campusmate_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SAP_ID = "sap_id";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences pref;

    @Inject
    public SessionManager(@ApplicationContext Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void createSession(String sapId, String email) {
        pref.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_SAP_ID, sapId != null ? sapId.trim() : "")
                .putString(KEY_EMAIL, email != null ? email.trim() : "")
                .apply();
    }

    public String getSapId() {
        return pref.getString(KEY_SAP_ID, "");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public void logout() {
        pref.edit().clear().apply();
    }
}
