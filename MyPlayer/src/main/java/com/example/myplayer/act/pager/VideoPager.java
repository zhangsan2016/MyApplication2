package com.example.myplayer.act.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplayer.R;
import com.example.myplayer.act.SystemVideoPlayer;
import com.example.myplayer.act.adapter.VideoPagerAdapter;
import com.example.myplayer.act.base.BasePager;
import com.example.myplayer.act.domain.MediaItem;
import com.example.myplayer.act.utils.LogUtil;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldgd on 2016/12/10.
 * 介绍：本地视频页面
 */

public class VideoPager extends BasePager {
    private ListView lvVieo;
    private TextView tvNoMedia;
    private ProgressBar pbVideoLoading;

    /**
     * 装数据集合
     */
    private List<MediaItem> mediaItems;

    public VideoPager(Context context) {
        super(context);

    }

    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mediaItems != null && mediaItems.size() > 0){

                // 有数据
                // 设置数据适配器
                VideoPagerAdapter videoPagerAdapter = new VideoPagerAdapter(context,mediaItems,true);
                lvVieo.setAdapter(videoPagerAdapter);

                // 不显示textView
                pbVideoLoading.setVisibility(View.GONE);
            }else{
                // 没有数据显示textView
                tvNoMedia.setVisibility(View.VISIBLE);

            }

            // 隐藏ProgressBar
            pbVideoLoading.setVisibility(View.GONE);

        }
    };

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.video_pager, null);
        lvVieo = (ListView) view.findViewById(R.id.lv_video);
        tvNoMedia = (TextView) view.findViewById(R.id.tv_no_media);
        pbVideoLoading = (ProgressBar) view.findViewById(R.id.pb_video_loading);

         // 设置ListView的Item的点击事件
        lvVieo.setOnItemClickListener(new MyOnItemClickListener());

        return view;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.d("本地视频的数据被初始化了。。。");
        //加载本地视频数据
        getDataFromLocal();
    }


    /**
     * 从本地的sdcard得到数据
     * //1.遍历sdcard,后缀名
     * //2.从内容提供者里面获取视频
     * //3.如果是6.0的系统，动态获取读取sdcard的权限
     */
    public void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();
       /*         try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                mediaItems = new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Video.Media.DURATION,//视频总时长
                        MediaStore.Video.Media.SIZE,//视频的文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST,//歌曲的演唱者

                };
                // 获取内容提供者数据
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                // 遍历数据
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        MediaItem mediaItem = new MediaItem();

                        mediaItems.add(mediaItem);//写在上面

                        String name = cursor.getString(0);//视频的名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);//视频的时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);//视频的文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);//视频的播放地址
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);//艺术家
                        mediaItem.setArtist(artist);

                    }
                     // 关闭
                    cursor.close();
                }
                myHandler.sendEmptyMessage(1);

            }
        }.start();


    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener{


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            MediaItem mediaItem =  mediaItems.get(position);

            //1.调起系统所有的播放-隐式意图
    /*        Intent intent =  new Intent();
            intent.setDataAndType(Uri.parse(mediaItem.getData()),"video*//*");
            context.startActivity(intent);*/

            // 2.调用自己写的播放器-显示意图
    /*        Intent intent = new Intent(context,SystemVideoPlayer.class);
            intent.setDataAndType(Uri.parse(mediaItem.getData()),"video");
            context.startActivity(intent);*/

            Intent intent = new Intent(context,SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", (Serializable) mediaItems);
            bundle.putInt("position",position);
            intent.putExtras(bundle);
            context.startActivity(intent);


        }
    }

}
