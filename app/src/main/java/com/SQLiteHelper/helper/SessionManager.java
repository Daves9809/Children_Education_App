package com.SQLiteHelper.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences pozwala na zapis i odzyskanie danych w formie klucza-wartosci
    SharedPreferences pref;

    Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    // nazwa pliku
    private static final String PREF_NAME = "ChildrenAppLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String KEY_LEVEL_UP = "isLevelUp";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void
    setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void
    setLevelUp(boolean isLevelUp) {

        editor.putBoolean(KEY_LEVEL_UP, isLevelUp);

        editor.commit();
        Log.d(TAG, "User leveledUp");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public boolean isLevelUp(){
        return pref.getBoolean(KEY_LEVEL_UP, false);
    }
}
