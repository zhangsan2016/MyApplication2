package com.example.myplayer.act;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplayer.IMusicPlayerService;
import com.example.myplayer.R;
import com.example.myplayer.act.View.BaseVisualizerView;
import com.example.myplayer.act.View.ShowLyricView;
import com.example.myplayer.act.domain.MediaItem;
import com.example.myplayer.act.service.MusicPlayerService;
import com.example.myplayer.act.utils.LogUtil;
import com.example.myplayer.act.utils.LyricUtils;
import com.example.myplayer.act.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.x;

import java.io.File;

/**
 * Created by ldgd on 2017/2/11.
 * 介绍：音乐播放界面
 */

public class AudioPlayerActivity extends Activity implements View.OnClickListener {
    private static final int PROGRESS = 1;
    private static final int SHOW_LYRIC = 2;
/*    @ViewInject(R.id.iv_icon)
    private ImageView ivIcon;*/

    private int position = 0;
    /**
     * 服务的代理类，通过它可以调用服务的方法
     */
    private IMusicPlayerService service;

    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private ShowLyricView showLyricView;
    private BaseVisualizerView baseVisualizerView;

    private MyReceiver receiver;


    /**
     * true:从状态栏进入的，不需要重新播放
     * false:从播放列表进入的
     */
    private boolean notification;

    /**
     * 工具类
     */
    private Utils utils;

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case PROGRESS:
                    try {

                        // 设置进度
                        seekbarAudio.setProgress(service.getCurrentPosition());
                        // 设置时间
                        tvTime.setText(utils.stringForTime(service.getCurrentPosition()) + "/" + utils.stringForTime(service.getDuration()));

                        // 每秒更新一次
                        myHandler.removeMessages(PROGRESS);
                        myHandler.sendEmptyMessageDelayed(PROGRESS, 1000);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                case SHOW_LYRIC: //显示歌词
                    try {
                        //1.得到当前的进度
                        int currentPosition = service.getCurrentPosition();
                        //2.把进度传入ShowLyricView控件，并且计算该高亮哪一句
                        showLyricView.setshowNextLyric(currentPosition);


                        myHandler.removeMessages(SHOW_LYRIC);
                        myHandler.sendEmptyMessage(SHOW_LYRIC);


                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audioplayer);

        getData();
        initData();
        initView();

        bindAndStartService();

    }

    private void initData() {
        utils = new Utils();

        //1.EventBus注册
        EventBus.getDefault().register(this);

        //注册广播
/*        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerService.OPENAUDIO);
        registerReceiver(receiver, intentFilter);*/

    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            showData(null);
        }
    }

    //3.订阅方法
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 0)
    public void showData(MediaItem mediaItem) {

        //发消息开始歌词同步
        showLyric();
        showViewData();
        checkPlaymode();
        setupVisualizerFxAndUi();  // 设置跳动视觉效果
    }

    /**
     * 发消息开始歌词同步
     */
    private void showLyric() {

        // 解析歌词
        LyricUtils lyricUtils = new LyricUtils();

        try {
            // 获取歌词绝对路径
            String path = service.getAudioPath();
            path = path.substring(0, path.lastIndexOf("."));
            //传歌词文件
            //mnt/sdcard/audio/beijingbeijing.mp3
            //mnt/sdcard/audio/beijingbeijing.lrc
            File file = new File(path + ".lrc");
            if (!file.exists()) {
                file = new File(path + ".txt");
            }
            // 解析歌词
            lyricUtils.readLyricFile(file);
            // 得到解析好的歌词列表
            showLyricView.setLyrics(lyricUtils.getLyrics());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (lyricUtils.isExistsLyric()) {
            myHandler.sendEmptyMessage(SHOW_LYRIC);
        }


    }

    private ServiceConnection con = new ServiceConnection() {
        /**
         * 当连接成功的时候回调这个方法
         * @param name
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {

            service = IMusicPlayerService.Stub.asInterface(iBinder);

            if (service != null) {
                try {
                    if (!notification) { //从列表
                        //播放
                        service.openAudio(position);
                    } else {  // 从状态栏
                        showViewData();
                    }

                    LogUtil.e("ServiceConnection 綁定服務成功");


                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当断开连接的时候回调这个方法
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (service != null) {
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }
    };

    private class myOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    //拖动进度
                    service.seekTo(progress);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    private void showViewData() {

        try {
            // 当前歌曲信息
            tvArtist.setText(service.getArtist());
            tvName.setText(service.getName());

            // 设置最大进度
            int maxDuration = service.getDuration();
            seekbarAudio.setMax(maxDuration);

            //发消息
            myHandler.sendEmptyMessage(PROGRESS);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void getData() {
        notification = getIntent().getBooleanExtra("notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    private void initView() {
        x.view().inject(AudioPlayerActivity.this);

        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        // 设置图片动画
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketAnimation = (AnimationDrawable) ivIcon.getBackground();
        rocketAnimation.start();

        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        seekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnLyrc = (Button) findViewById(R.id.btn_lyrc);
        showLyricView = (ShowLyricView) findViewById(R.id.showLyricView);
        baseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);

        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);

        //设置视频的拖动
        seekbarAudio.setOnSeekBarChangeListener(new myOnSeekBarChangeListener());


    }

    private void bindAndStartService() {

        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.OPENAUDIO);
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent); //不至于实例化多个服务
    }


    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {

            // 设置播放模式
            setPlaymode();
        } else if (v == btnAudioPre) {
            if (service != null) {
                try {
                    service.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioStartPause) {

            if (service != null) {
                try {
                    if (service.isPlaying()) {
                        // 暂停
                        service.pause();
                        // 按钮 - 播放
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);

                    } else {
                        // 播放
                        service.start();
                        // 按钮 - 停止
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } else if (v == btnAudioNext) {

            if (service != null) {
                try {
                    service.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnLyrc) {
            // Handle clicks for btnLyrc
        }
    }

    /**
     * 设置播放模式
     */
    private void setPlaymode() {

        try {
            int playMode = service.getPlayMode();

            if (playMode == MusicPlayerService.REPEAT_NORMAL) {  // 顺序播放
                playMode = MusicPlayerService.REPEAT_SINGLE;
            } else if (playMode == MusicPlayerService.REPEAT_SINGLE) {  // 单曲循环
                playMode = MusicPlayerService.REPEAT_ALL;
            } else if (playMode == MusicPlayerService.REPEAT_ALL) {  // 全部循环
                playMode = MusicPlayerService.REPEAT_NORMAL;
            } else {
                playMode = MusicPlayerService.REPEAT_NORMAL;
            }

            // 同步sercice模式
            service.setPlayMode(playMode);

            //设置图片
            showPlaymode();


        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    private void showPlaymode() {

        try {
            int playMode = service.getPlayMode();
            if (playMode == MusicPlayerService.REPEAT_NORMAL) {  // 顺序播放

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();

            } else if (playMode == MusicPlayerService.REPEAT_SINGLE) {  // 单曲循环

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                Toast.makeText(AudioPlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();

            } else if (playMode == MusicPlayerService.REPEAT_ALL) {  // 全部循环

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
                Toast.makeText(AudioPlayerActivity.this, "全部循环", Toast.LENGTH_SHORT).show();

            } else { // 顺序播放

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();

            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 校验状态
     */
    private void checkPlaymode() {

        try {
            int playMode = service.getPlayMode();

            if (playMode == MusicPlayerService.REPEAT_NORMAL) {  // 顺序播放

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);

            } else if (playMode == MusicPlayerService.REPEAT_SINGLE) {  // 单曲循环

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);

            } else if (playMode == MusicPlayerService.REPEAT_ALL) {  // 全部循环

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);

            } else { // 顺序播放

                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);

            }

            // 校验播放暂停
            if(service.isPlaying()){
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
            }else{
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private  Visualizer mVisualizer;
    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi()
    {

        try {
            int audioSessionid = service.getAudioSessionId();
            System.out.println("audioSessionid=="+audioSessionid);
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            baseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onDestroy() {
        //2.EventBus取消注册
        EventBus.getDefault().unregister(this);

        //取消注册广播
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        //解绑服务
        if (con != null) {
            unbindService(con);
            con = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mVisualizer != null){
            mVisualizer.release();
        }
    }
}
