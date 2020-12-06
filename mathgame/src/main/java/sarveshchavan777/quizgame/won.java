package sarveshchavan777.quizgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by LENOVO on 1/3/2017.
 */

public class won extends Activity {
    TextView tv;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.won);
        tv = (TextView) findViewById(R.id.congo);
        btn = (Button) findViewById(R.id.btnNextIntent);
        Bundle b = getIntent().getExtras();
        final int y = b.getInt("score");
        final String level = b.getString("level");
        tv.setText("FINAL SCORE:" + y);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = null;
                try {
                    intent = new Intent(won.this,
                            Class.forName("diamon.wordee.MainActivityWordee"));
                    intent.putExtra("appScore",y);
                    intent.putExtra("level",level);
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
