package sarveshchavan777.quizgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;


public class ResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView textResult = (TextView) findViewById(R.id.textResult);

        Bundle b = getIntent().getExtras();

        int score = b.getInt("score");

        textResult.setText("Wrong answer sorry!! Your points are " + " " + score);

    }

    public void playagain(View o) { // przejscie do kolejnej aktywnosci

        Intent intent = null;
        try {
            intent = new Intent(ResultActivity.this,
                    Class.forName("diamon.wordee.MainActivityWordee"));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}