package com.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.Adapter.User;
import com.Adapter.UserAdapter;
import com.ChildrenEducationApp.R;
import com.SQLiteHelper.app.AppConfig;
import com.SQLiteHelper.app.AppController;
import com.SQLiteHelper.helper.SQLiteHandler;
import com.SQLiteHelper.helper.SessionManager;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = EditActivity.class.getSimpleName();
    private Button btnSave, btnReset, btnDelete;
    private EditText inputName, inputPassword, inputSecondPassword;
    private SQLiteHandler db;
    private String email;
    private List<String> names;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        btnReset = findViewById(R.id.btnReset);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        inputName = findViewById(R.id.imie);
        inputPassword = findViewById(R.id.password);
        inputSecondPassword = findViewById(R.id.secondPassword);
        session = new SessionManager(getApplicationContext());
        names = new ArrayList<>();

        getUsers(); // pobranie imion

        db = new SQLiteHandler(getApplicationContext(), "android_user");

        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = inputName.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String secondPassword = inputSecondPassword.getText().toString().trim();

                if (!name.isEmpty()) {
                    if(!password.isEmpty() && !secondPassword.isEmpty()) {
                        if (password.equals(secondPassword)) {
                            changePassword(password);
                        } else
                            Toast.makeText(getApplicationContext(), "Hasła się nie pokrywają", Toast.LENGTH_LONG).show();
                    }

                    int i =0;
                    int size = names.size();
                    for (String s : names) {
                        if (!s.equals(name))
                            i++;
                        if(size == i) {
                            changeName(name);
                        }
                    }
                } else {
                    if (!password.isEmpty() && !secondPassword.isEmpty()) {
                        if (password.equals(secondPassword)) {
                            changePassword(password);
                        } else
                            Toast.makeText(getApplicationContext(), "Hasła się nie pokrywają", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
                db.updateUser("0",0,0,1,today());
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
                session.setLogin(false);

                db.deleteUsers();

                // Zainicjowanie LoginActivity
                Intent intent = new Intent(EditActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void deleteUser() {
        // Tag używany do anulowania żądania
        String tag_string_req = "reset_data";

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_DELETE_USER, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // method which gets response
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Delete user Response: " + response);

                Toast.makeText(getApplicationContext(), "Użytkownik pomyślnie usunięty!", Toast.LENGTH_LONG).show();

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url logowania
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }

        };


        // Dodanie zapytania do kolejki zapytań
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void resetData() {
        // Tag używany do anulowania żądania
        String tag_string_req = "reset_data";

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_RESET_DATA, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // method which gets response
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Reset data Response: " + response);

                Toast.makeText(getApplicationContext(), "Dane zostały wyzerowane!", Toast.LENGTH_LONG).show();

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url logowania
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }

        };


        // Dodanie zapytania do kolejki zapytań
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void changePassword(final String password) {
        // Tag używany do anulowania żądania
        String tag_string_req = "change_name";

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_CHANGE_PASSWORD, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // method which gets response
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Password Change Response: " + response);

                Toast.makeText(getApplicationContext(), "Hasło zostało pomyślnie zmienione!", Toast.LENGTH_LONG).show();

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url logowania
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }

        };


        // Dodanie zapytania do kolejki zapytań
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void getUsers() {
        String tag_string_req = "req_getUsers";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_ALL_USERS, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // funkcja odbierająca odpowiedz z pliku php
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "GetAllUsers response: " + response); // Dane dotyczące logowania w Logcat'ie

                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        String name = object.getString("name");

                        names.add(name);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "StatististicsActivity Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Dodanie zapytania do kolejki zapytań
        Volley.newRequestQueue(EditActivity.this).add(strReq);
    }

    private void changeName(final String name) { // trzeba konwertowac inta steps do stringa
        // Tag używany do anulowania żądania
        String tag_string_req = "change_name";

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_CHANGE_NAME, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // method which gets response
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Change name Response: " + response);
                db.updateName(name);
                Toast.makeText(getApplicationContext(), "Imie zostało pomyślnie zmienione", Toast.LENGTH_LONG).show();


            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url logowania
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("name",name);
                return params;
            }

        };


        // Dodanie zapytania do kolejki zapytań
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public String today() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(Calendar.getInstance().getTime()) + " 00:00:00";
    }

}