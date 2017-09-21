package com.example.zainfo.liveness;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    final RequestController mReqController = RequestController.getInstance(this);
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_main);

        Bundle bundle=getIntent().getExtras();
//        String str=bundle.getString("showString");
//        final TextView mTextView = (TextView) findViewById(R.id.textView5);
//        mTextView.setText(str);

        String imgDir = bundle.getString("imgDir");

        Bitmap bit = BitmapFactory.decodeFile(imgDir);

//        Matrix rotateMatrix = new Matrix();
//        rotateMatrix.postRotate((float)270.0);
//        Bitmap rotaBitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth() , bit.getHeight() , rotateMatrix, false);
//        Bitmap bitmap2 = Utils.bitmapCropper(rotaBitmap);
        String cameraId = bundle.getString("cameraId");

//        final TextView mTextView = (TextView) findViewById(R.id.textView5);
//        mTextView.setText(cameraId);

        Bitmap bitmap2 = bit;

        Matrix scalematrix = new Matrix();
        final Bitmap scaleBitmap ;

        if(cameraId.equals("1")){
            float scaleWidth = (float)0.4;
            float scaleHeight = (float)0.4;
//            scalematrix.postRotate((float)270.0);
            scalematrix.postScale(scaleWidth, scaleHeight);
            scaleBitmap = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth() , bitmap2.getHeight() , scalematrix, false);
        }else{
            float scaleWidth = (float)0.4;
            float scaleHeight = (float)0.4;
//            scalematrix.postRotate((float)90.0);
            scalematrix.postScale(scaleWidth, scaleHeight);
            scaleBitmap = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth() , bitmap2.getHeight() , scalematrix, false);
        }



        final ImageView imageView = (ImageView)findViewById(R.id.cropImg);
        imageView.setImageBitmap(scaleBitmap);



        Button reTakeBtn = (Button)findViewById(R.id.button2);
        reTakeBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent in = new Intent(ResultActivity.this,MainActivity.class);
                startActivity(in);
            }
        });

        Button livernessBtn = (Button) findViewById(R.id.button1);
        livernessBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                progressDialog = ProgressDialog.show(ResultActivity.this,"Loading...","Please Wait...",true,false);
                mReqController.addToRequestQueue(getCustomReq(scaleBitmap));
            }
        });

    }

    private void showResult(String str){
        TextView textView = (TextView) findViewById(R.id.textView5);
        textView.setText(str);
    }


    public CustomRequest getCustomReq(Bitmap bitmap){

//        final Bitmap bitmap2 = Utils.bitmapCropper(bitmap);
        final Bitmap bitmap2 = bitmap;

        String strBit = bitmap2.toString();

        final String url = "http://192.168.26.127:3248/";
        final String route = "StaticLiveness";

        CustomRequest customReq = new CustomRequest(Request.Method.POST,url+route,

                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String str = new String(response.data);
                        try {
                            JSONObject res =new JSONObject(str);
                            String result = res.getString("result");
                            String detail = res.getString("detail");

                            String show = " 检测结果 :"+result+"\n 详细信息 :"+detail;

                            showResult(show);

                            progressDialog.dismiss();

                        } catch (Exception e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                    }
                },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                Log.i("Error", errorMessage);
                showResult(errorMessage);
                error.printStackTrace();
            }
        }
        ){
            @Override
            protected Map<String,String> getParams(){
                Map<String ,String > params = new HashMap<>();
                params.put("userId","driving detector");

                return params;
            }

            @Override
            protected Map<String ,DataPart> getByteData(){
                Map<String,DataPart> params = new HashMap<>();
                params.put("image", new DataPart("screenshot.jpg", Utils.getFileDataFromDrawable(getBaseContext(), bitmap2), "image/jpeg"));
                return params;
            }
        };

        return customReq;
    }
}
