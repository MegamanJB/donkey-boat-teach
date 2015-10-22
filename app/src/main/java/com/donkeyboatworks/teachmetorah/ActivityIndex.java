package com.donkeyboatworks.teachmetorah;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joshua on 10/4/2015.
 */
public class ActivityIndex extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    TextView names;
    Button mainButton;
    ListView mainListView;
    ArrayAdapter mArrayAdapter;
    ArrayList mNameList = new ArrayList();
    Map<String, Category> nameToCategory;
    Map<String, Book> nameToBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        mainListView = (ListView) findViewById(R.id.main_listview);
        mArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mNameList);
        mainListView.setAdapter(mArrayAdapter);
        mainListView.setOnItemClickListener(this);

        Bundle extras = this.getIntent().getExtras();
        Integer parentCategoryId = null;
        Integer bookId = null;
        if (extras != null) {
            parentCategoryId = (Integer) extras.get("parentCategoryId");
            if (parentCategoryId != null) {
                display_books(parentCategoryId);
                return;
            }

            bookId = (Integer) extras.get("bookId");
            Log.w("bookId", bookId.toString());
            if (bookId != null) {
                display_chapters(bookId);
                return;
            }
        }

        Log.w("init", "display cats");
        display_categories(parentCategoryId);
    }

    @Override
    public void onClick(View v) {
        Intent viewTextIntent = new Intent(this, ActivityViewText.class);
        startActivity(viewTextIntent);
    }

    private void display_categories(Integer parentCategoryId)
    {
        DataBaseHelper myDbHelper = DataBaseHelper.getDB(this);
        List<Category> categories = myDbHelper.getCategories(parentCategoryId);

        if (categories == null) {
            display_books(parentCategoryId);
            return;
        }
        nameToCategory = new HashMap<>();

        for (Category category : categories) {
            Log.w("index", "adding " + category.name);

            mNameList.add(category.name);
            nameToCategory.put(category.name, category);

            mArrayAdapter.notifyDataSetChanged();
        }
    }

    private void display_books(Integer parentCategoryId)
    {
        DataBaseHelper myDbHelper = DataBaseHelper.getDB(this);
        List<Book> books = myDbHelper.getBooks(parentCategoryId);

        if (books == null) {
            return;
        }
        nameToBook = new HashMap<>();

        for (Book book : books) {
            Log.w("index", "adding " + book.name);

            mNameList.add(book.name);
            nameToBook.put(book.name, book);

            mArrayAdapter.notifyDataSetChanged();
        }
    }
    
    private void display_chapters(Integer bookId)
    {
        DataBaseHelper myDbHelper = DataBaseHelper.getDB(this);
        List<Chapter> chapters = myDbHelper.getChapters(bookId);

        if (chapters == null) {
            return;
        }

        for (Chapter chapter: chapters) {
            Log.w("index", "adding " + chapter.chapterNum);

            mNameList.add("Chapter " + chapter.chapterNum);
            mArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String name = mNameList.get(position).toString();
        Log.d("Index Selection", position + ": " +name);

        if (nameToCategory != null && nameToCategory.containsKey(name))
        {
            Category category = nameToCategory.get(name);

            Intent indexIntent = new Intent(this, ActivityIndex.class);
            indexIntent.putExtra("parentCategoryId", category.id);
            startActivity(indexIntent);
        }
        else if (nameToBook.containsKey(name))
        {
            Book book = nameToBook.get(name);

            Intent indexIntent = new Intent(this, ActivityIndex.class);
            indexIntent.putExtra("bookId", book.id);
            startActivity(indexIntent);

            /* Intent viewTextIntent = new Intent(this, ActivityViewText.class);
            viewTextIntent.putExtra("bookId", book.id);
            startActivity(viewTextIntent);*/
        }

    }
}