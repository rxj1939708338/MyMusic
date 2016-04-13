package com.rxj.mymusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.rxj.mymusic.activity.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer player;

    //存放数据映射的Map
    private Map<Integer, String> map;

    //包含所有歌曲id的list
    private ArrayList<Integer> list_ids;

    //设置权重之后的歌曲id的list
    private ArrayList<Integer> list_weightIds;

    //歌曲播放数据，需要播放的数据
    private ArrayList<String> list_dates;

    private Messenger msg;
    private Notification build;
    private NotificationManager manager;
    private MusicReceiver musicReceiver;
    private int stateId=1;
    //监听广播状态的tag
    private boolean tag;


    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        //一个音频文件播放结束时
        player.setOnCompletionListener(this);
        musicReceiver = new MusicReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        list_ids = intent.getIntegerArrayListExtra("ids");
        list_dates = intent.getStringArrayListExtra("dates");
        map = new HashMap<>();
        for (int i = 0; i < list_ids.size(); i++) {
            map.put(list_ids.get(i), list_dates.get(i));
        }
        msg = intent.getParcelableExtra("msg");
        if (currentId != -1){
            sendId();
        }
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        return super.onStartCommand(intent, flags, startId);
    }

    private MusicBinder binder;

    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null) {
            binder = new MusicBinder();
        }
        return binder;
    }

    /**
     * 再次绑定时调用
     * @param intent
     */
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        //关闭通知
        manager.cancel("play", 0);
        build = null;
        if (tag){
            unregisterReceiver(musicReceiver);
        }
    }

    //设置播放模式
    public void setStateId(int stateId){
        this.stateId=stateId;
    }

    //自定义随机播放的东西
    public void setList_weightIds(ArrayList<Integer> list_weightIds) {
        this.list_weightIds = list_weightIds;
    }

    //改变歌单后的数据
    public void setList_ids(ArrayList<Integer> list_ids) {
        this.list_ids = list_ids;
    }

    /**
     * 播放完成时调用，用于做播放模式 MediaPlayer.setOnCompletionListener;
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (stateId){
            case 1:
                //单曲循环
                playOrPause(currentId);
                break;
            case 0:
                //循环播放
                playNext();
                break;
            case 2:
                //随机播放
                Random random = new Random(System.currentTimeMillis());
                if(list_weightIds !=null){
                    //权重随机播放
                    int j=random.nextInt(list_weightIds.size());
                    while (list_weightIds.get(j)==currentId){
                        j=random.nextInt(list_weightIds.size());
                    }
                    playOrPause(list_weightIds.get(j));
                }else{
                    //基本随机播放
                    int i = random.nextInt(list_ids.size());
                    while (list_ids.get(i) == currentId){
                        i = random.nextInt(list_ids.size());
                    }
                    playOrPause(list_ids.get(i));
                }
                break;
        }
    }

    public class MusicBinder extends Binder{
        public MusicService getService() {
            return MusicService.this;
        }
    }
    private int currentId = -1;

    public void playOrPause(){
        playOrPause(currentId);
    }
    /**
     * 播放或暂停
     * @param id
     */
    public void playOrPause(int id){
        //首次点播放，播放第一次歌
        if(list_ids.size()>0){
            if (id == -1){
                id = list_ids.get(0);
            }
            //是否播放当前歌曲
            if (id == currentId){
                //由播放状态决定播放或暂停
                if (player.isPlaying()){
                    player.pause();
                } else {
                    player.start();
                }
            } else {
                //如果是新歌播放
                player.reset();
                try {
                    //设置歌曲地址
                    player.setDataSource(map.get(id));
                    //准备
                    player.prepare();
                    //开始播放
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            currentId = id;
            sendId();
            if (build != null) {
                updateRemoteViews();
            }
        }
    }

    /**
     * 发送当前歌曲的ID
     */
    private void sendId() {
        Message message = Message.obtain();
        message.what = 0;
        message.arg1 = currentId;
        try {
            msg.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int getDuration(){
        //音频文件的总时长（单位：毫秒）
        return player.getDuration();
    }
    public int getCurrentPosition(){
        //当前时间（单位：毫秒）
        return player.getCurrentPosition();
    }

    /**
     * 改变音频当前时间
     * @param position
     */
    public void seekTo(int position){
        player.seekTo(position);
    }
    public boolean isPlaying(){
        return player.isPlaying();
    }

    /**
     * 上一首
     */
    public void playPrevious(){
        for (int i = 0; i < list_ids.size(); i++) {
            if (currentId == list_ids.get(i)){
                int temp = i - 1;
                if (temp < 0){
                    temp = list_ids.size() - 1;
                }
                playOrPause(list_ids.get(temp));
                return;
            }
        }
    }

    /**
     * 下一首
     */
    public void playNext(){
        for (int i = 0; i < list_ids.size(); i++) {
            if (currentId == list_ids.get(i)){
                int temp = i + 1;
                if (temp >= list_ids.size()){
                    temp = 0;
                }
                playOrPause(list_ids.get(temp));
                return;
            }
        }
    }

    /**
     * 解除绑定
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        //如果正在播放，发一个通知
        if (player.isPlaying()){
            //启动Activity的意图
            Intent intent1 = new Intent(this, MainActivity.class);
            //延时启动
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_ONE_SHOT);
            //自定义通知
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification_layout);
            build = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
//                    .setContentText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)))
                    .setContentIntent(pendingIntent)
                    .setContent(views)
                    //点击后消失
                    .setAutoCancel(true)
                    .build();
            //给通知上的按钮加监听，当点击时发送广播
            views.setOnClickPendingIntent(R.id.btn_play,
                    PendingIntent.getBroadcast(this, 0, new Intent(CustomAction.ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT));
            views.setOnClickPendingIntent(R.id.btn_next,
                    PendingIntent.getBroadcast(this, 0, new Intent(CustomAction.ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT));
            views.setOnClickPendingIntent(R.id.btn_previous,
                    PendingIntent.getBroadcast(this, 0, new Intent(CustomAction.ACTION_PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT));
            //大通知，API16以后用的
//            build.bigContentView = views;
            //不可以被用户取消 增加标记 |=
            build.flags |= Notification.FLAG_NO_CLEAR;
            //取消标记 &= ~
//            build.flags &= ~Notification.FLAG_NO_CLEAR;
            updateRemoteViews();
            //注册按钮发送广播的接收者
            IntentFilter filter = new IntentFilter(CustomAction.ACTION_PLAY);
            filter.addAction(CustomAction.ACTION_NEXT);
            filter.addAction(CustomAction.ACTION_PREVIOUS);
            registerReceiver(musicReceiver, filter);
            tag=true;
        }
        return true;
    }

    /**
     * 更新通知
     */
    private void updateRemoteViews() {
        Cursor cursor = getContentResolver().query(
//                //content://media/external/audio/media/abc
//                Uri.withAppendedPath(Uri.parse("content://media/external/audio/media"), "abc");

                //content://media/external/audio/media
                //追加一个路径
                Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentId + ""),
                null, null, null, null);
        cursor.moveToNext();
        build.contentView.setTextViewText(R.id.not_title,
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));

        build.contentView.setTextViewText(R.id.not_text,
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
        build.contentView.setImageViewResource(R.id.btn_play, isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
//        views.setCharSequence(R.id.not_title, "setText",cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
        cursor.close();

        manager.notify("play", 0, build);
    }
}
