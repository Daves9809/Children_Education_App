package com.LoginAndRegistration.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ChildrenEducationApp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.SQLiteHelper.app.AppConfig;
import com.SQLiteHelper.app.AppController;
import com.SQLiteHelper.helper.SQLiteHandler;
import com.SQLiteHelper.helper.SessionManager;
import com.Main.MainActivity;


public class LoginActivity extends AppCompatActivity {

    //definiowanie zmiennych
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    //utworzenie widoku wraz z logiką
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //przypisywanie zmiennym ich odpowiedników w przestrzeni login.xml
        inputEmail =  findViewById(R.id.email);
        inputPassword =  findViewById(R.id.password);
        btnLogin =  findViewById(R.id.btnLogin);
        btnLinkToRegister =  findViewById(R.id.btnLinkToRegisterScreen);

        // Okno dialogowe pokazujące wskaźnik postępu, nie można go wyłączyć lub ominąć
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Lokalna baza danych SQLite
        db = new SQLiteHandler(getApplicationContext());

        // Session manager, pozwala obsługiwać aplikację w zakresie logowania
        session = new SessionManager(getApplicationContext());

        // Sprawdzamy czy użytkownik jest zalogowany
        if (session.isLoggedIn()) {
            // Użytkownik jest zalogowany, zabierz go do MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Wykonuje się w momencie kliknięcia w przycisk btnLogin
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Jesli dane są puste
                if (!email.isEmpty() && !password.isEmpty()) {
                    // logowanie
                    checkLogin(email, password);
                } else {
                    // poproś użytkownika o wypełnienie pustych przestrzeni
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // W przypadku kliknięcia w przycisk btnLinkToRegister następuje przeniesienie do RegisterActivity
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     Funkcja sprawdzająca poprawność danych w MySql oraz jednoczesnie aktualizaujaca dane w SQLite
     * */
    private void checkLogin(final String email, final String password) {
        // Tag używany do anulowania żądania
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_LOGIN, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // funkcja odbierająca odpowiedz od plikow php
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response); // Dane dotyczące logowania w Logcat'ie
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // sprawdz błąd
                    // jesli nie ma wykona się poniższa funkcja(użytkownik pomyślnie zalogowany)
                    if (!error) {
                        // Utworzenie sesji Logowania
                        session.setLogin(true);

                        // Utworzenie zmiennych do przechowania danych w SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String steps = user.getString("steps");
                        String points = user.getString("points");
                        String game = user.getString("game");
                        String poziom = user.getString("poziom");
                        String created_at = user
                                .getString("created_at");
                        String updated_at = user
                                .getString("updated_at");
                        // Wstawianie wiersza w tabeli użytkowników
                        db.addUser(name, email, uid, steps, points, game,poziom, created_at,updated_at);

                        // Inicjacja MainActivity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Błąd w logowaniu. Pobierz błąd
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}