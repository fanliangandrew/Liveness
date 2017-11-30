package com.example.zainfo.liveness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private String userId;
    private EditText videoTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);
        videoTime=(EditText)findViewById(R.id.videoTime);
        sp=getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        setUserID();

        Button CameraButton=(Button)findViewById(R.id.cameraButton);
        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId=sp.getString("userId","");
                if(userId==""){
                    startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                }else {
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putBoolean("signup",false);
                    editor.commit();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        Button RecordButton=(Button)findViewById(R.id.record);
        RecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                userId=sp.getString("userId","");
//                if(userId=="") {
//                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
//                }else{
//                    SharedPreferences.Editor editor=sp.edit();
//                    editor.putBoolean("signup",false);
//                    editor.commit();
//                String s=videoTime.getText().toString();
                Intent intent=new Intent(HomeActivity.this,SelectActivity.class);
//                intent.putExtra("Time",s);
                startActivity(intent);
//                }
            }
        });
        Button SignoutButton=(Button)findViewById(R.id.signOut);
        SignoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("userId","");
                editor.commit();
                new AlertDialog.Builder(HomeActivity.this).setTitle("提示：").setMessage("登出成功！").setNeutralButton("确认",null).show();
                setUserID();
            }
        });
        Button testButton=(Button)findViewById(R.id.button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("userId","0");
                editor.putBoolean("isTesting",true);
                editor.commit();
                startActivity(new Intent(HomeActivity.this,MainActivity.class));
            }
        });
    }
    private void setUserID(){
        userId=sp.getString("userId","");
        if(userId!=""){
            ((TextView)findViewById(R.id.userid)).setText(userId);
        }else{
            ((TextView)findViewById(R.id.userid)).setText("未登录");
        }
        return;
    }
}
