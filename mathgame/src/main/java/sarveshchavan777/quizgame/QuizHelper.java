package sarveshchavan777.quizgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuizHelper extends SQLiteOpenHelper {
    Context context;
    private static final int DATABASE_VERSION = 13;

    private static final String DATABASE_NAME = "mathgame";
    private static final String TABLE_QUEST = "quest";
    private static final String KEY_ID = "qid";
    private static final String KEY_QUES = "question";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_OPTA = "opta";
    private static final String KEY_OPTB = "optb";
    private static final String KEY_OPTC = "optc";
    String level;

    private SQLiteDatabase dbase;

    public QuizHelper(Context context, String level) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.level = level;

        Log.d("QuizHelper ", "konstruktor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("QuizHelper ", "onCreate");
        dbase = db;
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_QUEST + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_QUES
                + " TEXT, " + KEY_ANSWER + " TEXT, " + KEY_OPTA + " TEXT, "
                + KEY_OPTB + " TEXT, " + KEY_OPTC + " TEXT)";
        db.execSQL(sql);
        addQuestion();
    }

    private void addQuestion() {

        Log.d("QuizHelper ", "addQuestion1");
        InputStream is = context.getResources().openRawResource(R.raw.quizmath);
        String quizJson = getQuizJsonResource(is); // pobranie JSONA w formie Stringa
        ArrayList<Question> quizes = null;
        try{
            JSONObject quizObject = new JSONObject(quizJson); // tworzymy JSON object ze Stringa
            quizes = new ArrayList<>(); // lista przechowujaca pytania i odpowiedzi
            JSONArray quizesCategoriesArray = quizObject.getJSONArray("categories");
            String category = null; // inicjalizacja zmiennej
            for (int i = 0; i < quizesCategoriesArray.length(); i++) { // iterating over categories
                if(quizesCategoriesArray.getJSONObject(i).getString("category").equals(level)) {
                    JSONArray quizesJsonArray = quizesCategoriesArray.getJSONObject(i).getJSONArray("quizes");
                    for (int j = 0; j < quizesJsonArray.length(); j++) {   // iterujemy po quizach
                        String question = quizesJsonArray.getJSONObject(j).getString("question"); // pobieranie pytania
                        JSONArray answersJSON = quizesJsonArray.getJSONObject(j).getJSONArray("choices");
                        String [] answers= new String[answersJSON.length()];
                        for (int p = 0; p < answersJSON.length(); p++) {
                            answers[p] = answersJSON.getString(p);
                        }
                        quizes.add(new Question(question,answers[0],answers[1],answers[2],answers[3])); // ustawianie zawartosci oraz naglowka
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
        this.addQuestion(quizes);
    }
    public static final String getQuizJsonResource(InputStream is){
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return writer.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUEST);

        onCreate(db);
    }


    public void addQuestion(Question quest) {
        ContentValues values = new ContentValues();
        values.put(KEY_QUES, quest.getQUESTION());
        values.put(KEY_ANSWER, quest.getANSWER());
        values.put(KEY_OPTA, quest.getOPTA());
        values.put(KEY_OPTB, quest.getOPTB());
        values.put(KEY_OPTC, quest.getOPTC());


        dbase.insert(TABLE_QUEST, null, values);
    }
    public void addQuestion(ArrayList<Question> questions) {

        Log.d("QuizHelper ", "addQuestion");
        if(questions.isEmpty())
            Log.d("QuizHelper ", "questionsAreEmpty");
        for (Question question : questions) {
            ContentValues values = new ContentValues();
            values.put(KEY_QUES, question.getQUESTION());
            values.put(KEY_ANSWER, question.getANSWER());
            values.put(KEY_OPTA, question.getOPTA());
            values.put(KEY_OPTB, question.getOPTB());
            values.put(KEY_OPTC, question.getOPTC());

            dbase.insert(TABLE_QUEST, null, values);
        }

    }

    public List<Question> getAllQuestions() {
        Log.d("QuizHelper ", "getAllQuestions");
        List<Question> quesList = new ArrayList<Question>();

        String selectQuery = "SELECT  * FROM " + TABLE_QUEST;
        dbase = this.getReadableDatabase();
        Cursor cursor = dbase.rawQuery(selectQuery, null);

        while (cursor.moveToNext()) {
            Question quest = new Question();
            quest.setID(cursor.getInt(0));
            quest.setQUESTION(cursor.getString(1));
            quest.setANSWER(cursor.getString(2));
            quest.setOPTA(cursor.getString(3));
            quest.setOPTB(cursor.getString(4));
            quest.setOPTC(cursor.getString(5));

            quesList.add(quest);
        }

        return quesList;
    }
    public void deleteDatabase(){
        context.deleteDatabase(DATABASE_NAME);
    }


}
