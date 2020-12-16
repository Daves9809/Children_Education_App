package com.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ChildrenEducationApp.R;
import com.SQLiteHelper.app.AppConfig;
import com.SQLiteHelper.app.AppController;
import com.SQLiteHelper.helper.SQLiteHandler;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = EditActivity.class.getSimpleName();
    private Button btnSave,btnReset;
    private EditText inputName,inputPassword,inputSecondPassword;
    private SQLiteHandler db;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        btnReset = findViewById(R.id.btnReset);
        btnSave = findViewById(R.id.btnSave);

        inputName = findViewById(R.id.imie);
        inputPassword = findViewById(R.id.password);
        inputSecondPassword = findViewById(R.id.secondPassword);


        db = new SQLiteHandler(getApplicationContext(),"android_user");

        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = inputName.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String secondPassword = inputSecondPassword.getText().toString().trim();

                if(!name.isEmpty() ) {
                    if (password.equals(secondPassword))
                        //updateData(email, name, password);
                        Toast.makeText(getApplicationContext(), "Udało się zaktualizować dane", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), "Hasła się nie pokrywają", Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Podaj nowe imię lub wpisz aktualne", Toast.LENGTH_LONG).show();
            }
        });
    }

  /*  private void updateData(String email, String name, String password) { // trzeba konwertowac inta steps do stringa
        // Tag używany do anulowania żądania
        String tag_string_req = "req_update";

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_SEND_DATA, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // method which gets response
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response + " Steps = " + steps + " Points= " + appScore+ " Game= " + game + " Poziom= " + poziom + " today()= " + today());
                if(!basicPoints.equals("null")){
                    if((Integer.parseInt(steps) <= Integer.parseInt(poziom) * 10) && (Integer.parseInt(basicPoints)  + appScore >= Integer.parseInt(poziom) * 10)){
                        db.updateUser(id, steps, (Integer.parseInt(basicPoints) + appScore - Integer.parseInt(poziom) * 10), game, Integer.parseInt(poziom) + 1, today());
                        levelToSend = Integer.parseInt(poziom) +1;
                        pointsToSend = Integer.parseInt(basicPoints) + appScore - Integer.parseInt(poziom) * 10;
                    }
                    else{
                        db.updateUser(id, steps, Integer.parseInt(basicPoints) + appScore, game, Integer.parseInt(poziom), today());
                        levelToSend = Integer.parseInt(poziom);
                        pointsToSend = Integer.parseInt(basicPoints) + appScore;
                    }
                }
                else{
                    if((Integer.parseInt(steps) <= Integer.parseInt(poziom) * 10) && (appScore >= Integer.parseInt(poziom) * 10)) {
                        db.updateUser(id, steps, (appScore - Integer.parseInt(poziom) * 10), game, Integer.parseInt(poziom) + 1, today());
                        levelToSend = Integer.parseInt(poziom) +1;
                        pointsToSend = appScore - Integer.parseInt(poziom) * 10;
                        Log.d(TAG + "points to send: ",String.valueOf(pointsToSend));
                    }
                    else {
                        db.updateUser(id, steps, appScore, game, Integer.parseInt(poziom), today());
                        levelToSend = Integer.parseInt(poziom);
                        pointsToSend = appScore;
                    }
                }

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url logowania
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("steps", steps);
                params.put("updated_at",today());
                params.put("points",String.valueOf(pointsToSend));
                params.put("game", String.valueOf(game));
                params.put("poziom",String.valueOf(levelToSend));

                return params;
            }

        };


        // Dodanie zapytania do kolejki zapytań
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

   */
}