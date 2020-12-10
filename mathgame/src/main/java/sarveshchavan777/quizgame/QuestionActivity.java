package sarveshchavan777.quizgame;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class QuestionActivity extends Activity {
    List<Question> quesList;
    int score = 0;
    int qid = 0;
    Boolean isEnd= false;
    String poziom;


    Question currentQ;
    TextView txtQuestion, times, scored;
    Button button1, button2, button3;
    QuizHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainn);

        txtQuestion = (TextView) findViewById(R.id.txtQuestion);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        Bundle extras = getIntent().getExtras(); // pobieranie punktow z poprzedniej aktywnosci
        if (extras != null) {
            poziom = extras.getString("poziom");
        }

        db = new QuizHelper(this,poziom);
        quesList = db.getAllQuestions();
        currentQ = quesList.get(qid);


        scored = (TextView) findViewById(R.id.score);


        times = (TextView) findViewById(R.id.timers);


        setQuestionView();
        times.setText("00:02:00");


        CounterClass timer = new CounterClass(60000, 1000);
        timer.start();





        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                getAnswer(button1.getText().toString());
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnswer(button2.getText().toString());
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnswer(button3.getText().toString());
            }
        });
    }

    public void getAnswer(String AnswerString) {
        if (currentQ.getANSWER().equals(AnswerString)) {


            score++;
            scored.setText("Score : " + score);
        } else {
            Log.d("ELSE","1");


            Intent intent = new Intent(QuestionActivity.this,
                    ResultActivity.class);

            isEnd = true;
            Bundle b = new Bundle();
            b.putInt("score", score);
            b.putString("poziom",poziom);
            intent.putExtras(b);
            startActivity(intent);
            finish();
            db.deleteDatabase();
        }
        if (qid <= 4 ) {


            currentQ = quesList.get(qid);
            setQuestionView();
        } else if(!isEnd){
            Log.d("ELSE","2");

            Intent intent = new Intent(QuestionActivity.this,won.class);
            Bundle b = new Bundle();
            b.putInt("score",score);
            b.putString("poziom",poziom);
            intent.putExtras(b);
            startActivity(intent);
            finish();
            db.deleteDatabase();
        }


    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    public class CounterClass extends CountDownTimer {

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            // TODO Auto-generated constructor stub
        }


        @Override
        public void onFinish() {
            times.setText("Time is up");

        }

      @Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub

            long millis = millisUntilFinished;
            String hms = String.format(
                    "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(millis)));
            times.setText(hms);
        }


    }

    private void setQuestionView() {

        txtQuestion.setText(currentQ.getQUESTION());
        button1.setText(currentQ.getOPTA());
        button2.setText(currentQ.getOPTB());
        button3.setText(currentQ.getOPTC());

        qid++;
    }


}

