package com.rxj.mymusic.entity;

import com.rxj.mymusic.onClicks;

/**
 * Created by Android on 2016/1/20.
 */
public class dbEntity {
    private int id;
    private String name;
    private String ids;
    private onClicks clicks;

    public onClicks getClicks() {
        return clicks;
    }

    public void setClicks(onClicks clicks) {
        this.clicks = clicks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }
}
