package com.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.loginandregistration.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import loginandregistration.app.AppConfig;
import loginandregistration.app.AppController;
import loginandregistration.helper.SQLiteHandler;

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
    private BroadcastReceiver minuteUpdateReceiver;


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
        // pobieranie danych użytkownika z lokalnej bazy danych
        HashMap<String, String> user = db.getUserDetails();
        final String email = user.get("email");

        // SharedPreferences
        mPreferences = getPreferences(MODE_PRIVATE);
        editor = mPreferences.edit();

//        startMinuteUpdater(email,convertStepsToString());

        btnButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                editor.clear().apply();
            }

        });


        btnSend.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                updateSteps(email,convertStepsToString());
            }

        });

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

        textViewStepCounter.setText(String.valueOf(getPreferences(today())));

        if (todaySteps == 0) {
            savePreferences(today(), 1); // zapisujemy dane, key- data dzisiejszego dnia, value - liczba krokow dzisiaj
        } else {
            additionStep = totalStepCountSinceReboot - milestoneStep;
            savePreferences(today(), todaySteps + additionStep); // zapisujemy dane, key- data dzisiejszego dnia, value - liczba krokow dzisiaj
        }
        //Logcat pomocniczy
        Log.i("TAG", "TodayStep " + getPreferences(today()));
        Log.i("TAG", "AdditionStep" + additionStep);
        Log.i("TAG", "TotalStepCount" + totalStepCountSinceReboot);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void savePreferences(String key, int value) {
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
    private void updateSteps(final String email, final String steps) { // trzeba konwertowac inta steps do stringa
        // Tag używany do anulowania żądania
        String tag_string_req = "req_update";

        pDialog.setMessage("Updating ..");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_SEND_DATA, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // funkcja odbierająca odpowiedz
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response); // Dane dotyczące aktualizowania w Logcat'ie
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
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Wysyłanie parametrów do adresu url logowania
                Map<String, String> params = new HashMap<>();
                params.put("steps", steps);
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

    //funkcja do aktualizacji danych w bazie danych co minute
    public void startMinuteUpdater(final String email, final String steps){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        minuteUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSteps(email,steps);
            }
        };
        registerReceiver(minuteUpdateReceiver, intentFilter);
    }

}