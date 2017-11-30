package com.example.zainfo.liveness;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;



import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by zasx-fanliang on 2017/9/11.
 */

public class Utils {
    private static int recordTime=3000;
    private static int actionIndex=0;
    public static int  getRecordTime(){
        return recordTime;
    }
    public static void setRecordTime(int time){
        recordTime=time;
    }
    public static int getActionIndex(){
        return actionIndex;
    }
    public static void setActionIndex(int index){
        actionIndex=index;
    }
    public static byte[] imageToByteArray(Image image) {
        byte[] data = null;
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        data = new byte[buffer.capacity()];
        buffer.get(data);
        return data;
    }

    public static byte[] bitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] b = baos.toByteArray();
        return b;
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
    public static byte[] filetoByteArray(File file){
        FileInputStream fis=null;

        byte[] bytes=new byte[(int)file.length()];
        try {
            fis=new FileInputStream(file);
            int offset = 0;
            int numRead = 0;
            while (offset <bytes.length
                    && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return bytes;
        }
    }

    public static byte[] read(File file) {
        FileInputStream is = null;
        // 获取文件大小
        long length = file.length();
        // 创建一个数据来保存文件数据
        byte[] fileData = new byte[(int)length];

        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int bytesRead=0;
        // 读取数据到byte数组中
        while(bytesRead != fileData.length) {
            try {
                bytesRead += is.read(fileData, bytesRead, fileData.length - bytesRead);
                if(is != null)
                    is.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return fileData;
    }
    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


}
