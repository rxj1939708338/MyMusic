package com.rxj.mymusic.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rxj.mymusic.R;
import com.rxj.mymusic.db.MySqliteHelper;

public class AddGeDanActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et;
    private Button sure;
    public static boolean ADD_TAG=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ge_dan);
        ADD_TAG=false;
        initView();
    }

    private void initView() {
        findViewById(R.id.back).setOnClickListener(this);
        sure = ((Button) findViewById(R.id.sure));
        sure.setOnClickListener(this);
        et = ((EditText) findViewById(R.id.et));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.sure:
                String s = et.getText().toString();
                if (s == null||s.trim().equals("")) {
                    Toast.makeText(AddGeDanActivity.this, "歌单名为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                //存入数据库
                SQLiteDatabase database = new MySqliteHelper(AddGeDanActivity.this).getWritableDatabase();
                ContentValues values=new ContentValues();
                values.put("name",s.trim());
                database.insert(MySqliteHelper.TABLE_NAME,null,values);
                //添加歌单成功
                ADD_TAG=true;
                //设置点击一次后不能点击
                sure.setEnabled(false);

                Cursor cursor=database.query(MySqliteHelper.TABLE_NAME, null, null, null, null, null, null);
                Toast.makeText(AddGeDanActivity.this, ""+cursor.getCount(), Toast.LENGTH_SHORT).show();

                //跳回原来界面
                Intent intent=new Intent();
                intent.putExtra("gedanName", s.trim());
                setResult(2, intent);

                finish();
                break;
            default:
                break;
        }
    }
}
