package com.donkeyboatworks.teachmetorah;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.donkeyboatworks.teachmetorah/databases/";

    private static String DB_NAME = "sefaria_db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database: " + e.getMessage());

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    public void deleteDataBase(){
        myContext.deleteDatabase(DB_NAME);
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    public static DataBaseHelper getDB(Context context)
    {
        DataBaseHelper dbHelper = new DataBaseHelper(context);

        // Uncomment deleteDataBase when adding a new db file, but only for one run so the file gets replaced
        //myDbHelper.deleteDataBase();

        try {

            dbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            dbHelper.openDataBase();

        }catch(SQLException sqle){

            // throw sqle;
            throw new Error("Unable to open database");
        }

        return dbHelper;
    }

    public List<Book> getBooks(Integer categoryId)
    {

        String query = "SELECT _id, categoryId, name FROM book WHERE categoryId = " + categoryId.toString();
        Log.d("Query: ", query);
        Cursor cursor = myDataBase.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            Log.d("Query: ", "No results!");
            cursor.close();
            return null;
        }

        cursor.moveToFirst();

        try {
            int idColIdx = cursor.getColumnIndex("_id");
            int categoryIdColIdx = cursor.getColumnIndex("categoryId");
            int nameColIdx = cursor.getColumnIndex("name");
            Log.d("Query: ", idColIdx + " "+  cursor.getInt(categoryIdColIdx) +" "+nameColIdx);

            List<Book> books = new ArrayList<Book>();

            do {
                books.add(new Book(
                        cursor.getInt(idColIdx),
                        cursor.getInt(categoryIdColIdx),
                        cursor.getString(nameColIdx)
                ));
            } while (cursor.moveToNext());

            if (!cursor.isClosed()) {
                cursor.close();
            }

            return books;
        }
        catch (CursorIndexOutOfBoundsException exception) {
            Log.w("Db error", exception.getMessage());
        }
        return null;
    }

    public List<Category> getCategories(Integer parentCategoryId)
    {

        //Cursor cursor = myDataBase.rawQuery("SELECT locale FROM android_metadata", null);
        //Cursor cursor = myDataBase.rawQuery( " SELECT * FROM sqlite_master", null); //name FROM sqlite_master WHERE type='table' AND name LIKE 'PR_%' ", null);
        String query = "SELECT _id, parentCategoryId, name FROM category ";
        if (parentCategoryId != null) {
            query += " WHERE parentCategoryId = " + parentCategoryId.toString();
        }
        else {
            // I didn't insert the data correctly and now only = '' works. Probably converted that to an int or something
            query += " WHERE parentCategoryId = '' ";
            //query += " WHERE parentCategoryId = '0'";
        }
        Log.d("Query: ", query);
        Cursor cursor = myDataBase.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            Log.d("Query: ", "No results!");
            cursor.close();
            return null;
        }

        cursor.moveToFirst();

        try {
            int idColIdx = cursor.getColumnIndex("_id");
            int parentCategoryIdColIdx = cursor.getColumnIndex("parentCategoryId");
            int nameColIdx = cursor.getColumnIndex("name");
            Log.d("Query: ", idColIdx + " "+  cursor.getInt(parentCategoryIdColIdx) +" "+nameColIdx);

            List<Category> categories = new ArrayList<Category>();

            do {
                categories.add(new Category(
                        cursor.getInt(idColIdx),
                        cursor.getInt(parentCategoryIdColIdx),
                        cursor.getString(nameColIdx)
                ));
            } while (cursor.moveToNext());

            if (!cursor.isClosed()) {
                cursor.close();
            }

            return categories;
        }
        catch (CursorIndexOutOfBoundsException exception) {
            Log.w("Db error", exception.getMessage());
        }
        return null;
    }

    List<Verse> getVerses(Integer bookId, Integer chapterNum)
    {
        String query = "SELECT _id, bookId, chapterNum, verseNum, text FROM verse " +
                "WHERE bookId = " + bookId.toString() + " AND chapterNum = " + chapterNum.toString();
        Log.d("Query: ", query);
        Cursor cursor = myDataBase.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            Log.d("Query: ", "No results!");
            cursor.close();
            return null;
        }

        cursor.moveToFirst();

        try {
            int idColIdx = cursor.getColumnIndex("_id");
            int bookIdColIdx = cursor.getColumnIndex("bookId");
            int chapterNumColIdx = cursor.getColumnIndex("chapterNum");
            int verseNumColIdx = cursor.getColumnIndex("verseNum");
            int textColIdx = cursor.getColumnIndex("text");

            List<Verse> verses = new ArrayList<Verse>();

            do {
                verses.add(new Verse(
                        cursor.getInt(idColIdx),
                        cursor.getInt(bookIdColIdx),
                        cursor.getInt(chapterNumColIdx),
                        cursor.getInt(verseNumColIdx),
                        cursor.getString(textColIdx)
                ));
            } while (cursor.moveToNext());

            if (!cursor.isClosed()) {
                cursor.close();
            }

            return verses;
        }
        catch (CursorIndexOutOfBoundsException exception) {
            Log.w("Db error", exception.getMessage());
        }
        return null;
    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}