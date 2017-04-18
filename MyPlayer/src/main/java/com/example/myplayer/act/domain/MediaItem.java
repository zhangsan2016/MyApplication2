package com.example.myplayer.act.domain;

import java.io.Serializable;

/**
 * Created by ldgd on 2016/12/21.
 * 介绍：代表一个视频和音频
 */

public class MediaItem implements Serializable {

    /*
       媒体名称
     */
    private String name;
    /*
       媒体时长
      */
    private long duration;
    /*
       媒体大小
     */
    private long size;
    /*
       媒体数据
      */
    private String data;
    /*
          媒体作者
      */
    private String artist;

    private String desc;

    private String imageUrl;


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", artist='" + artist + '\''+
                ", desc='" + desc + '\''+
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }


}
