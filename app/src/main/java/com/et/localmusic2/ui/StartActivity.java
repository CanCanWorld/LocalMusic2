package com.et.localmusic2.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.et.localmusic2.R;
import com.google.android.material.imageview.ShapeableImageView;

public class StartActivity extends AppCompatActivity {

    private TranslateAnimation translateAnimation;
    private ShapeableImageView mAzi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();
    }

    private void initView() {
        mAzi = findViewById(R.id.iv_azi);
        Glide.with(this)
                .asGif()
                .load(R.drawable.gif)
                .into(mAzi);
        TextView mTvTranslate = findViewById(R.id.tv_translate);
        mTvTranslate.post(() -> {
            translateAnimation = new TranslateAnimation(0, mTvTranslate.getWidth(), 0, 0);
            translateAnimation.setDuration(3000);
            translateAnimation.setFillAfter(true);
            mTvTranslate.startAnimation(translateAnimation);

            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        });
    }
}