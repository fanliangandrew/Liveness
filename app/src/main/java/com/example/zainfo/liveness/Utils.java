package com.example.zainfo.liveness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by zasx-fanliang on 2017/9/11.
 */

public class Utils {

    public static byte[] imageToByteArray(Image image) {
        byte[] data = null;
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        data = new byte[buffer.capacity()];
        buffer.get(data);
        return data;
    }

    public static byte[] getFileDataFromDrawable(Context context, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    public static Bitmap bitmapCropper(Bitmap bitmap){
        Bitmap bitmap0 = bitmap;

        Bitmap bitmap1 = Bitmap.createBitmap(bitmap0,480,400,2600,1650);

        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 60, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();

        final Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

        return bitmap2;
    }

}
