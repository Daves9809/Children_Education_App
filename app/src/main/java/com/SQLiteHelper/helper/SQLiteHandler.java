package com.SQLiteHelper.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandler.class.getSimpleName();
    // Wszystkie statyczne zmienne
    // Wersja bazy danych
    private static final int DATABASE_VERSION = 2;

    // Nazwa bazy danych
    private static final String DATABASE_NAME = "android_api";

    // Nazwa tabeli logowania
    private static final String TABLE_USER = "user";

    // Nazwa kolumn tabeli logowania
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_POINTS = "points";
    private static final String KEY_STEPS = "steps";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Utworzenie tablic
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_STEPS + " INTEGER,"
                + KEY_POINTS + " INTEGER,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_UPDATED_AT + " TEXT" +")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Aktualizacja tablic
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Usunięcie starych istniejących tablic
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Ponowne utworzenie aktualnej tabeli
        onCreate(db);
    }
    //funkcja do update'owania danych w SQLite
    public void updateUser(String id, String steps){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("UPDATE " + TABLE_USER +" SET " + KEY_STEPS + " = " + steps + "  WHERE " + KEY_ID + " = " +  id);
        Log.d(TAG,"Sqlite data updated");
    }


    /**
     Zapis danych użytkownika do bazy danych
     * */
    public void addUser(String name, String email, String uid,String steps, String points, String created_at, String updated_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Email
        values.put(KEY_STEPS, steps); // Steps
        values.put(KEY_POINTS, points); // Points
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_UPDATED_AT, updated_at); // Created At

        // Dodanie wiersza
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     Pobieranie danych użytkownika z bazy danych
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Przenieśdo pierwszego wiersza
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("steps", cursor.getString(4));
            user.put("points", cursor.getString(5));
            user.put("created_at", cursor.getString(6));
            user.put("updated_at", cursor.getString(7));
        }
        cursor.close();
        db.close();
        // zwrócenie użytkownika
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     Utworzenie ponownie bazy danych Usunięcie wszystkich tabel i utworzenie ich ponownie
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
