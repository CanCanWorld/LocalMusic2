package com.et.localmusic2.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.et.localmusic2.R;
import com.et.localmusic2.adapter.MusicAdapter;
import com.et.localmusic2.base.BaseActivity;
import com.et.localmusic2.bean.Song;
import com.et.localmusic2.service.MusicService;
import com.et.localmusic2.utils.Constants;
import com.et.localmusic2.utils.MusicUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = "LocalMusicActivity";

    private RecyclerView mRvMusic;
    private MusicAdapter adapter;
    private FloatingActionButton mFloatingBtn;
    private MaterialTextView mTvSongName;
    private MaterialButton mBtnPlay;
    private ObjectAnimator albumAnimator;
    private ShapeableImageView mIvAlbum;
    private MusicService.MusicBinder musicBinder;
    private MusicService musicService;
    private final ServiceConnection connection = new ServiceConnection() {
        @SuppressLint({"NotifyDataSetChanged", "UseCompatLoadingForDrawables", "SetTextI18n"})
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;
            musicService = musicBinder.getService();
            musicService.setOnButtonClick(state -> {
                switch (state) {
                    case Constants.PLAY:
                        if (mPosition == -1) {
                            mPosition = 0;
                            list.get(mPosition).setCheck(true);
                            adapter.notifyDataSetChanged();
                        }
                        if (isPlaying) {
                            isPlaying = false;
                            pauseStyle();
                        } else {
                            isPlaying = true;
                            playStyle();
                        }
                        break;
                    case Constants.PREV:
                        if (mPosition != -1) list.get(mPosition).setCheck(false);
                        if (mPosition <= 0) {
                            mPosition = list.size() - 1;
                        } else {
                            mPosition -= 1;
                        }
                        isPlaying = true;
                        Song s1 = list.get(mPosition);
                        s1.setCheck(true);
                        adapter.notifyDataSetChanged();
                        playStyle();
                        break;
                    case Constants.NEXT:
                        if (mPosition != -1) list.get(mPosition).setCheck(false);
                        if (mPosition >= list.size() - 1) {
                            mPosition = 0;
                        } else {
                            mPosition += 1;
                        }
                        isPlaying = true;
                        Song s2 = list.get(mPosition);
                        s2.setCheck(true);
                        adapter.notifyDataSetChanged();
                        playStyle();
                        break;
                    case Constants.CLOSE:
                        finish();
                        break;
                    default:
                        break;
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBinder = null;
        }
    };

    private boolean isPlaying;
    private int mPosition;
    //请求状态码
    private static final int REQUEST_PERMISSION_CODE = 1;
    //读写权限
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private List<Song> list;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        mRvMusic = findViewById(R.id.recycler_view);
        mFloatingBtn = findViewById(R.id.floating_btn);
        mTvSongName = findViewById(R.id.tv_song_name);
        mBtnPlay = findViewById(R.id.btn_play);
        mIvAlbum = findViewById(R.id.iv_album);
    }


    @Override
    public void initData() {
        isPlaying = false;
        mPosition = -1;
        list = new ArrayList<>();
        initAnimation();
        layoutManager = new LinearLayoutManager(context);
        mRvMusic.setLayoutManager(layoutManager);
        adapter = new MusicAdapter(context, list);
        mRvMusic.setAdapter(adapter);
        Intent serviceIntent = new Intent(context, MusicService.class);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
        permissionsRequest();
        refreshList();
    }

    @SuppressLint({"NotifyDataSetChanged", "UseCompatLoadingForDrawables"})
    @Override
    public void initEvent() {
        adapter.setOnClickListener(position -> {
            isPlaying = true;
            if (mPosition != -1) list.get(mPosition).setCheck(false);
            list.get(position).setCheck(true);
            mPosition = position;
            adapter.notifyDataSetChanged();
            playStyle();
            musicService.play(mPosition);
        });
        mFloatingBtn.setOnClickListener(v -> {
            movePlayPosition();
        });
        mRvMusic.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    showFloatBtn(false);
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    showFloatBtn(true);
                }
            }
        });
        mBtnPlay.setOnClickListener(v -> {
            if (mPosition == -1) {
                mPosition = 0;
                list.get(mPosition).setCheck(true);
                adapter.notifyDataSetChanged();
                isPlaying = true;
                playStyle();
                musicService.play(mPosition);
            } else {
                musicService.playOrPause();
                if (isPlaying) {
                    pauseStyle();
                    isPlaying = false;
                } else {
                    playStyle();
                    isPlaying = true;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void movePlayPosition() {
        if (mPosition == -1) return;
        layoutManager.scrollToPosition(mPosition);
    }

    /**
     * 初始化动画
     */
    private void initAnimation() {
        albumAnimator = ObjectAnimator.ofFloat(mIvAlbum, "rotation", 0.0f, 360.0f);
        albumAnimator.setDuration(6000);
        albumAnimator.setInterpolator(new LinearInterpolator());
        albumAnimator.setRepeatCount(-1);
        albumAnimator.setRepeatMode(ObjectAnimator.RESTART);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单按钮监听
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_fresh:
                refreshList();
                break;
            case R.id.menu_test:
                Toast.makeText(context, "还在做", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 刷新列表
     */
    @SuppressLint("NotifyDataSetChanged")
    private void refreshList() {
        list.clear();
        list.addAll(MusicUtils.getMusicData(context));
        mPosition = -1;
        mTvSongName.setText("暂无播放");
        mIvAlbum.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_1));
        adapter.notifyDataSetChanged();
    }

    /**
     * 显示悬浮按钮
     * @param isScroll  是否滚动
     */
    private void showFloatBtn(boolean isScroll) {
        if (mPosition == -1) return;
        if (isScroll) {
            mFloatingBtn.setVisibility(View.VISIBLE);
        } else {
            new Handler(Looper.myLooper()).postDelayed(() -> {
                mFloatingBtn.setVisibility(View.GONE);
            }, 2000);
        }
    }

    /**
     * 请求权限
     */
    private void permissionsRequest() {
        new Thread(() -> {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }).start();
    }

    /**
     * 申请权限的回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            Log.d(TAG, "申请权限成功");
            refreshList();
        }
    }

    /**
     * 更改为播放样式
     */
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void playStyle() {
        Song song = list.get(mPosition);
        if (MusicUtils.getAlbumPic(song.getPath()) != null) {
            mIvAlbum.setImageBitmap(MusicUtils.getAlbumPic(song.getPath()));
        } else {
            mIvAlbum.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pic_1));
        }
        mTvSongName.setText(song.getSong() + "-" + song.getSinger());
        mBtnPlay.setIcon(getDrawable(R.drawable.zanting_bg));
        mBtnPlay.setIconTint(getColorStateList(R.color.gold_color));
        albumAnimator.start();
    }

    /**
     * 更改为暂停样式
     */
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void pauseStyle() {
        mBtnPlay.setIcon(getDrawable(R.drawable.bofang_bg));
        mBtnPlay.setIconTint(getColorStateList(R.color.white));
        albumAnimator.pause();
    }
}


