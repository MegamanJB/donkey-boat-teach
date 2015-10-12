package com.donkeyboatworks.teachmetorah;

/**
 * Created by Joshua on 10/7/2015.
 */
public class Category {

    public int id;
    public Integer parentCategoryId = null;
    public String name;

    public Category(int id, Integer parentCategoryId, String name)
    {
        this.id = id;
        this.parentCategoryId = parentCategoryId;
        this.name = name;
    }

}
