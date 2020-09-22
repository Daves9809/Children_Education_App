package com.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.loginandregistration.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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
import java.util.Map;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewStepCounter, textViewStepDetector;
    private Button btnButton;
    private SensorManager mSensorManager;
    private boolean running = false;
    private Sensor countSensor;
    private int milestoneStep;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;


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

        textViewStepCounter = (TextView) findViewById(R.id.textViewStepCounter);
        textViewStepDetector = (TextView) findViewById(R.id.textViewStepDetector);
        btnButton = (Button) findViewById(R.id.btnButton);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // SharedPreferences
        mPreferences = getPreferences(MODE_PRIVATE);
        editor = mPreferences.edit();

        btnButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
               editor.clear().apply();
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
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
        //Logcat
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

    //kod ktory sprawdza czy juz jest kolejny dzien
//    public void nextDay() {
//        Map<String, ?> allEntries = mPreferences.getAll();
//        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//            Log.v("map values", entry.getKey() + ": " + entry.getValue().toString());
//            if (entry.getKey().equals(today())) {
//                editor.clear().apply();
//                Log.v("TAG", "shared preferences " + today() + " deleted");
//            }
//        }
//    }
}