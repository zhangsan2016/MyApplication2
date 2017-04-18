package com.example.myplayer.act;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplayer.R;
import com.example.myplayer.act.base.BasePager;
import com.example.myplayer.act.pager.AudioPager;
import com.example.myplayer.act.pager.NetAudioPager;
import com.example.myplayer.act.pager.NetVideoPager;
import com.example.myplayer.act.pager.VideoPager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ldgd on 2016/12/7.
 */

public class ActivityMain extends FragmentActivity {
    private FrameLayout mainContent;
    private RadioGroup rg_bottom_tag;
    /*
       页面集合
     */
    private List<BasePager> listPager;
    /*
       选中位置
     */
    private int position;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化视图
        initView();
        // 初始化监听
        initListeners();
        // 初始化参数
        initVariable();


    }

    private void initListeners() {
        rg_bottom_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());

    }

    private void initVariable() {
        listPager = new ArrayList<BasePager>();
        // 获取所有的pager
        listPager.add(new VideoPager(this));  // 本地视频
        listPager.add(new AudioPager(this));  // 本地音频
        listPager.add(new NetVideoPager(this)); // 网络视屏
        listPager.add(new NetAudioPager(this)); // 网络音频


        // 默认选中
        rg_bottom_tag.check(R.id.rb_video);

   /*     listPager.add(new AudioPager(this));
        listPager.add(new AudioPager(this));
        listPager.add(new AudioPager(this));
        listPager.add(new AudioPager(this));*/


    }

    private void initView() {
        mainContent = (FrameLayout) findViewById(R.id.fl_main_content);
        rg_bottom_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);
    }

    private class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkId) {

            switch (checkId) {
                default:
                    position = 0;
                    break;
                case R.id.rb_audio:
                    position = 1;
                    break;
                case R.id.rb_net_video:
                    position = 2;
                    break;
                case R.id.rb_netaudio:
                    position = 3;
                    break;

            }
            setFragment();

        }
        /**
         * 把页面添加到Fragment中
         */
        private void setFragment() {
            //1.得到FragmentManger
            FragmentManager manager = getSupportFragmentManager();
            //2.开启事务
            FragmentTransaction ft = manager.beginTransaction();
            //3.替换
            ft.replace(R.id.fl_main_content,new Fragment(){
                @Nullable
                @Override
                public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                    BasePager basePager =  getBasePager();
                    if(basePager != null){
                      return  basePager.rootView;
                    }
                    return null;
                }
            });

            // 4.提交事务
            ft.commit();

        }
    }


    /**
     * 根据位置得到对应的页面
     * @return
     */
    private BasePager getBasePager(){
        BasePager basePager = listPager.get(position);
        //  basePager.isInitData第一次打开页面加载数据
        if(basePager != null && !basePager.isInitData){
            basePager.initData();
            basePager.isInitData = true;
        }
        return basePager;

    }

    private boolean isExit = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(position != 0){  // 不是第一页面
                position = 0;
                rg_bottom_tag.check(R.id.rb_video); // 首页
                return true;
            }else if(!isExit){
                isExit = true;
                Toast.makeText(ActivityMain.this,"再按一次推出",Toast.LENGTH_SHORT).show();
                // 两秒后恢复
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                },2000);
                return true;
            }

        }


        return super.onKeyDown(keyCode, event);
    }
}
