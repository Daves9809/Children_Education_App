package com.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private TextView tvStepCounter, tvStepsToDO,tvDistance;
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
    private String uid;
    private String poziom;
    private String basicPoints;
    private int levelToSend;
    private int pointsToSend = 0;
    private boolean zeroSteps;
    private int todaySteps;

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
        // pobieranie potrzebnych danych użytkownika z lokalnej bazy danych
        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");
        uid = user.get("uid");
        poziom = user.get("poziom");
        basicPoints = user.get("points");
        tvStepsToDO.setText(getString(R.string.dailySteps) + String.valueOf(Integer.parseInt(poziom) * 100));

        savePreferences("score",Integer.parseInt(basicPoints));

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getInt("game") != 0) {
            appScore = extras.getInt("appScore");
            game = extras.getInt("game");
//            savePreferences("score",appScore+Integer.parseInt(basicPoints));
        }

        getDataFromMySql(email,uid,poziom);
    }
    private void initializeVariables(){
        Handler mHandler = new Handler();

        tvStepCounter =  findViewById(R.id.textViewStepCounter);
        tvStepsToDO = findViewById(R.id.stepsToDo);
        tvDistance = findViewById(R.id.textViewDistance);
        zeroSteps = false;

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        db = new SQLiteHandler(getApplicationContext(),"android_user");

        mPreferences = getPreferences(MODE_PRIVATE);
        editor = mPreferences.edit();
    }

    @Override
    public void onDestroy() {
        if(getPreferences(today()) >= Integer.parseInt(poziom)*100) {
            Log.d(TAG," data cleared, sensor unregistered");
            editor.clear().commit();
            mSensorManager.unregisterListener(this);
        }
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
        if(getPreferences(today()) >= Integer.parseInt(poziom)*100) {
            Log.d(TAG," data cleared, sensor unregistered");
            mSensorManager.unregisterListener(this);
        }
        super.onPause();
    }

    // w przypadku gdy stan sensora się zmieni
    @Override
    public void onSensorChanged(SensorEvent event) {
        int additionStep = 0;
        int totalStepCountSinceReboot = (int) event.values[0]; // kroki od ostatniego resetu
        todaySteps = getPreferences(today());
        int milestoneStep = totalStepCountSinceReboot - 1;



        if (todaySteps == 0) {
            if(!zeroSteps){
                savePreferences(today(), 0); // zapisywanie danych jako klucz-wartość z tego dnia
                zeroSteps = true;
            } else
                savePreferences(today(), 1);
        }
        else if(todaySteps == Integer.parseInt(poziom) *100){
            savePreferences(today(), 100);
            updateData(email,String.valueOf(Integer.parseInt(poziom) *100), poziom,uid);
            Intent intent = new Intent(StepCounterActivity.this, MainActivity.class);
            intent.putExtra("game",4);
            startActivity(intent);
            finish();
            return;
        }
        else {
            additionStep = totalStepCountSinceReboot - milestoneStep;
            savePreferences(today(), todaySteps + additionStep);
        }
        tvDistance.setText(String.valueOf(countDistance(getPreferences(today()))));
        updateProgressBar(progressBar ,poziom,getPreferences(today())); // update'owanie pasku progresu
        tvStepCounter.setText(String.valueOf(getPreferences(today()))); // wyświetlenie wyniku na ekranie
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private void savePreferences(String key, int value) {
        Log.d("TAG","Preferences saved, steps = " + value);
        editor.putInt(key, value);
        editor.commit();
    }

    private int getPreferences(String key) {
        return mPreferences.getInt(key, 0);

    }

    // Aktualizowanie danych w mySql i SqLite
    private void updateData(final String email, final String steps, final String poziom, final String id) { // trzeba konwertowac inta steps do stringa
        // Tag używany do anulowania żądania
        String tag_string_req = "req_update";

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_SEND_DATA, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // method which gets response
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response + " Steps = " + steps + " Points= " + appScore+ " Game= " + game + " Poziom= " + poziom + " today()= " + today());
                if(!basicPoints.equals("null")){
                    if((Integer.parseInt(steps) <= Integer.parseInt(poziom) * 100) && (Integer.parseInt(basicPoints)  + appScore >= Integer.parseInt(poziom) * 10)){
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
                    if((Integer.parseInt(steps) <= Integer.parseInt(poziom) * 100) && (appScore >= Integer.parseInt(poziom) * 10)) {
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

    private void getDataFromMySql(final String email, final String id, final String basicPoziom){
        String tag_string_req = "req_getSteps";

        pDialog.setMessage("Getting data ..");

        StringRequest strReq = new StringRequest(Request.Method.POST, // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
                AppConfig.URL_GET_STEPS, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi


            // funkcja odbierająca odpowiedz
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "GetSteps Response: " + response); // Dane dotyczące aktualizowania w Logcat'ie
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");


                        JSONObject user = jObj.getJSONObject("user");
                        String steps = user.getString("steps");
                        String updated_at = user.getString("updated_at");
                        String points = user.getString("points");
                        String game = user.getString("game");
                        if(updated_at.equals(today())) {
                            if(!steps.equals("null") && Integer.parseInt(steps) < (Integer.parseInt(poziom) *10)) {
                                db.updateUser(id, steps, Integer.parseInt(points), Integer.parseInt(game),Integer.parseInt(poziom),today());
                                savePreferences(today(), Integer.parseInt(steps));
                                Log.w(TAG,"1");
                            }
                            else if(steps.equals("null")){ // kiedy konto jest nowe
                                db.updateUser(id, "0", 0, 4,Integer.parseInt(basicPoziom),today());
                                savePreferences(today(), 0);
                                Log.w(TAG,"2");
                            }
                            else{
                                Log.w(TAG,"3");
                                Intent intent = new Intent(StepCounterActivity.this, MainActivity.class);
                                intent.putExtra("game",4);
                                startActivity(intent);
                                finish();
                            }
                        }
                        else{
                            Log.w(TAG,"4");
                            Log.w("updated_at= ",updated_at);
                            Log.w("today()= ",today());
                            savePreferences(today(),0);
                        }

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

    public String today() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(Calendar.getInstance().getTime()) + " 00:00:00";
    }


    private String convertStepsToString(){
        int steps = getPreferences(today());
        return String.valueOf(steps);
    }

    private void updateProgressBar(ProgressBar progressBar, String poziom, int steps) {
        double doubleSteps = steps;
        progressBar.setMax(Integer.parseInt(poziom)*100);
        if (!poziom.equals("null")) {
            progressBar.setProgress(steps);
        } else {
            progressBar.setProgress(0);
        }
    }

    private double countDistance(int steps){
        double doubleSteps = steps;
        return (doubleSteps/1250)*1000;
    }

}