package com.hucmuaf.locket_mobile.auth;

import android.content.Context;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ID_TOKEN = "idToken";
    private static final String KEY_UID = "userId";
    private TokenManager() {
    }
    public static void saveToken(Context context, String token) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ID_TOKEN, token)
                .apply();
    }
    public static void saveUid(Context context, String uid) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_UID, uid)
                .apply();
    }

    public static String getUid(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_UID, null);
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ID_TOKEN, null);
    }

    public static void clearToken(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_ID_TOKEN)
                .apply();
    }
}
