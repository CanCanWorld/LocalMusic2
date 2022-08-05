package com.et.localmusic2.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.et.localmusic2.R;
import com.et.localmusic2.bean.Song;
import com.et.localmusic2.ui.MainActivity;
import com.et.localmusic2.utils.Constants;
import com.et.localmusic2.utils.MusicUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private static final String TAG = "MusicService";
    private static RemoteViews remoteViews;
    private static NotificationManager manager;
    private MusicReceiver musicReceiver;
    private final List<Song> list = new ArrayList<>();
    public MediaPlayer mediaPlayer;
    int mPosition = -1;
    private final int NOTIFICATION_ID = 1;
    private static Notification notification;
    private OnButtonClick onButtonClick;

    public interface OnButtonClick {
        void buttonClick(String state);
    }

    public void setOnButtonClick(OnButtonClick onButtonClick) {
        this.onButtonClick = onButtonClick;
    }

    public MusicService() {
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onCreate() {
        super.onCreate();
        initData();
        initRemoteViews();
        registerMusicReceiver();
        showNotification();
    }

    private void initData() {
        list.clear();
        list.addAll(MusicUtils.getMusicData(this));
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> nextMusic());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicReceiver != null) {
            //解除动态注册的广播
            unregisterReceiver(musicReceiver);
        }
    }

    /**
     * 更改通知的信息和UI
     *
     * @param position 歌曲位置
     */
    public void updateNotificationShow(int position) {
        if (mediaPlayer.isPlaying()) {
            remoteViews.setImageViewResource(R.id.btn_notification_play, R.drawable.ic_baseline_pause_24);
        } else {
            remoteViews.setImageViewResource(R.id.btn_notification_play, R.drawable.ic_baseline_play_arrow_24);
        }
        Song song = list.get(position);
        Bitmap albumPic = MusicUtils.getAlbumPic(song.getPath());
        Matrix matrix = new Matrix();
        matrix.setScale(0.2f, 0.2f);
        albumPic = Bitmap.createBitmap(albumPic, 0, 0, albumPic.getWidth(), albumPic.getHeight(), matrix, false);
        if (albumPic != null) {
            remoteViews.setImageViewBitmap(R.id.iv_album_cover, albumPic);
        } else {
            remoteViews.setImageViewResource(R.id.iv_album_cover, R.drawable.pic_1);
        }
        remoteViews.setTextViewText(R.id.tv_notification_song_name, song.getSong());
        remoteViews.setTextViewText(R.id.tv_notification_singer, song.getSinger());
        //发送通知
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * 显示通知
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"UnspecifiedImmutableFlag", "RemoteViewLayout"})
    private void showNotification() {
        String channelId = "play_control";
        String channelName = "播放控制";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        createNotificationChannel(channelId, channelName, importance);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new NotificationCompat.Builder(this, channelId)
//                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.pic_1)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.pic_1))
                .setCustomContentView(remoteViews)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * 创建通知渠道
     *
     * @param channelId   渠道id
     * @param channelName 渠道名称
     * @param importance  渠道重要性
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setVibrationPattern(new long[]{0});
        channel.setSound(null, null);
        //获取系统通知服务
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    /**
     * 注册动态广播
     */
    private void registerMusicReceiver() {
        musicReceiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.PLAY);
        intentFilter.addAction(Constants.PREV);
        intentFilter.addAction(Constants.NEXT);
        intentFilter.addAction(Constants.CLOSE);
        registerReceiver(musicReceiver, intentFilter);
    }

    /**
     * 初始化自定义通知栏 的按钮点击事件
     */
    @SuppressLint({"UnspecifiedImmutableFlag", "RemoteViewLayout"})
    private void initRemoteViews() {
        remoteViews = new RemoteViews(this.getPackageName(), R.layout.notification);

        //通知栏控制器上一首按钮广播操作
        Intent intentPrev = new Intent(Constants.PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, intentPrev, 0);
        //为prev控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_previous, prevPendingIntent);

        //通知栏控制器播放暂停按钮广播操作  //用于接收广播时过滤意图信息
        Intent intentPlay = new Intent(Constants.PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
        //为play控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_play, playPendingIntent);

        //通知栏控制器下一首按钮广播操作
        Intent intentNext = new Intent(Constants.NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, intentNext, 0);
        //为next控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_next, nextPendingIntent);

        //通知栏控制器关闭按钮广播操作
        Intent intentClose = new Intent(Constants.CLOSE);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 0, intentClose, 0);
        //为close控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_close, closePendingIntent);
    }

    public void play(int position) {
        mPosition = position;
        if (mPosition == -1) {
            mPosition++;
        }
        Log.d(TAG, "play: " + mPosition);
        //监听音乐播放完毕事件，自动下一曲'
        //播放时 获取当前歌曲列表是否有歌曲
        if (list.size() <= 0) {
            return;
        }
        try {
            //切歌前先重置，释放掉之前的资源
            mediaPlayer.reset();
            //设置播放音频的资源路径
            mediaPlayer.setDataSource(list.get(mPosition).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            //显示通知
            updateNotificationShow(mPosition);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void previousMusic() {
        Log.d(TAG, "previousMusic: " + mPosition);
        if (mPosition <= 0) {
            mPosition = list.size() - 1;
        } else {
            mPosition -= 1;
        }
        play(mPosition);
    }

    public void nextMusic() {
        Log.d(TAG, "nextMusic: " + mPosition);
        if (mPosition >= list.size() - 1) {
            mPosition = 0;
        } else {
            mPosition += 1;
        }
        play(mPosition);
    }

    public void playOrPause() {
        if (mPosition == -1) {
            play(0);
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
        //更改通知栏播放状态
        updateNotificationShow(mPosition);
    }

    public void closeNotification() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
        manager.cancel(NOTIFICATION_ID);
    }

    /**
     * 广播接收器 （内部类）
     */
    public class MusicReceiver extends BroadcastReceiver {

        public static final String TAG = "MusicReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            //UI控制
            UIControl(intent.getAction());
        }

        /**
         * 页面的UI 控制 ，通过服务来控制页面和通知栏的UI
         *
         * @param state 状态码
         */
        private void UIControl(String state) {
            onButtonClick.buttonClick(state);
            switch (state) {
                case Constants.PLAY:
                    playOrPause();
                    Log.d(TAG, "UIControl: play");
                    break;
                case Constants.PREV:
                    previousMusic();
                    Log.d(TAG, "UIControl: prev");
                    break;
                case Constants.NEXT:
                    nextMusic();
                    Log.d(TAG, "UIControl: next");
                    break;
                case Constants.CLOSE:
                    closeNotification();
                    Log.d(TAG, "UIControl: close");
                    break;
                default:
                    break;
            }
        }
    }
}