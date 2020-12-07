package com.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ChildrenEducationApp.BuildConfig;
import com.ChildrenEducationApp.R;
import com.SQLiteHelper.app.AppController;
import com.StepCounter.StepCounterActivity;

import com.SQLiteHelper.helper.SQLiteHandler;
import com.SQLiteHelper.helper.SessionManager;

import com.LoginAndRegistration.Activity.LoginActivity;
import com.amitshekhar.DebugDB;
import com.facebook.stetho.Stetho;

import diamon.wordee.MainActivityWordee;
import sarveshchavan777.quizgame.QuestionActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnPlay;
    private Button btnStatistics;
    private Button btnSettings;

    private SQLiteHandler db;
    private SessionManager session;

    //utworzenie widoku wraz z logiką
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = AppController.getInstance();

        //przypisanie zmiennym odpowiednich elementów w przestrzeni activity_mainn.xml
        txtName = (TextView) findViewById(R.id.name);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnStatistics = (Button) findViewById(R.id.btnInfo);
        btnSettings = (Button) findViewById(R.id.btnSettings) ;

        // Definiowanie lokalnego programu obsługi bazy danych
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        HashMap<String, String> user = db.getUserDetails();
        String name = user.get("name");
        // Wyświetlenie danych podstawowych danych użytkownika na ekranie
        txtName.setText(name);

        // przycisk do wylogowania
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // pobieranie danych użytkownika z lokalnej bazy danych
                HashMap<String, String> user = db.getUserDetails();
                String game;
                String steps;
                String points = user.get("points");
                String updated_at = user.get("updated_at");
                String poziom = user.get("poziom");

                if(!updated_at.equals(today())){ // jeśli dzień się zmienił zerujemy gry i kroki

                    game = "0";
                    steps = "0"; // ??

                        if(Integer.parseInt(game) >= 3){
                            Intent intent = new Intent(MainActivity.this, StepCounterActivity.class);
                            intent.putExtra("updated_at",updated_at);
                            startActivity(intent);
                            finish();
                            Log.d("mainActivity:","1 "+ updated_at + " " + today());

                        } else{
                            Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                            intent.putExtra("poziom",poziom);
                            startActivity(intent);
                            finish();
                            Log.d("mainActivity:","2 "+ updated_at + " " + today());
                        }


                } else if(updated_at.equals(today())){ // jeśli dzień się nie zmienił kontynuujemy rozpoczęty proces

                    game = user.get("game");

                    if(game.equals("null")){
                        Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                        intent.putExtra("poziom",poziom);
                        startActivity(intent);
                        finish();
                        Log.d("mainActivity:","4 "+ updated_at + " " + today() + " 00:00:00");
                    } else{
                        Log.d("mainActivity:","5 "+ updated_at + " " + today());
                        Toast.makeText(getApplicationContext(), "Wykonałeś wszystkie zadania, wróc ponownie jutro!", Toast.LENGTH_LONG).show();
                    }
                }


            }
        });

        btnStatistics.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                startActivity(intent);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivityWordee.class);
                startActivity(intent);
            }
        });

        Stetho.initializeWithDefaults(this);
    }
    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
            } catch (Exception ignore) {

            }
        }
    }

    /**
     * Wylogowywanie użytkownika. Operacja ta ustawi flagę isLoggedIn na false, a Shared
     * Preferences wyczysci podreczne dane uzytkownika w sqLiteDatabase
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Zainicjowanie LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public String today() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(Calendar.getInstance().getTime());
    }
}