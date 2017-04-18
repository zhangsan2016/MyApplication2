package com.example.myplayer.act;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.example.myplayer.R;
import com.example.myplayer.act.utils.LogUtil;

public class Splash extends AppCompatActivity {
    private Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               startMain();
            }
        },3000);
    }

    private boolean isStartMain = false;
    /**
     * 跳转到主页面，并且把当前页面关闭掉
     */
    private synchronized void startMain() {
        if(!isStartMain){
            isStartMain = true;
            Intent  intent = new Intent(this,ActivityMain.class);
            startActivity(intent);
            //关闭当前页面
            this.finish();
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMain();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        myHandler.removeCallbacksAndMessages(null);
        super.onDestroy();

    }
}
