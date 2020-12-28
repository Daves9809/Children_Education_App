package com.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.SQLiteHelper.helper.SessionManager;
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
import android.widget.ProgressBar;
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
    private TextView tvStepCounter, tvStepsToDO, tvDistance;
    ProgressBar progressBar;
    private SensorManager mSensorManager;
    private Sensor countSensor;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;
    private SQLiteHandler db;
    private ProgressDialog pDialog;
    private int appScore;
    private int game;
    private String email;
    private String name;
    private String uid;
    private String poziom;
    private String basicPoints;
    private int levelToSend;
    private int pointsToSend = 0;
    private boolean zeroSteps;
    private int todaySteps;
    private int bPoints;
    private SessionManager session;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        // pozwolenie na poprawne działanie aplikacji również na Androidzie 29+
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            // zapytanie o pozwolenie
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }
        initializeVariables();
        session = new SessionManager(getApplicationContext());
        // pobieranie potrzebnych danych użytkownika z lokalnej bazy danych
        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");
        name = user.get("name");
        uid = user.get("uid");
        poziom = user.get("poziom");
        basicPoints = user.get("points"); // to będzie miało wartość w przypadku gdy użytkownik nie jest nowy
        if (basicPoints.equals("null"))
            bPoints = 0;
        else
            bPoints = Integer.parseInt(basicPoints);

        tvStepsToDO.setText(getString(R.string.dailySteps) + String.valueOf(Integer.parseInt(poziom) * 10));


        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getInt("game") != 0) {
            appScore = extras.getInt("appScore");
            game = extras.getInt("game");
        }

        if (getPreferencesString("name").equals("null")) { // w przypadku zresetowania lub nowego konta
            Log.d("test", "1");
            Toast.makeText(getApplicationContext(), "nie wyłączono apki", Toast.LENGTH_SHORT).show();
            if (getPreferencesInt("pointss") == 0) { // czyli nastąpiło kasowanie na skutek przelogowania lub skonczenia aktywnosci
                savePreferencesInt("pointss", bPoints + appScore);
                savePreferences("poziom", poziom);
            } else {
                savePreferencesInt("pointss", appScore);
                savePreferences("poziom", poziom);
            }

        } else if (getPreferencesString("name").equals(name)) { // nie nastąpiło przelogowanie, ale nastapilo wyjscie z aplikacji lub
            Log.d("test", "2");
            Toast.makeText(getApplicationContext(), "nie nastąpiło przelogowanie", Toast.LENGTH_SHORT).show();
            if (getPreferencesInt("pointss") == 0) { // czyli nie nastąpiło kasowanie na skutek przelogowania lub wypełnienia zadania
                savePreferencesInt("pointss", bPoints + appScore);
                savePreferences("poziom", poziom);
            }
        } else if (!getPreferencesString("name").equals(name)) { // nastąpiło przelogowanie
            Log.d("test", "3");
            Toast.makeText(getApplicationContext(), "nastąpiło przelogowanie", Toast.LENGTH_SHORT).show();
            editor.clear().commit(); // czyszczenie podręcznych danych
            savePreferences("name",name);
            savePreferencesInt("pointss", appScore);
            savePreferences("poziom", poziom);
        }

    }

    private void initializeVariables() {
        Handler mHandler = new Handler();

        tvStepCounter = findViewById(R.id.textViewStepCounter);
        tvStepsToDO = findViewById(R.id.stepsToDo);
        tvDistance = findViewById(R.id.textViewDistance);
        zeroSteps = false;

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        db = new SQLiteHandler(getApplicationContext(), "android_user");

        mPreferences = getPreferences(MODE_PRIVATE);
        editor = mPreferences.edit();
    }

    @Override
    public void onDestroy() {
        if (getPreferencesInt(today()) >= Integer.parseInt(poziom) * 10) { // jesli uzytkownik zbierze wyznaczona liczbe punktow
            Log.d(TAG, " data cleared, sensor unregistered");
            editor.clear().commit();
            mSensorManager.unregisterListener(this);
        }
        else if (getPreferencesInt(today()) < Integer.parseInt(poziom) * 10) {
            db.updateUser("0", 0, 4, 1, today());
        }
        savePreferences("name", name); // przy usunięciu aktywności zabezpieczenie przed przelogowaniem
        mSensorManager.unregisterListener(this); // wyłączanie sensora
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
        if (getPreferencesInt(today()) >= Integer.parseInt(poziom) * 100) {
            Log.d(TAG, " data cleared, sensor unregistered");
            mSensorManager.unregisterListener(this);
        }
        super.onPause();
    }

    // w przypadku gdy stan sensora się zmieni
    @Override
    public void onSensorChanged(SensorEvent event) {
        int additionStep = 0;
        int totalStepsCountSinceReboot = (int) event.values[0]; // kroki od ostatniego resetu
        todaySteps = getPreferencesInt(today());
        int milestoneStep = totalStepsCountSinceReboot - 1;

        String level = getPreferencesString("poziom");
        int points = getPreferencesInt("pointss");


        if (todaySteps == 0) {
            firstRun();
        } else if (todaySteps >= Integer.parseInt(level) * 10) {
            updateDatabases(level, points);
            return;
        } else {
            additionStep = totalStepsCountSinceReboot - milestoneStep;
            savePreferencesInt(today(), todaySteps + additionStep);
        }
        setDataOnDisplay();
        updateProgressBar(progressBar, level, getPreferencesInt(today()));
    }

    private void setDataOnDisplay() {
        tvDistance.setText(String.valueOf(countDistance(todaySteps)));
        tvStepCounter.setText(String.valueOf(getPreferencesInt(today())));
    }

    private void updateDatabases(String level, int points) {
        savePreferencesInt(today(), Integer.parseInt(level) * 10);
        if (points >= Integer.parseInt(level) * 10) {
            points = points - Integer.parseInt(level) * 10;
            updateData(String.valueOf(Integer.parseInt(level) * 10), Integer.parseInt(level)+1, uid, points);
            session.setLevelUp(true); // ustawiamy flage poniewaz level sie zwieksyl
        }
        else{
            updateData(String.valueOf(Integer.parseInt(level) * 10), Integer.parseInt(level), uid, points);
        }
        Intent intent = new Intent(StepCounterActivity.this, MainActivity.class);
        intent.putExtra("game", 4);
        startActivity(intent);
        finish();
        return;
    }

    private void firstRun() {
        if (!zeroSteps) {
            savePreferencesInt(today(), 0); // zapisywanie danych jako klucz-wartość z tego dnia
            zeroSteps = true;
        } else
            savePreferencesInt(today(), 1);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private void savePreferencesInt(String key, int value) {
        Log.d("TAG", "Preferences saved, steps = " + value);
        editor.putInt(key, value);
        editor.commit();
    }

    private void savePreferences(String key, String value) {
        Log.d("TAG", "Preferences saved, name = " + value);
        editor.putString(key, value);
        editor.commit();
    }

    private int getPreferencesInt(String key) {
        return mPreferences.getInt(key, 0);

    }

    private String getPreferencesString(String key) {
        return mPreferences.getString(key, "null");

    }

    // Aktualizowanie danych w mySql i SqLite
    private void updateData(final String steps, final int level, final String id, final int points) { // ta funkcja do poprawy
        // Tag używany do anulowania żądania
        String tag_string_req = "req_update";

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_SEND_DATA, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // method which gets response
            @Override
            public void onResponse(String response) {

                    db.updateUser(steps, points, 4, level, today());

                Log.d(TAG, "Update Response: " + response + " Steps = " + steps + " Points= " + points + " Game= " + 4 + " Poziom= " + level + " today()= " + today());

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
                params.put("steps", steps);
                params.put("updated_at", today());
                params.put("points", String.valueOf(points));
                params.put("game", String.valueOf(4));
                params.put("poziom", String.valueOf(level));

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


    private String convertStepsToString() {
        int steps = getPreferencesInt(today());
        return String.valueOf(steps);
    }

    private void updateProgressBar(ProgressBar progressBar, String level, int steps) {
        double doubleSteps = steps;
        progressBar.setMax(Integer.parseInt(level) * 10);
        if (!level.equals("null")) {
            progressBar.setProgress(steps);
        } else {
            progressBar.setProgress(0);
        }
    }

    private double countDistance(int steps) {
        return ((double) steps / 1250) * 1000;
    }

}