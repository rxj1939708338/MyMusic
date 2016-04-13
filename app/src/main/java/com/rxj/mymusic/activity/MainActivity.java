package com.rxj.mymusic.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.rxj.mymusic.MusicService;
import com.rxj.mymusic.adapter.MusicListAdapter;
import com.rxj.mymusic.db.MySqliteHelper;
import com.rxj.mymusic.entity.Entity;
import com.rxj.mymusic.entity.dbEntity;

import com.rxj.mymusic.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements ServiceConnection, Handler.Callback, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    //播放器服务
    private MusicService service;

    //控件
    private ImageButton play_btn;
    private TextView time;
    private SeekBar seek;
    private TextView title;
    private Spinner mode;
    private ListView lv;
    private Button setting;
    private Spinner song_menu;

    //包含所有歌曲id的list
    private ArrayList<Integer> list_ids;

    //设置权重之后的歌曲id的list
    private ArrayList<Integer> list_weightIds;

    //包含歌曲信息的list，是ListView的数据源
    private List<Entity> list_musicEntity=new ArrayList<>();

    //所有歌单名的list
    private List<String> list_songMenu=new ArrayList<>();

    //歌曲播放数据，需要传入service才能播放
    private ArrayList<String> list_musicDates;

    //需要入库、出库的数据
    private List<dbEntity> list_db=new ArrayList<>();

    //ListView适配器
    private MusicListAdapter musicListAdapter;

    //获取歌曲信息的cursor
    private Cursor cursor;

    //数据库表名
    public static final String TABLE_NAME = "gedan";

    //默认播放歌曲ID
    private int currentId = -1;

    //当前currentId在ids中的下标，用于实现界面流畅滑动
    private int currentId_position=-1;

    //与服务相连的handler，负责切歌和更新seekbar
    private Handler handler = new Handler(this);

    //Intent的请求码和返回码
    private static int REQUEST_CODE=0;
    private static int RESULT_CODE_WEIGHT=1;
    private static int RESULT_CODE_CHANGEGEDAN=2;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("mm:ss", Locale.CHINA);
    static {
        SDF.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getData();
        musicService();
        adapterAndListener();
    }

    //为控件设置监听和适配器
    private void adapterAndListener() {
        //播放模式的适配器与监听
        String ms[]=new String[]{"顺序播放","单曲循环","随机播放"};
        mode.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, ms));
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                service.setStateId(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                service.setStateId(0);
            }
        });

        //ListView的适配器
        musicListAdapter = new MusicListAdapter(MainActivity.this, list_musicEntity, new MusicListAdapter.onClick() {
            //设置权重的界面
            @Override
            public void onWeightClick(int position) {
                Intent intent=new Intent(MainActivity.this,WeightActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("weight",list_musicEntity.get(position).getWeight());
                intent.putExtra("name",list_musicEntity.get(position).getName());
                startActivityForResult(intent, REQUEST_CODE);
            }

            //长按显示dialog，提示加入哪个歌单
            @Override
            public void onLongClick(int position) {
                View v= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_main,null);
                ListView list_dialog = (ListView) v.findViewById(R.id.list_gedan);
                list_dialog.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, list_songMenu));
                list_dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });
                new AlertDialog.Builder(MainActivity.this).setView(v).show();
            }

            //item点击事件
            @Override
            public void onClick(int position) {
                service.playOrPause(list_ids.get(position));
            }
        });
        lv.setAdapter(musicListAdapter);

        //seekBar的变更监听事件
        seek.setOnSeekBarChangeListener(this);

        //设置界面的跳转
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SheZhiActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //选择歌单的Spinner的适配器
        song_menu.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, list_songMenu));
        //切换歌单
        song_menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //从数据库中获得相应歌单的id，从总的列表中获取其中信息
//                String ids_all = list_db.get(position).getIds();
//
//                String[] split = ids_all.split(",");
//                List<Entity> list_main=new ArrayList<Entity>();
//                List<String> list_ids=new ArrayList<String>();
//                for (int i = 0; i < split.length; i++) {
////                    list_main.add()
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //注册对象
    private void initView() {
        title = ((TextView) findViewById(R.id.title));
        lv = (ListView) findViewById(R.id.list);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_previous).setOnClickListener(this);
        play_btn = ((ImageButton) findViewById(R.id.btn_play));
        time = ((TextView) findViewById(R.id.text_time));
        seek = ((SeekBar) findViewById(R.id.seek));
        setting = ((Button) findViewById(R.id.shezhi));
        //歌单的Spinner
        song_menu = ((Spinner) findViewById(R.id.gedan));
        mode = ((Spinner) findViewById(R.id.moshi));
    }

    //开启播放音乐service
    private void musicService() {
        Intent intent = new Intent(this, MusicService.class);
        intent.putIntegerArrayListExtra("ids",list_ids);
        intent.putStringArrayListExtra("dates", list_musicDates);
        intent.putExtra("msg", new Messenger(handler));
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    //加载list数据
    private void getData() {
        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        String name=null;
        Entity entity=null;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            entity=new Entity();
            name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            entity.setName(name);
            entity.setWeight(3);
            list_musicEntity.add(entity);
        }
        list_ids=new ArrayList<>();
        list_musicDates = new ArrayList<>();
        list_weightIds=new ArrayList<>();
        int id;
        StringBuilder ids_str=new StringBuilder();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            list_ids.add(id);
            list_musicDates.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            for (int j = 0; j < list_musicEntity.get(i).getWeight(); j++) {
                list_weightIds.add(id);
            }
            ids_str.append(id).append(",");
        }

        //刷新默认歌单
        SQLiteDatabase database = new MySqliteHelper(MainActivity.this).getWritableDatabase();
        database.delete(TABLE_NAME, "name=?", new String[]{"默认歌单"});
        ContentValues values=new ContentValues();
        values.put("name","默认歌单");
        values.put("ids", ids_str.toString());
        database.insert(TABLE_NAME, null, values);
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
            String names=cursor.getString(cursor.getColumnIndex("name"));
            String ids=cursor.getString(cursor.getColumnIndex("ids"));
            dbEntity entitys=new dbEntity();
            entitys.setIds(ids);
            entitys.setName(names);
//            list_db.add(entitys);
            list_songMenu.add(names);
        }
    }

    //更改播放器的名字
    public void rename(View view){
        final EditText et=new EditText(MainActivity.this);
        et.setText(title.getText().toString());
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("新名字")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name=et.getText().toString();
                        title.setText(name);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //回调改变自定义随机方式
        if(requestCode==REQUEST_CODE){
            if(resultCode==RESULT_CODE_WEIGHT){
                //list的item权重设置
                int weight = data.getIntExtra("weight", 3);
                int position=data.getIntExtra("position", 0);
                Entity entity=list_musicEntity.get(position);
                entity.setWeight(weight);
                list_musicEntity.set(position,entity);
                for (int i = 0; i < 10; i++) {
                    Log.d("=====","list值"+list_musicEntity.get(i).getWeight()+"");
                }
                list_weightIds=new ArrayList<>();
                for (int i = 0; i < list_ids.size(); i++) {
                    for (int j = 0; j < list_musicEntity.get(i).getWeight(); j++) {
                        list_weightIds.add(list_ids.get(i));
                    }
                }
                //将变更后的ids传递过去
                service.setList_weightIds(list_weightIds);
                musicListAdapter.notifyDataSetChanged();
            }else if(resultCode==RESULT_CODE_CHANGEGEDAN){
//                //歌单变更,重新从数据库中获得新的歌单
                SQLiteDatabase database = new MySqliteHelper(MainActivity.this).getWritableDatabase();
                Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
                list_songMenu=new ArrayList<>();
                for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
                    String names=cursor.getString(cursor.getColumnIndex("name"));
                    String ids=cursor.getString(cursor.getColumnIndex("ids"));
                    dbEntity entitys=new dbEntity();
                    entitys.setIds(ids);
                    entitys.setName(names);
//                list_db.add(entitys);
                    list_songMenu.add(names);
                }
                //刷新适配器
                song_menu.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, list_songMenu));
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = ((MusicService.MusicBinder) service).getService();
        this.service.setList_weightIds(list_weightIds);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case 0:
                currentId = msg.arg1;
                for (int i = 0; i < list_ids.size(); i++) {
                    if(list_ids.get(i)==currentId){
                        currentId_position=i;
                    }
                }
                musicListAdapter.setCurrentId_position(currentId_position);
                musicListAdapter.notifyDataSetChanged();
                //设置切歌时，画面会跟随移动
                int i1 = lv.getFirstVisiblePosition();
                int i2 = lv.getLastVisiblePosition();
                if(currentId_position<i1+1||currentId_position>i2-1){
                    if(currentId_position==i1){
                        lv.setSelection(currentId_position-1);
                    }else if(currentId_position==i2){
                        lv.setSelection(currentId_position-7);
                    }else if(currentId_position>=4){
                        lv.setSelection(currentId_position-4);
                    }else {
                        lv.setSelection(currentId_position);
                    }
                }

                play_btn.setImageResource(service.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                if (service.isPlaying()){
                    handler.sendEmptyMessage(1);
                }
                break;
            case 1:
                //格式化 当前时间/总时长
                time.setText(String.format("%s/%s",
                        SDF.format(new Date(service.getCurrentPosition())),
                        SDF.format(new Date(service.getDuration()))));
                //让seekBar跟随音频移动
                seek.setProgress(service.getCurrentPosition() * seek.getMax() / service.getDuration());

                if (service.isPlaying()){
                    handler.sendEmptyMessageDelayed(1, 500);
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous:
                service.playPrevious();
                break;
            case R.id.btn_play:
                service.playOrPause(currentId);
                break;
            case R.id.btn_next:
                service.playNext();
                break;
        }
    }

    /**
     * 进度改变
     * @param seekBar
     * @param progress 改变后的进度
     * @param fromUser 是否来自于用户
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // 进度/最大值 = 当前时间/总时长
        if (fromUser) {
            service.seekTo(progress * service.getDuration() / seekBar.getMax());
        }
    }

    /**
     * 开始触摸
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 停止触摸
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
