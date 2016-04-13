package com.rxj.mymusic.entity;

import android.graphics.Bitmap;

import com.rxj.mymusic.onClicks;


/**
 * Created by Android on 2016/1/20.
 */
public class SheZhiEntity{
    private String name;
    private Bitmap pic;
    private String Img_url;
    private onClicks clicks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getPic() {
        return pic;
    }

    public void setPic(Bitmap pic) {
        this.pic = pic;
    }

    public String getImg_url() {
        return Img_url;
    }

    public void setImg_url(String img_url) {
        Img_url = img_url;
    }

    public onClicks getClicks() {
        return clicks;
    }

    public void setClicks(onClicks clicks) {
        this.clicks = clicks;
    }
}
