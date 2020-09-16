package loginandregistration.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import java.util.HashMap;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loginandregistration.R;
import com.stepcounter.StepCounterActivity;

import loginandregistration.helper.SQLiteHandler;
import loginandregistration.helper.SessionManager;

public class MainActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnPlay;

    private SQLiteHandler db;
    private SessionManager session;

    //utworzenie widoku wraz z logiką
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //przypisanie zmiennym odpowiednich elementów w przestrzeni activity_main.xml
        txtName = (TextView) findViewById(R.id.name);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnPlay = (Button) findViewById(R.id.btnPlay);

        // Definiowanie lokalnego programu obsługi bazy danych
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // pobieranie danych użytkownika z lokalnej bazy danych
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
                Intent intent = new Intent(MainActivity.this, StepCounterActivity.class);
                startActivity(intent);
            }
        });
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
}