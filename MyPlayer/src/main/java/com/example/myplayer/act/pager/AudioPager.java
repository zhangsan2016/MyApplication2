package com.example.myplayer.act.pager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myplayer.R;
import com.example.myplayer.act.AudioPlayerActivity;
import com.example.myplayer.act.adapter.VideoPagerAdapter;
import com.example.myplayer.act.base.BasePager;
import com.example.myplayer.act.domain.MediaItem;
import com.example.myplayer.act.utils.LogUtil;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;


/**
 * Created by ldgd on 2016/12/10.
 * 介绍：本地音频页面
 */

public class AudioPager extends BasePager {

    @ViewInject(R.id.listview)
    private ListView listview;

    @ViewInject(R.id.tv_nomedia)
    private TextView tv_nomedia;

    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;

    private VideoPagerAdapter videoPagerAdapter;

    /**
     * 装数据集合
     */
    private ArrayList<MediaItem> mediaItems;

    private Handler ShowViewHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(mediaItems != null && mediaItems.size() > 0){
                 // 绑定数据到listView
                videoPagerAdapter = new VideoPagerAdapter(context,mediaItems,false);
                listview.setAdapter(videoPagerAdapter);
                videoPagerAdapter.notifyDataSetChanged();
                tv_nomedia.setVisibility(View.GONE);

            }else{
                tv_nomedia.setText("没有发现音频....");
                tv_nomedia.setVisibility(View.VISIBLE);
            }

            //ProgressBar隐藏
            pb_loading.setVisibility(View.GONE);

        }
    };
    /**
     * 构造方法
     * @param context
     */
    public AudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        LogUtil.e("AudioPager initView。。。");
        View view = View.inflate(context, R.layout.audio_pager, null);
        // 把当前View绑定到当前Activity
        x.view().inject(AudioPager.this, view);

        //设置ListView的Item的点击事件
        listview.setOnItemClickListener(new MyOnItemClickListener());

        return view;
    }

    /**
     * 网络获取数据
     */
    public void initData() {
        LogUtil.e("本地音频的数据被初始化了。。。");
        //加载本地视频数据
        getDataFromLocal();
    }

    /**
     * 从本地的sdcard得到数据
     * //1.遍历sdcard,后缀名
     * //2.从内容提供者里面获取视频
     * //3.如果是6.0的系统，动态获取读取sdcard的权限
     */
    private void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                //    isGrantExternalRW((Activity) context);
                //  SystemClock.sleep(2000);

                mediaItems = new ArrayList<>();

                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] object = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Audio.Media.DURATION,//视频总时长
                        MediaStore.Audio.Media.SIZE,//视频的文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST,//歌曲的演唱者
                };
                Cursor cursor = resolver.query(uri, object, null, null, null);
                while (cursor.moveToNext()) {
                    if (cursor != null) {
                        MediaItem mediaItem = new MediaItem();
                        // 可以先添加到集合
                        mediaItems.add(mediaItem);

                        String name = cursor.getString(0); //音乐的名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1); //音乐的时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);  //音乐的文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3); //音乐的播放地址
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);  //艺术家
                        mediaItem.setArtist(artist);
                    }
                }
                cursor.close();  // 关闭cursor
                ShowViewHandle.sendEmptyMessage(0);

            }
        }.start();


    }


    private class MyOnItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.e("MyOnItemClickListener" + position);
            Intent intent = new Intent(context,AudioPlayerActivity.class);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }
}
