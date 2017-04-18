package com.example.myplayer.act.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myplayer.R;
import com.example.myplayer.act.domain.MediaItem;
import com.example.myplayer.act.utils.Utils;

import java.util.List;

/**
 * Created by ldgd on 2016/12/21.
 * 介绍：VideoPagerAdapter
 */

public class VideoPagerAdapter extends BaseAdapter {
    private final boolean isVideo;
    private List<MediaItem> mediaItems;
    private Context context;
    private Utils utils;

    public VideoPagerAdapter(Context context, List<MediaItem> mediaItems,boolean isVideo) {
        this.mediaItems = mediaItems;
        this.context = context;
        this.isVideo = isVideo;
        this.utils = new Utils();
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
            convertView =  View.inflate(context, R.layout.item_video_pager,null);

            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            viewHoder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);

            convertView.setTag(viewHoder);
        }else {
            viewHoder = (ViewHoder) convertView.getTag();
        }
        // 根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_time.setText(utils.stringForTime((int) mediaItem.getDuration()));
        viewHoder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));

        // 判断视屏
        if(!isVideo){
            //音频
            viewHoder.iv_icon.setImageResource(R.drawable.music_default_bg);
        }

        return convertView;
    }

    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }
}
