package com.donkeyboatworks.teachmetorah;

import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ActivityViewText extends ActionBarActivity {

    TextView textVerses;
    ListView translationListView;
    ArrayAdapter mArrayAdapter;
    ArrayList mTranslationList = new ArrayList();

    VersesDB versesDB;

    private Integer currentPage = 1;
    private String nextPage;
    private String prevPage;

    private static final String SEFARIA_URL =  "http://www.sefaria.org/api/";
    private static final String TOC_URI = "index/";
    private static final String TEXTS_URI =  "texts/";
    private static final String SHULCHAN_ARUCH = "Shulchan_Arukh%2C_Orach_Chayyim";
    private static final String MISHNAH_BERURAH = "Mishnah_Berurah";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_text);

        // Enable the "Up" button for more navigation options
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        versesDB = new VersesDB(this);

        textVerses = (TextView)findViewById(R.id.textVerses);
        textVerses.setMovementMethod(new ScrollingMovementMethod());
        textVerses.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return handleOnTouch(v, event);
            }
        });

        translationListView = (ListView) findViewById(R.id.translation_listview);
        mArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mTranslationList);
        translationListView.setAdapter(mArrayAdapter);


        Bundle extras = this.getIntent().getExtras();
        Integer bookId = null;
        Integer chapterNum = 1;
        if (extras != null) {
            bookId = (Integer) extras.get("bookId");
            chapterNum = (Integer) extras.get("chapterNum");
        }
        getVerses(bookId, chapterNum);

        //getVerses(request);
    }

    public boolean handleOnTouch(View v, MotionEvent event)
    {
        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d("onTouch","Action was DOWN");
                return true;

            case (MotionEvent.ACTION_MOVE) :
                Log.d("onTouch","Action was MOVE");
                return true;

            case (MotionEvent.ACTION_UP) :
                Log.d("onTouch", "Action was UP");
                int x = (int) event.getX();
                int y = (int) event.getY();
                handler_translateClick(x, y);
                return true;

            case (MotionEvent.ACTION_CANCEL) :
                Log.d("onTouch","Action was CANCEL");
                return true;

            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d("onTouch","Movement occurred outside bounds " +
                        "of current screen element");
                return true;

            default :
                Log.d("onTouch","default");
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handler_translateClick(int x, int y) {
        List<String> clickedWords = util_getWordsAtPosition(x, y);
        display_translateWords(clickedWords);
    }

    private List<String> util_getWordsAtPosition(int x, int y)
    {
        List<String> translateWords = new ArrayList<>();

        int offset = textVerses.getOffsetForPosition(x, y);
        String fullText = textVerses.getText().toString();

        Pair<Integer, Integer> clickedWordBounds = util_getWordBoundsAt(offset);
        String clickedWord = fullText.substring(
                clickedWordBounds.first, clickedWordBounds.second);

        Pair<Integer, Integer> nextWordBounds = util_getWordBoundsAt(clickedWordBounds.second + 1);
        String nextWord = fullText.substring(
                nextWordBounds.first, nextWordBounds.second);

        Pair<Integer, Integer> prevWordBounds = util_getWordBoundsAt(clickedWordBounds.first - 1);
        String prevWord = fullText.substring(
                prevWordBounds.first, prevWordBounds.second);

        translateWords.add(clickedWord);
        translateWords.add(prevWord + " " + clickedWord);
        translateWords.add(clickedWord + " " + nextWord);
        translateWords.add(prevWord + " " + clickedWord + " " + nextWord);

        return translateWords;
    }

    private Pair<Integer, Integer> util_getWordBoundsAt(int offset)
    {
        int startRange = offset;
        int endRange = offset + 1;
        String fullText = textVerses.getText().toString();

        char curChar = fullText.charAt(startRange);
        while (startRange > 0 && curChar != ' ') {
            startRange--;
            curChar = fullText.charAt(startRange);
        }

        curChar = fullText.charAt(endRange);
        while (endRange < fullText.length()-1 && curChar != ' ') {
            endRange++;
            curChar = fullText.charAt(endRange);
        }

        return new Pair<>(startRange, endRange);
    }

    private void getVerses(int bookId, int chapterNum)
    {
        DataBaseHelper myDbHelper = DataBaseHelper.getDB(this);
        List<Verse> verses = myDbHelper.getVerses(bookId, chapterNum);

        String displayText = "";

        for (Verse verse : verses)
        {
            displayText += verse.chapterNum + "." + verse.verseNum + ") " + verse.text + "\n\n";
        }
        util_setTextViewText(displayText);
    }

    private void display_translateWords(List<String> clickedWords)
    {
        for (String word : clickedWords)
        {
            request_translate(word);
        }
    }

    private void request_translate(final String word)
    {
       // Illegal character in query at index 47: http://api.mymemory.translated.net/get?q=%20?':</i>



        // Need to set up billing and pay for google api
        String urlString = "https://www.googleapis.com/language/translate/v2?source=en";//&target=en&q=Hello%20world&key=KEY";

        // need to url escape the word!
        urlString = "http://api.mymemory.translated.net/get?q=" + word + "&langpair=he%7Cen";



        AsyncHttpClient client = new AsyncHttpClient();
        Log.w("josh", "sending " + urlString);

        client.get(urlString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.w("josh", "success json object" + response);
                        displayTranslation(word, response);
                    }

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONArray response) {
                        Log.w("josh", "success got json array" + response);
                        //displayVerse(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String errStr, Throwable throwable) {
                        Log.w("josh", "fail: " + errStr + headers.toString() + throwable.toString());
                    }
                });
    }

    private void displayTranslation(String word, JSONObject jsonObject)
    {
        JSONObject responseData = jsonObject.optJSONObject("responseData");
        JSONObject matches = responseData.optJSONObject("matches");
        String translatedText = responseData.optString("translatedText");
        String match = responseData.optString("match");

        String displayText = word + " = " + translatedText;

        mTranslationList.add(displayText);
        mArrayAdapter.notifyDataSetChanged();

        //Toast.makeText(this, displayText, Toast.LENGTH_SHORT).show();
    }

    private void util_setTextViewText(final String textViewStr)
    {
        textVerses.setText(textViewStr);
    }

}
