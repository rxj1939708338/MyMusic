package com.rxj.mymusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rxj.mymusic.R;


public class WeightActivity extends AppCompatActivity implements View.OnClickListener {

    //权重
    private int weight;
    private int position;
    private String name;
    private TextView music_name;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private RadioButton rb4;
    private RadioButton rb5;
    private Button sure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        Intent intent=getIntent();
        weight = intent.getIntExtra("weight", 3);
        position = intent.getIntExtra("position", 0);
        name = intent.getStringExtra("name");
        initView();
        switch (weight){
            case 1:
                rb1.setChecked(true);
                break;
            case 2:
                rb2.setChecked(true);
                break;
            case 3:
                rb3.setChecked(true);
                break;
            case 4:
                rb4.setChecked(true);
                break;
            case 5:
                rb5.setChecked(true);
                break;
        }
    }

    private void initView() {
        music_name = ((TextView) findViewById(R.id.name));
        rb1 = ((RadioButton) findViewById(R.id.rb1));
        rb2 = ((RadioButton) findViewById(R.id.rb2));
        rb3 = ((RadioButton) findViewById(R.id.rb3));
        rb4 = ((RadioButton) findViewById(R.id.rb4));
        rb5 = ((RadioButton) findViewById(R.id.rb5));
        sure = ((Button) findViewById(R.id.sure));
        music_name.setText(name);
        sure.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        intent.putExtra("position",position);
        if(rb1.isChecked()){
            weight=1;
        }else if(rb2.isChecked()){
            weight=2;
        }else if(rb3.isChecked()){
            weight=3;
        }else if(rb4.isChecked()){
            weight=4;
        }else if(rb5.isChecked()){
            weight=5;
        }
        intent.putExtra("weight",weight);
        setResult(1,intent);
        this.finish();
    }
}
