package com.hfjs.amapcardemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.hfjs.amapcardemo.GlideApp;
import com.hfjs.amapcardemo.R;

public class ImageUtil {

    private RequestBuilder<Bitmap> getBaseGlide(Context context){
        return GlideApp.with(context).asBitmap().error(R.mipmap.end).centerCrop().transition(BitmapTransitionOptions.withCrossFade()).load("");
    }
    public static void getBitmap(Context context, String url, ImageView imageView){
        GlideApp.with(context).asBitmap().load(url).centerCrop().into(imageView);
    }

}
