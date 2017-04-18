package com.example.myplayer.act.pager;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myplayer.act.base.BasePager;
import com.example.myplayer.act.utils.LogUtil;

import org.xutils.common.Callback;
import org.xutils.x;

/**
 * Created by ldgd on 2016/12/10.
 * 介绍：网络音频页面
 */

public class NetAudioPager extends BasePager {
    private TextView textView;


    public NetAudioPager(Context context) {
        super(context);
    }


    @Override
    public View initView() {
        textView = new TextView(context);
        textView.setTextSize(20);
        textView.setTextColor(Color.BLUE);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络音频的数据被初始化了。。。");
        textView.setText("网络音频的内容");

    }
}
