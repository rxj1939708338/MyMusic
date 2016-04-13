package com.rxj.mymusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;

import com.rxj.mymusic.R;
import com.rxj.mymusic.adapter.SheZhiAdapter;
import com.rxj.mymusic.entity.SheZhiEntity;
import com.rxj.mymusic.onClicks;

import java.util.ArrayList;
import java.util.List;

public class SheZhiActivity extends AppCompatActivity implements View.OnClickListener {

    private GridView gd;

    //
    private List<SheZhiEntity> list_shezhiEntity =new ArrayList<>();
    private SheZhiAdapter adapter;
    private static int RESULT_CODE_CHANGEGEDAN=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_she_zhi);
        initView();
        getData();
        setAdapter();
    }

    private void setAdapter() {
        adapter = new SheZhiAdapter(SheZhiActivity.this, list_shezhiEntity);
        gd.setAdapter(adapter);
    }

    private void getData() {
        //为GridView添加子组件
        SheZhiEntity entity=new SheZhiEntity();
        entity.setName("添加歌单");
        entity.setClicks(new onClicks() {
            @Override
            public void onClickListener() {
                //进入添加歌单界面
                Intent i = new Intent(SheZhiActivity.this, AddGeDanActivity.class);
                startActivity(i);
            }

            @Override
            public void onLongClickListener() {

            }
        });
        list_shezhiEntity.add(entity);
        //删除歌单
        entity=new SheZhiEntity();
        entity.setName("删除歌单");
        entity.setClicks(new onClicks() {
            @Override
            public void onClickListener() {
                //进入删除歌单界面
                Intent i=new Intent(SheZhiActivity.this,DeleteGeDanActivity.class);
                startActivity(i);
            }

            @Override
            public void onLongClickListener() {

            }
        });
        list_shezhiEntity.add(entity);
    }

    private void initView() {
        findViewById(R.id.back).setOnClickListener(this);
        gd = ((GridView) findViewById(R.id.gd));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                setResult(RESULT_CODE_CHANGEGEDAN);
                finish();
                break;
            default:
                break;
        }
    }
}
