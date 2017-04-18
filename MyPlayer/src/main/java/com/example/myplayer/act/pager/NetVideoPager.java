package com.example.myplayer.act.pager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myplayer.R;
import com.example.myplayer.act.SystemVideoPlayer;
import com.example.myplayer.act.View.XListView;
import com.example.myplayer.act.adapter.NetVideoPagerAdapter;
import com.example.myplayer.act.base.BasePager;
import com.example.myplayer.act.domain.MediaItem;
import com.example.myplayer.act.utils.CacheUtils;
import com.example.myplayer.act.utils.Constants;
import com.example.myplayer.act.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ldgd on 2016/12/10.
 * 介绍：网络视屏页面
 */

public class NetVideoPager extends BasePager {

    @ViewInject(R.id.net_video_listview)
    private XListView mListview;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mProgressBar;

    @ViewInject(R.id.tv_nonet)
    private TextView mTv_nonet;

    /**
     * 装数据集合
     */
    private ArrayList<MediaItem> mediaItems;
    private NetVideoPagerAdapter adapter;
    /**
     * 是否已经加载更多了(用来判断加载更多还是刷新)
     */
    private boolean isLoadMore = false;


    public NetVideoPager(Context context) {
        super(context);

    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager,null);
        //第一个参数是：NetVideoPager.this,第二个参数：布局
        x.view().inject(NetVideoPager.this, view);

        mListview.setOnItemClickListener(new MyOnItemClickListener());
        mListview.setPullLoadEnable(true);
        mListview.setXListViewListener(new MyIXListViewListener());
        return view;
    }


    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络视屏的数据被初始化了。。。");
        // 获取缓存数据
        String saveJson = CacheUtils.getString(context,Constants.NET_URL);
        if(!TextUtils.isEmpty(saveJson)){
           processData(saveJson);
        }
        getDataFromNet();

    }
    private class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            getDataFromNet();
        }

        @Override
        public void onLoadMore() {
            getMoreDataFromNet();
        }
    }
    public void getMoreDataFromNet(){
        // 联网获取视屏内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功==" + result);
                isLoadMore = true;
                // 解析json
                processData(result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败==" + ex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");   isLoadMore = false;
            }
        });
    }
    public void getDataFromNet() {
        // 联网获取视屏内容
        RequestParams params = new RequestParams(Constants.NET_URL);
         x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功==" + result);
                // 缓存数据
                CacheUtils.putString(context,Constants.NET_URL,result);
                // 解析json
                processData(result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());

            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });

    }

    private void processData(String json) {
        if(!isLoadMore){
            // 解析listView
            mediaItems = parseJson(json);
            // 显示数据到listView
            showData();

        }else{
            //加载更多
            //要把得到更多的数据，添加到原来的集合中
            isLoadMore = false;
            mediaItems.addAll(parseJson(json));
            //刷新适配器
            adapter.notifyDataSetChanged();
            onLoad();
        }


    }

    private void onLoad() {
        mListview.stopRefresh();
        mListview.stopLoadMore();
        mListview.setRefreshTime("更新时间:" + getSysteTime());
    }

    /**
     * 解决json数据：
     * 1.用系统接口解析json数据
     * 2.使用第三方解决工具（Gson,fastjson）
     *
     */
    private ArrayList<MediaItem> parseJson(String json){
        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArry = jsonObject.optJSONArray("trailers");

            if(jsonArry != null && jsonArry.length() > 0){

                for (int i = 0; i < jsonArry.length(); i++) {

                   JSONObject jsonObjectItem = (JSONObject) jsonArry.get(i);

                    if(jsonObjectItem != null){

                        MediaItem mediaItem = new MediaItem();

                        String movieName =  jsonObjectItem.optString("movieName"); // name
                        mediaItem.setName(movieName);

                        String videoTitle =  jsonObjectItem.optString("videoTitle"); // desc
                        mediaItem.setDesc(videoTitle);

                        String imageUrl =  jsonObjectItem.optString("coverImg");  // imageUrl
                        mediaItem.setImageUrl(imageUrl);

                        String hightUrl =  jsonObjectItem.optString("hightUrl"); // data
                        mediaItem.setData(hightUrl);

                        // 添加到集合中
                        mediaItems.add(mediaItem);
                    }

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return mediaItems;
    }

    private void showData() {
        if(mediaItems != null && mediaItems.size() > 0){
            adapter = new NetVideoPagerAdapter(context,mediaItems);
            mListview.setAdapter(adapter);
            //把文本隐藏
            mTv_nonet.setVisibility(View.GONE);
            onLoad();
        }else{
            //没有数据
            //文本显示
            mTv_nonet.setVisibility(View.VISIBLE);
        }

        //ProgressBar隐藏
        mProgressBar.setVisibility(View.GONE);
    }

    private class MyOnItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //3.传递列表数据-对象-序列化
            Intent intent = new Intent(context,SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position-1);
            context.startActivity(intent);
        }
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

}
