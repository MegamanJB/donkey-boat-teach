package com.donkeyboatworks.teachmetorah;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.BreakIterator;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    TextView textVerses;
    Button buttonForward;
    Button buttonBack;

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
        //setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_index);

        DataBaseHelper myDbHelper = DataBaseHelper.getDB(this);
        //String results = myDbHelper.query("select bookId,chapterNum,count(*) from verse group by bookId,chapterNum");
        //Log.w("TEST", results);

        Intent indexIntent = new Intent(this, ActivityIndex.class);
        startActivity(indexIntent);

    /*    versesDB = new VersesDB(this);

        textVerses = (TextView)findViewById(R.id.textVerses);
        textVerses.setMovementMethod(new ScrollingMovementMethod());

        buttonForward = (Button) findViewById(R.id.buttonForward);
        buttonForward.setOnClickListener(this);

        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        //makeRequest();

        String request = SEFARIA_URL + TOC_URI;
        //getTOC(request);

//        String request = SEFARIA_URL + TEXTS_URI + SHULCHAN_ARUCH + "." + currentPage;
        getVerses(request);*/
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

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private void makeRequest()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("key", "value");
        params.put("more", "data");
        client.get("http://www.google.com", params, new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        // called when response HTTP status is "200 OK"
                        Log.w("josh", "google success "+ res);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.w("josh", "fail");
                    }
                }
        );
    }

    private void getTOC(final String searchString) {

        Log.w("josh", searchString);

        String response = versesDB.getVerseByUrl(searchString);
        if (response != null) {
            Log.w("josh", "using saved response");
            textVerses.setText(response.substring(0, 200));
            JSONArray responseArray = null;
            try {
                responseArray = new JSONArray(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String toc = getTOCText(responseArray, 0);
            util_setTextViewText(toc);
            return;
        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("key", "value");
        params.put("more", "data");
        client.get(searchString, params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                        Log.w("josh", "success jsonObject");
                       // displayTOC(jsonObject);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.w("josh", "success JSONArray");
                        displayTOC(response, searchString);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.w("josh", "fail jsonObject" + throwable.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        Log.w("josh", "fail JSONArray");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.w("josh", "fail String");
                    }
                });
    }

    private String getFromNewDB()
    {
        String verse = "";

        DataBaseHelper myDbHelper = new DataBaseHelper(this);

        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            myDbHelper.openDataBase();

        }catch(SQLException sqle){

            // throw sqle;
            throw new Error("Unable to open database");
        }

        verse = "";// myDbHelper.getBook();
        return verse;
    }

    private void getVerses(String searchString) {

        Toast.makeText(this, "setting query " + searchString, Toast.LENGTH_LONG).show();
        Log.w("josh", searchString);


        String dbText = getFromNewDB();
        util_setTextViewText(dbText);
        return;
        /*

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";
//        try {
//            urlString = URLEncoder.encode(searchString, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//
//            // if this fails for some reason, let the user know why
//            e.printStackTrace();
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        // Have the client get a JSONArray of data
        // and define how to respond
        Log.w("josh", "sending " + searchString);

//        String query = QUERY_URL + urlString;
//        query = "http://openlibrary.org/search.json?q=test";
//        query = "http://www.sefaria.org/api/texts/Kohelet.5";

        client.get(searchString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.w("josh", "success json object");
                        displayVerse(response);
                    }

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONArray response) {
                        Log.w("josh", "success got json array");
                        //displayVerse(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String errStr, Throwable throwable) {
                        Log.w("josh", "fail");
                    }
                });

                */
    }

    private void translate(String word)
    {
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
                        displayTranslation(response);
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

    private void displayVerse(JSONObject jsonObject)
    {
        String title = jsonObject.optString("heRef");
        String english = jsonObject.optString("text");
        String hebrew = jsonObject.optString("he");
        nextPage = jsonObject.optString("next");
        prevPage = jsonObject.optString("prev");

        util_setTextViewText(title + "\n\n\n" + hebrew);
//        textVerses.setText(title + "\n\n\n" + hebrew);
    }

    private void displayTOC(JSONArray response, String url)
    {
        String responseStr = response.toString();
        versesDB.insertVerseByUrl(url, responseStr);

        // Log.w("josh",response.toString());
        String toc = getTOCText(response, 0);
        textVerses.setText(toc);
    }

    private void displayTranslation(JSONObject jsonObject)
    {
        JSONObject responseData = jsonObject.optJSONObject("responseData");
        JSONObject matches = responseData.optJSONObject("matches");
        String translatedText = responseData.optString("translatedText");
        String match = responseData.optString("match");
        Toast.makeText(this, "translatedText " + translatedText, Toast.LENGTH_SHORT).show();
    }

    private String getTOCText(JSONArray catContents, int level)
    {
        String toc = "";
        JSONObject category = null;
        try {
            int TEST_MAX = 2;

            for (int i = 0; i < catContents.length() && i < TEST_MAX; i++) {
                for (int j = 0; j < level; j++) {
                    toc += "--";
                }

                JSONObject content = (JSONObject) catContents.get(i);
                String catName = content.optString("category");
                if (catName != "null" && catName != null && catName.trim() != "") {
                    String heCatName = content.optString(("heCategory"));
                    JSONArray contents = content.optJSONArray("contents");

                    toc += (i+1) + ") " + catName + "\n";
                    if (contents != null && contents.length() != 0) {
                        toc += getTOCText(contents, level + 1);
                    }
                }
                else {
                    String title = content.optString("title");
                    if (title != "null") {
                        toc += (i+1) + ". " + title;
                    }
                }

                toc += "\n";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return toc;
    }

    private void util_setTextViewText(final String textViewStr)
    {
        //textVerses.setText(textViewStr);

        //String definition = "Clickable words in text view ".trim();
        //TextView definitionView = (TextView) findViewById(R.id.text);

        textVerses.setMovementMethod(LinkMovementMethod.getInstance());
        textVerses.setText(textViewStr, TextView.BufferType.SPANNABLE);
        Spannable spans = (Spannable) textVerses.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(textViewStr);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = textViewStr.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                ClickableSpan clickSpan = ClickableSpan_translate(possibleWord);
                spans.setSpan(clickSpan, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private ClickableSpan ClickableSpan_translate(final String word) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            public void onClick(View widget) {
                Log.d("tapped on:", mWord);
                Toast.makeText(widget.getContext(), mWord, Toast.LENGTH_SHORT).show();
                translate(mWord);
                //String request = "https://www.googleapis.com/language/translate/v2?source=en&target=de&q=Hello%20world&key=AIzaSyC13zwX4iI3LpAdVvmnjLQo4IqdMaI7gd4";
                //String request = SEFARIA_URL + TEXTS_URI + mWord + "." + currentPage;
                //getVerses(request);
            }//keytool -exportcert -alias androiddebugkey -keystore path-to-debug-or-production-keystore -list -v

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }
        };
    }

    private ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            public void onClick(View widget) {
                Log.d("tapped on:", mWord);
                Toast.makeText(widget.getContext(), mWord, Toast.LENGTH_SHORT).show();
                String request = SEFARIA_URL + TEXTS_URI + mWord + "." + currentPage;
                getVerses(request);
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }
        };
    }

    public void loadNextPage()
    {
        if (nextPage != "null") {
            String request = SEFARIA_URL + TEXTS_URI + nextPage;
            getVerses(request);
        }
    }

    public void loadPrevPage()
    {
        if (prevPage != "null") {
            String request = SEFARIA_URL + TEXTS_URI + prevPage;
            getVerses(request);
        }
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.buttonBack:
                loadPrevPage();
                break;

            case R.id.buttonForward:
                loadNextPage();
                break;
        }
    }
}
