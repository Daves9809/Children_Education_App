package com.StepCounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.LoginAndRegistration.Activity.LoginActivity;
import com.Main.MainActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ChildrenEducationApp.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.SQLiteHelper.app.AppConfig;
import com.SQLiteHelper.app.AppController;
import com.SQLiteHelper.helper.SQLiteHandler;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = StepCounterActivity.class.getSimpleName();
    private TextView textViewStepCounter, textViewStepDetector;
    private Button btnButton;
    private Button btnSend;
    private SensorManager mSensorManager;
    private Sensor countSensor;
    private int milestoneStep;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;
    private SQLiteHandler db;
    private ProgressDialog pDialog;
    private Handler mHandler;
    private int appScore;
    private int game;
    private String email;
    private String uid;


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        // permission to app working propely also on android 29+
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }
        initializeVariables();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getInt("game") != 0) {
            appScore = extras.getInt("appScore");
            game = extras.getInt("game");
            //The key argument here must match that used in the other activity
        }
        db = new SQLiteHandler(getApplicationContext());

        // pobieranie danych użytkownika z lokalnej bazy danych
        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");
        uid = user.get("uid");
        Log.e(TAG,"EMAIL: " + email);
        Log.e(TAG,"uid: " + uid);



        btnButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                editor.clear().apply();
            }

        });


        btnSend.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                updateStepsIntoMySql(email,convertStepsToString(),uid);
            }

        });
        getStepsFromMySql(email,uid); // jednorazowe pobranie danych(kroki, gra, punkty) z MySql po przejsciu do tej aktywnosci
        if(game == 0){
            game = Integer.parseInt(user.get("game"));
            appScore = Integer.parseInt(user.get("points"));
        }
//        startRepeatingTask(); // funkcja aktualizująca liczbę kroków z bazą danych co minutę
    }
    private void initializeVariables(){
        mHandler = new Handler();

        textViewStepCounter =  findViewById(R.id.textViewStepCounter);
        textViewStepDetector =  findViewById(R.id.textViewStepDetector);
        btnButton =  findViewById(R.id.btnButton);
        btnSend =  findViewById(R.id.btnSend);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Lokalna baza danych SQLite
        db = new SQLiteHandler(getApplicationContext());

        // SharedPreferences
        mPreferences = getPreferences(MODE_PRIVATE);
        editor = mPreferences.edit();
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (countSensor != null) {

            mSensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(this, "Sensor found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    // w przypadku gdy stan sensora się zmieni
    @Override
    public void onSensorChanged(SensorEvent event) {
        int additionStep = 0;
        int totalStepCountSinceReboot = (int) event.values[0]; // kroki zrobione od początku zainstalowania apliakcji
        textViewStepDetector.setText(today()); // tutaj test
        int todaySteps = getPreferences(today()); // przypisywanie zapisanej zmiennej do codziennych krokow
        milestoneStep = totalStepCountSinceReboot - 1;



        if (todaySteps == 0) {
            savePreferences(today(), 1); // zapisujemy dane, key- data dzisiejszego dnia, value - liczba krokow dzisiaj
        }
        else if(todaySteps == 10){
            savePreferences(today(), 10);
            updateStepsIntoMySql(email,String.valueOf(10),uid);
            editor.clear().commit();
            Intent intent = new Intent(StepCounterActivity.this, MainActivity.class);
            intent.putExtra("game",4);
            startActivity(intent);
            finish();
            return;
        }
        else {
            additionStep = totalStepCountSinceReboot - milestoneStep;
            savePreferences(today(), todaySteps + additionStep); // zapisujemy dane, key- data dzisiejszego dnia, value - liczba krokow dzisiaj
        }
        textViewStepCounter.setText(String.valueOf(getPreferences(today()))); // wyswietlenie wyniku na ekranie
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
//
//    Runnable mStatusChecker = new Runnable() { //
//        @Override
//        public void run() {
//            // Lokalna baza danych SQLite
//            db = new SQLiteHandler(getApplicationContext());
//            // pobieranie danych użytkownika z lokalnej bazy danych
//            HashMap<String, String> user = db.getUserDetails();
//            final String email = user.get("email");
//            final String id = user.get("id");
//            try{
//                    Log.d("TAG","empty try/catch for update");
//            }finally {
//                updateStepsIntoMySql(email,convertStepsToString(),id);
//                // 5 seconds by default, can be changed later
//                int mInterval = 60000; // update co 1 minute
//                mHandler.postDelayed(mStatusChecker, mInterval);
//            }
//        }
//    };

//    void startRepeatingTask() {
//        mStatusChecker.run();
//    }
//    void stopRepeatingTask() {
//        mHandler.removeCallbacks(mStatusChecker);
//    }

    private void savePreferences(String key, int value) {
        Log.d("TAG","Preferences saved, steps = " + value);
        editor.putInt(key, value);
        editor.commit();
    }

    private int getPreferences(String key) {
        return mPreferences.getInt(key, 0);

    }

    public String today() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(Calendar.getInstance().getTime());
    }

    // funkcja do aktualizowania krokow w MySQL
    private void updateStepsIntoMySql(final String email, final String steps, final String id) { // trzeba konwertowac inta steps do stringa
        // Tag używany do anulowania żądania
        String tag_string_req = "req_update";
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_SEND_DATA, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // funkcja odbierająca odpowiedz
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response + " Steps = " + steps + " Points= " + appScore+ " Game= " + game); // Dane dotyczące aktualizowania w Logcat'ie
                db.updateUser(id, steps,appScore,game,today());
                Log.w(TAG,"UPDATED_AT: " + today());
                hideDialog();

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url logowania
                Map<String, String> params = new HashMap<>();
                params.put("steps", steps);
                params.put("updated_at",today()); // updateowanie daty
                params.put("points",String.valueOf(appScore));
                params.put("game", String.valueOf(game));
                params.put("email", email);

                return params;
            }

        };


        // Dodanie zapytania do kolejki zapytań
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    private void getStepsFromMySql(final String email,final String id){
        final int stepss =0;
        // Tag używany do anulowania żądania
        String tag_string_req = "req_getSteps";

        pDialog.setMessage("Getting data ..");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_GET_STEPS, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi


            // funkcja odbierająca odpowiedz
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "GetSteps Response: " + response); // Dane dotyczące aktualizowania w Logcat'ie
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // sprawdz błąd
                    // jesli nie ma wykona się poniższa funkcja(użytkownik pomyślnie zalogowany)

                        JSONObject user = jObj.getJSONObject("user");
                        String steps = user.getString("steps");
                        String updated_at = user.getString("updated_at");
                        Log.e(TAG,updated_at);
                        String points = user.getString("points");
                        String game = user.getString("game");
                        if(updated_at.equals(today() + " 00:00:00")) {
                            if(!steps.equals("null")) {
                                db.updateUser(id, steps, Integer.parseInt(points), Integer.parseInt(game),today());
                                savePreferences(today(), Integer.parseInt(steps));
                            }
                            else {
                                db.updateUser(id, "0", 0, 0,today());
                                savePreferences(today(), 0);
                            }
                        }
                        else
                            savePreferences(today(),0);

                    } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "GetSteps Error: " + error.getMessage());
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

    private String convertStepsToString(){
        int steps = getPreferences(today());
        return String.valueOf(steps);
    }

}