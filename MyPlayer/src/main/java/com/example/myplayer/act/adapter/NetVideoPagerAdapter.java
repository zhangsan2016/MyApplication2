package com.example.myplayer.act.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myplayer.R;
import com.example.myplayer.act.domain.MediaItem;
import com.example.myplayer.act.utils.LogUtil;
import com.example.myplayer.act.utils.Utils;

import org.xutils.x;

import java.util.List;

/**
 * Created by ldgd on 2016/12/21.
 * 介绍：VideoPagerAdapter
 */

public class NetVideoPagerAdapter extends BaseAdapter {
    private List<MediaItem> mediaItems;
    private Context context;

    public NetVideoPagerAdapter(Context context, List<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mediaItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if(convertView == null){
            viewHoder = new ViewHoder();

            convertView =  View.inflate(context, R.layout.item_netvideo_pager,null);
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);

            convertView.setTag(viewHoder);
        }else {
            viewHoder = (ViewHoder) convertView.getTag();
        }

        // 根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_desc.setText(mediaItem.getDesc());

        //1.使用xUtils3请求图片
      //  x.image().bind(viewHoder.iv_icon,mediaItem.getImageUrl());

        //2.使用Glide请求图片
        Glide.with(context).load(mediaItem.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.video_default)
                .error(R.drawable.video_default)
                .into(viewHoder.iv_icon);

        //3.使用Picasso 请求图片
//        Picasso.with(context).load(mediaItem.getImageUrl())
//               .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.video_default)
//                .error(R.drawable.video_default)
//                .into(viewHoder.iv_icon);

        return convertView;
    }

    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}
