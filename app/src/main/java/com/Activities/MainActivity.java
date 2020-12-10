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
import android.widget.TextView;
import android.widget.Toast;

import com.ChildrenEducationApp.R;
import com.SQLiteHelper.app.AppController;

import com.SQLiteHelper.helper.SQLiteHandler;
import com.SQLiteHelper.helper.SessionManager;

import com.facebook.stetho.Stetho;

import sarveshchavan777.quizgame.QuestionActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtEmail;
    private Button btnPlay;
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
        TextView txtName = (TextView) findViewById(R.id.name);
        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        Button btnStatistics = (Button) findViewById(R.id.btnInfo);
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



        btnStatistics.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                startActivity(intent);
            }
        });


        Stetho.initializeWithDefaults(this);
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
        return sdf.format(Calendar.getInstance().getTime())+ " 00:00:00";
    }

    public void start(View view) {
        // pobieranie danych użytkownika z lokalnej bazy danych
        HashMap<String, String> user = db.getUserDetails();
        String game;
        String steps = user.get("steps");
        String updated_at = user.get("updated_at");
        String poziom = user.get("poziom");
        Log.d("TODAY",today());

        if(!updated_at.equals(today())){ // jeśli dzień się zmienił zerujemy gry i kroki

            game = "0";
            steps = "0"; // ??

            if(Integer.parseInt(game) >= 3){
                Intent intent = new Intent(MainActivity.this, StepCounterActivity.class);
                startActivity(intent);
                finish();

            } else{
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                intent.putExtra("poziom",poziom);
                startActivity(intent);
                finish();
            }


        } else if(updated_at.equals(today())){ // jeśli dzień się nie zmienił kontynuujemy rozpoczęty proces

            game = user.get("game");

            if(game.equals("null")){
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                intent.putExtra("poziom",poziom);
                startActivity(intent);
                finish();
            }
            else if(Integer.parseInt(game) == 4 && Integer.parseInt(steps) < 10){
                Intent intent = new Intent(MainActivity.this, StepCounterActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(), "Wykonałeś wszystkie zadania, wróc ponownie jutro!", Toast.LENGTH_LONG).show();
            }
        }




    }
}