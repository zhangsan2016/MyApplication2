package com.example.myplayer.act.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myplayer.act.utils.LogUtil;

/**
 * Created by ldgd on 2016/12/10.
 * 介绍：基类，公共类，
 * VideoPager
 * <p/>
 * AudioPager
 * <p/>
 * NetVideoPager
 * <p/>
 * NetAudioPager
 * 都继承该类
 */

public abstract class BasePager extends Fragment {
    public  final Context context;
    public boolean isInitData;
    public View rootView;

    public BasePager(Context context) {
        this.context = context;
        rootView = initView();
    }

    public abstract View initView();
    /*
       子类根据需要实现
     */
    public void initData(){};
}
