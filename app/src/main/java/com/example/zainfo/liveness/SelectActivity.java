package com.example.zainfo.liveness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SelectActivity extends AppCompatActivity {
    private Spinner mSpinner;
    private EditText mTimeMark;
    private Button mStartButton;
    private int actionIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Utils.setActionIndex(0);
        Utils.setRecordTime(3000);
        actionIndex=0;
        mSpinner=(Spinner)findViewById(R.id.actions);
        mTimeMark=(EditText)findViewById(R.id.videoTime);
        mStartButton=(Button)findViewById(R.id.startButton);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actionIndex=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=mTimeMark.getText().toString();
                if(!s.isEmpty()){
                    int rTime;
                    rTime=Integer.parseInt(s);
                    Utils.setRecordTime(rTime);
                }else{
                    Utils.setRecordTime(3000);
                }
                Utils.setActionIndex(mSpinner.getSelectedItemPosition());
                Intent intent=new Intent(SelectActivity.this,VideoActivity.class);
                startActivity(intent);
            }
        });
    }
}
