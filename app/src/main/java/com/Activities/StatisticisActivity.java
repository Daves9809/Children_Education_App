package com.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.Adapter.User;
import com.ChildrenEducationApp.R;
import com.SQLiteHelper.app.AppConfig;
import com.SQLiteHelper.helper.SQLiteHandler;
import com.Adapter.UserAdapter;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class StatisticisActivity extends AppCompatActivity {

    private static final String TAG = StatisticisActivity.class.getSimpleName();
    private SQLiteHandler db;
    private ArrayList<User> users;
    private UserAdapter userAdapter;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list);

        db = new SQLiteHandler(getApplicationContext(),"android_users");
        users = new ArrayList<>();
        listView = (ListView) findViewById(R.id.list);

        getUsers();
    }

    // Funkcja sprawdzająca poprawność danych w MySql oraz jednoczesnie aktualizaujaca dane w SQLite
    private void getUsers() {
        String tag_string_req = "req_getUsers";


        // zapytanie do bazy danych pod adresem AppConfig.URL_LOGIN
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_ALL_USERS, new Response.Listener<String>() { // stworzenie obiektu sluchacza odpowiedzi

            // funkcja odbierająca odpowiedz z pliku php
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "GetAllUsers response: " + response); // Dane dotyczące logowania w Logcat'ie

                try {
                    JSONArray array = new JSONArray(response);
                    for(int i =0;i<array.length();i++){
                        JSONObject object = array.getJSONObject(i);
                        String name = object.getString("name");
                        Integer poziom = object.getInt("poziom");
                        Integer points = object.getInt("points");


                        User user = new User(name, poziom, points);

                        users.add(user);
                        Collections.sort(users);
                    }

                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                userAdapter = new UserAdapter(getApplicationContext(),users);

                listView.setAdapter(userAdapter);


            }
        }, new Response.ErrorListener() { // utworzenie instancji obiektu Response.ErrorListener()

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "StatististicsActivity Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Dodanie zapytania do kolejki zapytań
//        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        Volley.newRequestQueue(StatisticisActivity.this).add(strReq);
    }

}