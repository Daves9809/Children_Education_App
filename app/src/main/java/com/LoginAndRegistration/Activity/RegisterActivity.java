package com.LoginAndRegistration.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.ChildrenEducationApp.R;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.SQLiteHelper.app.AppConfig;
import com.SQLiteHelper.app.AppController;
import com.SQLiteHelper.helper.SQLiteHandler;
import com.SQLiteHelper.helper.SessionManager;
import com.Main.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    //definiowanie zmiennych
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    //utworzenie widoku wraz z logiką
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //przypisywanie zmiennym ich odpowiedników w przestrzeni activity_register.xml
        inputFullName =  findViewById(R.id.name);
        inputEmail =  findViewById(R.id.email);
        inputPassword =  findViewById(R.id.password);
        btnRegister =  findViewById(R.id.btnRegister);
        btnLinkToLogin =  findViewById(R.id.btnLinkToLoginScreen);

        // Okno dialogowe pokazujące wskaźnik postępu, nie można go wyłączyć lub ominąć
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager, pozwala obsługiwać aplikację w zakresie logowania
        session = new SessionManager(getApplicationContext());

        // Lokalna baza danych SQLite
        db = new SQLiteHandler(getApplicationContext());

        // Sprawdzamy czy użytkownik jest zalogowany
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    registerUser(name, email, password);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Wykonuje się w momencie kliknięcia w przycisk btnLogin
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * Funkcja do przechowywani użytkownika w bazie danych MySql będzie wysyłac parametry(tag,nazwa,
     * email, password) do adresu url rejestracji
     * */
    private void registerUser(final String name, final String email,
                              final String password) {

        // Tag używany do anulowania zapytania
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // Użytkownik pomyślnie zalogowany
                        // Teraz zapisz użytkownika w bazie danych SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String steps = user.getString("steps");
                        String points = user.getString("points");
                        String created_at = user
                                .getString("created_at");
                        String updated_at = user
                                .getString("updated_at");

                        // Wstawianie wiersza w tabeli użytkowników
                        db.addUser(name, email, uid, steps, points, created_at,updated_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Inicjowanie LoginActivity
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // W rejestracji wystąpił błąd. Wydrukuj wiadomość błędu
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url rejestracji
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Dodanie zapytania do kolejki zapytań
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}