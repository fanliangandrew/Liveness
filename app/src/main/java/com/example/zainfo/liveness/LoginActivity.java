package com.example.zainfo.liveness;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private Button login,signup;
    private SharedPreferences sp;
    private String uid;
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("注册/登录");
        setContentView(R.layout.login_main);
        mQueue = Volley.newRequestQueue(LoginActivity.this);
        username=(EditText)findViewById(R.id.username);
        login=(Button)findViewById(R.id.loginButton);
        signup=(Button)findViewById(R.id.signupButton);
        sp=getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid=sp.getString("userID","");
        if(uid!=""){
            LoginActivity.this.finish();
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.this.createLoginRequest();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.this.createSignupRequest();
            }
        });
    }

    protected void createLoginRequest(){
        uid=username.getText().toString();
        StringRequest sr=new StringRequest(Request.Method.POST, "http://192.168.26.125:3245/login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jo=new JSONObject(response);
                    String res=jo.getString("result");
                    if(res.equals("success")){
                        new AlertDialog.Builder(LoginActivity.this).setTitle("提示：").setMessage("登录成功！").setNeutralButton("确定",null).show();
                        SharedPreferences.Editor editor=sp.edit();
                        editor.putString("userId", uid);
                        editor.putBoolean("signup",false);
                        editor.commit();
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    }else if(res.equals("fail")){
                        new AlertDialog.Builder(LoginActivity.this).setTitle("错误！").setMessage("用户名不匹配").setNeutralButton("确定",null).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LOGIN ERROR",error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params=new HashMap<>();
                params.put("user",uid);
                return params;
            }
        };
        mQueue.add(sr);
        return;
    }
    protected void createSignupRequest(){
        uid=username.getText().toString();
        StringRequest sr=new StringRequest(Request.Method.POST, "http://192.168.26.125:3245/signup", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jo=new JSONObject(response);
                    String res=jo.getString("result");
                    if(res.equals("success")){
                        new AlertDialog.Builder(LoginActivity.this).setTitle("提示：").setMessage("注册成功！").setNeutralButton("确定",null).show();
                        SharedPreferences.Editor editor=sp.edit();
                        editor.putString("userId",uid);
                        editor.putBoolean("signup",true);
                        editor.commit();

                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    }else if(res.equals("fail")){
                        new AlertDialog.Builder(LoginActivity.this).setTitle("错误").setMessage("注册失败！").setNeutralButton("确定",null).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LOGIN ERROR",error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("user",uid);
                return params;
            }
        };
        mQueue.add(sr);
    }
}
