package com.rxj.mymusic.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;


import com.rxj.mymusic.R;
import com.rxj.mymusic.adapter.ShanChuAdapter;
import com.rxj.mymusic.db.MySqliteHelper;
import com.rxj.mymusic.entity.dbEntity;

import java.util.ArrayList;
import java.util.List;

public class DeleteGeDanActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView sc_list;
    public static List<dbEntity> delete_list=new ArrayList<>();
    private List<dbEntity> list=new ArrayList<>();
    private ShanChuAdapter adapter;
    public static final String TABLE_NAME = "gedan";
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_ge_dan);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        sc_list = ((ListView) findViewById(R.id.shanchu_list));
        database = new MySqliteHelper(DeleteGeDanActivity.this).getWritableDatabase();
        Cursor cursor=database.query(TABLE_NAME, null, null, null, null, null, null);
//        Cursor cursor=new MyContentProvider(DeleteGeDanActivity.this).query(null,null,null,null,null);
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
            dbEntity entity=new dbEntity();
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String ids = cursor.getString(cursor.getColumnIndex("ids"));
            entity.setName(name);
            entity.setIds(ids);
            list.add(entity);
        }
        adapter = new ShanChuAdapter(DeleteGeDanActivity.this,list);
        sc_list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.delete:
                //存入数据库
                for (dbEntity entity :delete_list) {
                    list.remove(entity);
                    //从数据库中移除信息
//                    provider.delete(null, "name=?", new String[]{entity.getName()});
                    database.delete(TABLE_NAME,"name=?",new String[]{entity.getName()});
                }
                adapter.notifyDataSetChanged();
                break;
        }
    }
}
