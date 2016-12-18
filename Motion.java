package com.fengqi.motions;

import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by fengqi on 15-06-17.
 */
public class Motion extends Object implements Serializable{
    private String mDescription;
    private String mUrl;

    public Motion(String url, String desc) {
        mUrl = url;
        mDescription = desc;
    }

    public String getDescription() {
        return mDescription;
    }
    public Drawable getDrawable() {
        GifDrawable gif = null;
        try {
            gif = new GifDrawable(new File(mUrl));
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return gif;
        //return Drawable.createFromPath(mUrl);
    }

    public static Motion from(String url) {
        return new Motion(url, url);
    }
}
