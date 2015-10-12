package com.donkeyboatworks.teachmetorah;

/**
 * Created by Joshua on 10/7/2015.
 */
public class Verse {

    public int id;
    public int bookId;
    public int chapterNum;
    public int verseNum;
    public String text;

    public Verse(int id, int bookId, int chapterNum, int verseNum, String text)
    {
        this.id = id;
        this.bookId = bookId;
        this.chapterNum = chapterNum;
        this.verseNum = verseNum;
        this.text = text;
    }

}
