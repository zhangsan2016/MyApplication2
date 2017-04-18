package com.example.myplayer.act.application;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.xutils.BuildConfig;
import org.xutils.x;

/**
 * Created by ldgd on 2017/2/6.
 * 介绍：
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        // 讯飞语音初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=58c12799");
        super.onCreate();
    }
}
