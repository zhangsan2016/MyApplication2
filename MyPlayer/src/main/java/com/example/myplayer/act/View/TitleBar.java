package com.example.myplayer.act.View;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplayer.R;
import com.example.myplayer.act.SearchActivity;

/**
 * Created by ldgd on 2016/12/14.
 * 介绍：自定义标题栏
 */

public class TitleBar extends LinearLayout implements View.OnClickListener {
    // 使用View便于以后对xml中对布局的更换
    private View tv_search;
    private View tv_game;
    private View tv_record;

    private Context context;

    /**
     * 在代码中实例化该类的时候使用这个方法
     *
     * @param context
     */
    public TitleBar(Context context) {
        this(context, null);
    }

    /**
     * 当布局文件使用该类的时候，Android系统通过这个构造方法实例化该类
     *
     * @param context
     * @param attrs
     */
    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    /**
     * 当需要设置样式的时候，可以使用该方法
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    /**
     * 当布局文件加载完成后调用方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 得到孩子的实例
        tv_search = getChildAt(1);
        tv_game = getChildAt(2);
        tv_record = getChildAt(3);
        TextView tt = (TextView) this.findViewById(R.id.tv_search);

        // 设置点击事件
        tv_search.setOnClickListener(this);
        tv_game.setOnClickListener(this);
        tv_record.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search:
                //Toast.makeText(context,"搜索",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, SearchActivity.class);
                context.startActivity(intent);

                break;
            case R.id.rl_game:
                Toast.makeText(context,"游戏",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_record:
                Toast.makeText(context,"历史",Toast.LENGTH_SHORT).show();
                break;
            

        }
    }
}
