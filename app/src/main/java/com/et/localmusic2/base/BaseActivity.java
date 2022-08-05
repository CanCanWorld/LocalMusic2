package com.et.localmusic2.base;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int FAST_CLICK_DELAY_TIME = 500;

    private static long lastClickTime;

    protected Activity context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(initLayout());
        initView();
        initData();
        initEvent();
    }

    protected abstract int initLayout();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initEvent();


    /**
     * 返回
     */
    protected void Back(Toolbar toolbar) {
        toolbar.setNavigationOnClickListener(v -> {
            context.finish();
            if (!isFastClick())
                context.finish();
        });
    }

    /**
     * 两次点击间隔不少于500ms
     */
    protected static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= FAST_CLICK_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return false;
    }

    /**
     * 消息提示
     */
    protected void show(CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
