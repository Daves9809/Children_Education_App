package com.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.loginandregistration.R;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewStepCounter, textViewStepDetector;
    private SensorManager mSensorManager;
    private boolean running = false;
    int stepCount = 0, stepDetect = 0;
    private Sensor countSensor, detectorSensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        // permission to app working propely also on s10e
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},1);
        }

        textViewStepCounter = (TextView) findViewById(R.id.textViewStepCounter);
        textViewStepDetector = (TextView) findViewById(R.id.textViewStepDetector);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        detectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);


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
        if (detectorSensor != null) {
            mSensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Detector Sensor found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Detector Sensor not found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
            stepCount = (int) event.values[0];
            textViewStepCounter.setText(String.valueOf(stepCount));
            if(stepCount == 400) {
                stepCount = 0;
                Toast.makeText(this, "Na dzis koniec", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}