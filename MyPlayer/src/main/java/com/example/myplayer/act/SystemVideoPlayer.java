package com.example.myplayer.act;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplayer.R;
import com.example.myplayer.act.View.VideoView;
import com.example.myplayer.act.domain.MediaItem;
import com.example.myplayer.act.utils.LogUtil;
import com.example.myplayer.act.utils.Utils;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/**
 * Created by ldgd on 2016/12/23.
 * 介绍：系统播放器
 */
public class SystemVideoPlayer extends Activity implements View.OnClickListener {
    private VideoView videoView;
    private Utils utils;

    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private Button btnSwichPlayer;
    private RelativeLayout mediaController;
    private LinearLayout ll_buffer;
    private TextView tv_buffer_netspeed;
    private TextView tv_laoding_netspeed;
    private LinearLayout ll_loading;


    /**
     * 传入进来的视频列表
     */
    private ArrayList<MediaItem> mediaItems;
    /**
     * 隱藏控制面板
     */
    private static final int HIDE_MEDIACONTROLLER = 2;
    /**
     * 进度
     */
    public static final int PROGRESS = 1;
    /**
     * 显示网速
     */
    public static final int SHOW_SPEED = 3;
    /**
     * 要播放的列表中的具体位置
     */
    private int position;
    private Uri uri;
    /**
     * 监听电量变化的广播
     */
    private MyReceiver myReceiver;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    /**
     * 手势控制器
     */
    private GestureDetector detector;
    /**
     * 是否显示控制面板
     */
    private boolean isshowMediaController = false;
    /**
     * 是否静音
     */
    private boolean isMute = false;
    /**
     * 调用声音
     */
    private AudioManager am;
    /**
     * 当前音量
     */
    private int currentAudio;

    /**
     * 最大音量
     */
    private int maxAudio;
    /**
     * 屏宽
     */
    private int screenWidth;
    /**
     * 屏高
     */
    private int screenHeight;
    /**
     * 真是视屏的宽
     */
    private int videoWidth;
    /**
     * 真是视屏的高
     */
    private int videoHeight;
    /**
     * 是否全屏状态
     */
    private boolean isFullScreen = false;
    /**
     * 全屏
     */
    private final int FULL_SCREEN = 1;
    /**
     * 默认屏幕
     */
    private final int DEFAULT_SCREEN = 2;
    /**
     * 是否网络视屏
     */
    private boolean isNetUri;
    /**
     * 是否使用系统监听卡
     */
    public boolean isUseSystem = true;
    /**
     * 上一次的播放进度
     */
    private int precurrentPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video_player);

        initData();

        initView();

        setListener();

        getData();
        setData();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    private Handler MyHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:

                    // 设置视屏进度
                    int currentPosition = videoView.getCurrentPosition();

                    //SeekBar.setProgress(当前进度);
                    seekbarVideo.setProgress(currentPosition);

                    //更新文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));

                    // 设置系统时间
                    tvSystemTime.setText(getTvSystemTime());

                    // 判断是否网络视屏、缓存进度的更新
                    if (isNetUri) {
                        //只有网络资源才有缓存效果
                        int buffer = videoView.getBufferPercentage(); // 0 ~ 100
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);

                    } else {
                        //本地视频没有缓冲效果
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    //监听卡（判断是否使用系统监听卡，Android2.3以后有，4.2后集成到videoview中）
                    if (!isUseSystem && videoView.isPlaying()) {
                        if (videoView.isPlaying() && currentPosition != 0) {
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 600) {
                                //视频卡了
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡了
                                ll_buffer.setVisibility(View.GONE);
                            }
                        } else {
                            //视频不卡了
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    precurrentPosition = currentPosition;


                    //每秒更新一次进度
                    MyHandler.removeMessages(PROGRESS);
                    if (videoView.isPlaying()) {
                        MyHandler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    }


                    break;
                case HIDE_MEDIACONTROLLER:

                    hideMediaController();

                    break;
                case SHOW_SPEED: //显示网速

                    //得到网络速度
                    String netSpeed = utils.getNetSpeed(SystemVideoPlayer.this);
                    //显示网络速
                    tv_laoding_netspeed.setText("玩命加载中..." + netSpeed);
                    tv_buffer_netspeed.setText("缓存中..." + netSpeed);
                    //每两秒更新一次
                    MyHandler.removeMessages(SHOW_SPEED);
                    MyHandler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                    break;
            }
        }
    };

    private void getData() {
        uri = this.getIntent().getData();
        // mediaItems = (ArrayList<MediaItem>) this.getIntent().getSerializableExtra("videolist");
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = this.getIntent().getIntExtra("position", 0);
    }

    private void initData() {
        utils = new Utils();
        // 注册电量广播
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(myReceiver, intentFilter);

        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                Toast.makeText(SystemVideoPlayer.this, "长按方法", Toast.LENGTH_SHORT).show();
                startAndPause();
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Toast.makeText(SystemVideoPlayer.this, "双击方法", Toast.LENGTH_SHORT).show();
                setFullScreenAndDefault();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isshowMediaController) {
                    // 隐藏
                    hideMediaController();
                    // 把隐藏消息移除
                    MyHandler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    // 显示控制面板
                    showMediaController();
                    // 发送隐藏控制面板
                    MyHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                }

                Toast.makeText(SystemVideoPlayer.this, "点击方法", Toast.LENGTH_SHORT).show();
                return super.onSingleTapConfirmed(e);
            }
        });

        // 获取系统音量管理器
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        // 获取当前音量
        currentAudio = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 获取最大音量
        maxAudio = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;


    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SystemVideoPlayer Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int level = intent.getIntExtra("level", 0);//0~100;
            // 设置电量
            setBattery(level);
        }

    }


    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private void setData() {
        // 判断传递过来的media数组是否为空
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName());//设置视频的名称
            isNetUri = utils.isNetUri(mediaItem.getData());
            videoView.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            tvName.setText(uri.toString());//设置视频的名称
            isNetUri = utils.isNetUri(uri.toString());
            videoView.setVideoURI(uri);

        } else {
            Toast.makeText(this, "当前没有传递数据", Toast.LENGTH_SHORT).show();
        }

        setButtonState();

     /*   if (uri != null) {
            videoView.setVideoURI(uri);
        }*/
    }

    private void setListener() {
        //准备好的监听
        videoView.setOnPreparedListener(new MyOnPreparedListener());
        //播放出错的监听
        videoView.setOnErrorListener(new MyOnErrorListener());
        //播放完成了的监听
        videoView.setOnCompletionListener(new MyOnCompletionListener());
        // 设置控制面板
        //  videoView.setMediaController(new MediaController(this));
        // 设置播放进度监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        // 设置音量监听
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        if (isUseSystem) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                videoView.setOnInfoListener(new MyOnInfoListener());
            }
        }


    }

    private void initView() {
        videoView = (VideoView) this.findViewById(R.id.video_view);


        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnVideoStartPause = (Button) findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSiwchScreen = (Button) findViewById(R.id.btn_video_siwch_screen);
        mediaController = (RelativeLayout) findViewById(R.id.media_controller);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        tv_laoding_netspeed = (TextView) findViewById(R.id.tv_laoding_netspeed);
        btnSwichPlayer = (Button) findViewById(R.id.btn_swich_player);


        btnVoice.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);
        btnSwichPlayer.setOnClickListener(this);

        // 设置音量关联
        seekbarVoice.setMax(maxAudio);
        seekbarVoice.setProgress(currentAudio);

        //开始更新网络速度
        MyHandler.sendEmptyMessage(SHOW_SPEED);

    }


    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            // 调节声音
            isMute = !isMute;
            updataVoice(currentAudio, isMute);

        } else if (v == btnExit) {
            finish();
        } else if (v == btnVideoPre) {
            // 播放上一个视屏
            playPreVideo();

        } else if (v == btnVideoStartPause) {
            // 播放按钮
            startAndPause();

        } else if (v == btnVideoNext) {
            // 播放下一个视屏
            playNextVideo();

        } else if (v == btnVideoSiwchScreen) {
            // 全屏或默认
            setFullScreenAndDefault();
        } else if (v == btnSwichPlayer) {

            showSwichPlayerDialog();

        }

        MyHandler.removeMessages(HIDE_MEDIACONTROLLER);
        MyHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);

    }

    private void showSwichPlayerDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提醒您");
        builder.setMessage("切换到万能播放器");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();

    }

    /**
     * 全屏或者默认
     */
    private void setFullScreenAndDefault() {

        if (isFullScreen) {
            // 默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            // 全屏
            setVideoType(FULL_SCREEN);
        }
    }

    /**
     * 设置播放器全屏或默认
     */
    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN:  // 全屏

                //1.设置视频画面的大小-屏幕有多大就是多大
                videoView.setVideoSize(screenWidth, screenHeight);
                //2.设置按钮的状态-默认
                // btn_video_siwch_screen
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                isFullScreen = true;

                break;
            case DEFAULT_SCREEN:  // 默认

                //1.设置视频画面的大小
                //视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;


                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }

                // 设置videoView当前宽高
                videoView.setVideoSize(width, height);
                // 设置按钮状态 - 全屏
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                isFullScreen = false;
                break;
        }

    }

    /**
     * 设置音量的大小
     */
    private void updataVoice(int progress, boolean isMute) {

        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);

        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentAudio = progress;
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            // 播放下一个视屏
            position++;
            if (position < mediaItems.size()) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoView.setVideoPath(mediaItem.getData());
                tvName.setText(mediaItem.getName());

                //设置按钮状态
                setButtonState();
            }
        } else if (uri != null) {
            isNetUri = utils.isNetUri(uri.toString());
            //设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();
        }
    }

    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position--;
            if (position >= 0) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoView.setVideoPath(mediaItem.getData());
                tvName.setText(mediaItem.getName());
                //设置按钮状态
                setButtonState();
            }
        } else if (uri != null) {
            isNetUri = utils.isNetUri(uri.toString());
            //设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();


        }

    }

    private void setButtonState() {
        // 判断播放列表中有没有值，没有的话再判断uri中是否有值
        if (mediaItems != null && mediaItems.size() > 0) {
            // 播放按钮有三种状态，1.只有一个资源2.有两个资源3.有三个资源
            if (mediaItems.size() == 1) {
                setEnabled(false);
            } else if (mediaItems.size() == 2) {
                if (position == 0) {

                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);

                } else if (position == (mediaItems.size() - 1)) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                }

            } else if (mediaItems.size() == 3) {
                // 做第一个和最后一个判断
                if (position == 0) {

                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                } else if (position == (mediaItems.size() - 1)) {

                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnabled(true);
                }

            }

        } else if (uri != null) {
            // 设置buttom激活状态
            setEnabled(false);
        }
    }

    private void setEnabled(boolean isEnabled) {
        if (isEnabled) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }

    }

    private void startAndPause() {
        // 判断是否在播放状态
        if (videoView.isPlaying()) {
            // 视屏暂停
            videoView.pause();
            // 按钮状态设置播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            // 更新进度
            MyHandler.sendEmptyMessage(PROGRESS);
            // 视频播放
            videoView.start();
            // 按钮状态设置暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }

    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            // 隐藏控制面板
            hideMediaController();

            //开始播放
            videoView.start();
            int duration = videoView.getDuration();
            seekbarVideo.setMax(duration);
            tvDuration.setText(utils.stringForTime(duration));
            //2.发消息
            MyHandler.sendEmptyMessage(PROGRESS);

            //把加载页面消失掉
            ll_loading.setVisibility(View.GONE);


        }
    }


    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            //1.播放的视频格式不支持--跳转到万能播放器继续播放
            startVitamioPlayer();

            return true;
        }
    }

    /**
     * a,把数据按照原样传入VtaimoVideoPlayer播放器
     * b,关闭系统播放器
     */
    private void startVitamioPlayer() {

        if (videoView != null) {
            videoView.stopPlayback();
        }

        Intent intent = new Intent(SystemVideoPlayer.this, VitamioVideoPlayer.class);
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtra("position", position);
            intent.putExtras(bundle);
        } else if (uri != null) {
            intent.setData(uri);
        }

        startActivity(intent);
        finish(); // 关闭页面

    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {

        /*    //关闭网络速度更新
            MyHandler.removeMessages(SHOW_SPEED);*/

        }
    }

    private class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当手指滑动的时候，会引起SeekBar进度变化，会回调这个方法
         *
         * @param seekBar
         * @param progress
         * @param fromUser 如果是用户引起的true,不是用户引起的false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (fromUser) {
                videoView.seekTo(progress);
            }
        }

        /**
         * 当手指触碰的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            MyHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /**
         * 当手指离开的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MyHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);

        }
    }

    private class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            // LogUtil.i("LogUtil progress = " + progress);
            // 判断当前是否手动
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updataVoice(progress, isMute);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            MyHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MyHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    ll_buffer.setVisibility(View.VISIBLE);//视频卡了，拖动卡
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END: //视频卡结束了，拖动卡结束了
                    ll_buffer.setVisibility(View.GONE);
                    break;

            }

            return false;
        }
    }

    @Override
    protected void onDestroy() {
        //移除所有的消息
        MyHandler.removeCallbacksAndMessages(null);

        //释放资源的时候，先释放子类，再释放父类
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }

        super.onDestroy();
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    public String getTvSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }


    private float startY;
    private float startX;
    private float endY;
    /**
     * 当一按下的音量
     */
    private int mVol;
    /**
     * 屏幕的高
     */
    private float touchRang;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //把事件传递给手势识别器
        detector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  // 手指按下
                //1.按下记录值
                startY = event.getY();
                startX = event.getX();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenWidth, screenHeight);
                MyHandler.removeMessages(HIDE_MEDIACONTROLLER);

                break;
            case MotionEvent.ACTION_MOVE:  // 手指移动

                //2.移动的记录相关值
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = startY - endY;
                if (endX < screenWidth / 2) {
                    // 左边屏幕调节亮度
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        Log.e(TAG, "up");
                        setBrightness(20);
                    }
                    if (distanceY < FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        Log.e(TAG, "down");
                        setBrightness(-20);
                    }

                } else {
                    //改变声音 = （滑动屏幕的距离： 总距离）*音量最大值
                    float delta = (distanceY / touchRang) * maxAudio;
                    //最终声音 = 原来的 + 改变声音；
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), maxAudio);
                    if (delta != 0) {
                        isMute = false;
                        updataVoice(voice, isMute);
                    }
                }


                break;
            case MotionEvent.ACTION_UP:  // 手指离开
                MyHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;
        }

        return super.onTouchEvent(event);
    }

    /*
   *
   * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
   */
    private  Vibrator vibrator;
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // if (lp.screenBrightness <= 0.1) {
        // return;
        // }
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        }
//        Log.e(TAG, "lp.screenBrightness= " + lp.screenBrightness);
        getWindow().setAttributes(lp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentAudio--;
            updataVoice(currentAudio, false);
            MyHandler.removeMessages(HIDE_MEDIACONTROLLER);
            MyHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            // 返回true 当前事件被拦截
            return true;


        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentAudio++;
            updataVoice(currentAudio, false);
            MyHandler.removeMessages(HIDE_MEDIACONTROLLER);
            MyHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;

        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 显示控制面板
     */
    private void showMediaController() {
        mediaController.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        mediaController.setVisibility(View.GONE);
        isshowMediaController = false;
    }
}
