package com.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import com.loginandregistration.R;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewStepCounter, textViewStepDetector;
    private SensorManager mSensorManager;
    private boolean running = false;
    int stepCount = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        textViewStepCounter = (TextView) findViewById(R.id.textViewStepCounter);
        textViewStepDetector = findViewById(R.id.textViewStepDetector);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);



    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null){
            mSensorManager.registerListener(this,countSensor,SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(this, "Sensor found", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"Sensor not found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(running){
            stepCount = (int) event.values[0];
            textViewStepCounter.setText(String.valueOf(stepCount));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}