package com.donkeyboatworks.teachmetorah;

/**
 * Created by Joshua on 10/7/2015.
 */
public class Book {

    public int id;
    public Integer categoryId = null;
    public String name;

    public Book(int id, Integer categoryId, String name)
    {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
    }

}
