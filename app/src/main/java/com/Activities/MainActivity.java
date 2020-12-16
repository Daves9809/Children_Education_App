package com.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ChildrenEducationApp.R;
import com.SQLiteHelper.app.AppController;

import com.SQLiteHelper.helper.SQLiteHandler;
import com.SQLiteHelper.helper.SessionManager;

import com.facebook.stetho.Stetho;


import sarveshchavan777.quizgame.QuestionActivity;

public class MainActivity extends AppCompatActivity {

    //definiowanie zmiennych
    private static final String TAG = MainActivity.class.getSimpleName();
    private SQLiteHandler db;
    private SessionManager session;
    private TextView imieLayout, poziomLayout, punktyLayout, dataLayout;
    private String imie, poziom, punkty, data;

    //utworzenie widoku wraz z logiką
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = AppController.getInstance();

        //przypisanie zmiennym odpowiednich elementów w przestrzeni activity_main.xml
        TextView txtName = (TextView) findViewById(R.id.name);
        imieLayout = (TextView) findViewById(R.id.imieLayout);
        poziomLayout = (TextView) findViewById(R.id.poziomLayout);
        punktyLayout = (TextView) findViewById(R.id.punktyLayout);
        dataLayout = (TextView) findViewById(R.id.dataLayout);
        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        Button btnStatistics = (Button) findViewById(R.id.btnInfo);
        Button btnEdit = findViewById(R.id.btnEdit);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        // Definiowanie lokalnego programu obsługi bazy danych
        db = new SQLiteHandler(getApplicationContext(), "android_user");

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        getUserData();
        transformData();
        setDataOnDisplay();


        if (poziom != null && punkty != null)
            updateProgressBar(progressBar, poziom, punkty);

        // przycisk do wylogowania
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });


        btnStatistics.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatisticisActivity.class);
                startActivity(intent);
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });


        Stetho.initializeWithDefaults(this);
    }

    private void transformData() {
        char[] chars = new char[10];
        for (int i = 0; i < 10; i++) {
            chars[i] = data.charAt(i);
        }
        data = String.valueOf(chars);
    }

    private void setDataOnDisplay() {
        imieLayout.setText(imie);
        poziomLayout.setText(poziom);
        if (!punkty.equals("null"))
            punktyLayout.setText(punkty);
        else
            punktyLayout.setText("0");
        dataLayout.setText(data);
    }

    private void getUserData() {
        HashMap<String, String> user = db.getUserDetails();
        imie = user.get("name");
        Log.d("mainact", imie);
        poziom = user.get("poziom");
        punkty = user.get("points");
        data = user.get("created_at");
    }

    private void updateProgressBar(ProgressBar progressBar, String poziom, String points) {
        if (!poziom.equals("null") && !points.equals("null")) {
            progressBar.setProgress((int) ((Double.parseDouble(points) / (Double.parseDouble(poziom) * 10)) * 100));
        } else {
            progressBar.setProgress(0);
        }
    }

    /**
     * Wylogowywanie użytkownika. Operacja ta ustawi flagę isLoggedIn na false, a Shared
     * Preferences wyczysci podreczne dane uzytkownika w sqLiteDatabase
     */
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
        return sdf.format(Calendar.getInstance().getTime()) + " 00:00:00";
    }

    public void start(View view) {
        // pobieranie danych użytkownika z lokalnej bazy danych
        HashMap<String, String> user = db.getUserDetails();
        String game;
        String steps = user.get("steps");
        String updated_at = user.get("updated_at");
        String poziom = user.get("poziom");
        Log.d("TODAY", today());

        if (!updated_at.equals(today())) { // jeśli dzień się zmienił zerujemy gry i kroki

            game = "0";
            steps = "0"; // ??

            if (Integer.parseInt(game) >= 3) {
                Intent intent = new Intent(MainActivity.this, StepCounterActivity.class);
                startActivity(intent);
                finish();

            } else {
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                intent.putExtra("poziom", poziom);
                startActivity(intent);
                finish();
            }


        } else if (updated_at.equals(today())) { // jeśli dzień się nie zmienił kontynuujemy rozpoczęty proces

            game = user.get("game");

            if (game.equals("null")) {
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                intent.putExtra("poziom", poziom);
                startActivity(intent);
                finish();
            } else if (Integer.parseInt(game) == 4 && Integer.parseInt(steps) < (Integer.parseInt(poziom)*100)) {
                Intent intent = new Intent(MainActivity.this, StepCounterActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Wykonałeś wszystkie zadania, wróc ponownie jutro!", Toast.LENGTH_LONG).show();
            }
        }


    }
}