package com.gx.me.demo4;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.SparseArray;

import com.gx.me.demo4.model.BaseEvent;
import com.gx.me.demo4.model.Const;
import com.gx.me.demo4.model.IHandler;
import com.gx.me.demo4.model.Msg;
import com.gx.me.demo4.model.ServiceEvent;
import com.gx.me.demo4.utils.HandlerMapping;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlaybackService extends Service implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener{

    public PlaybackService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        m_MediaPlayer = new MediaPlayer();
        m_MediaPlayer.setOnErrorListener(this);
        m_MediaPlayer.setOnCompletionListener(this);
        m_MediaPlayer.setOnPreparedListener(this);
        //注册EventBus
        EventBus.getDefault().register(this);
        addFunc();

        //开启一个记时任务
        m_UpdateStatusTimer = new Timer();
        m_UpdateStatusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (m_PlaybackStatus == STA_PAUSED || m_PlaybackStatus == STA_STARTED) {
                    Intent it = new Intent();
                    it.setAction(Const.PACKBACKUPDATEACTION);
                    it.putExtra("status", m_PlaybackStatus);//播放状态
                    it.putExtra("duration", m_MediaPlayer.getDuration());//总时间
                    it.putExtra("current_position", m_MediaPlayer.getCurrentPosition());//当前时间
                    sendBroadcast(it);//发广播
                }else if (m_PlaybackStatus == STA_PREPARING) {//准备
                    Intent it = new Intent();
                    it.setAction(Const.PACKBACKUPDATEACTION);
                    it.putExtra("status", m_PlaybackStatus);//播放状态
                    it.putExtra("duration", 0);//总时间
                    it.putExtra("current_position", 0);//当前时间
                    sendBroadcast(it);//发广播
                }
            }
        }, 3000, 1000);
    }

    @Override
    public void onDestroy () {
        m_UpdateStatusTimer.cancel();//销毁记时器
        m_MediaPlayer.release();//释放多媒体
        EventBus.getDefault().unregister(this);//取消广播
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        m_PlaybackStatus = STA_ERROR;
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        m_PlaybackStatus = STA_PREPARED;
        start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO  播放下一个 这是作业。
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent (ServiceEvent event) {
        //该方法会接收广播的消息
        m_Hm.exeFunc(event);
    }

    /**
     * 根据播放的状态操作音频文件
     */
    public void addFunc () {
        m_Hm.addFunc(Msg.PLAY, new IHandler() {
            @Override
            public int handleFunc(BaseEvent event) {
                if (event.data != null) {
                    preparingUrl((String) event.data);
                }else {
                    if (m_PlaybackStatus == STA_PAUSED || m_PlaybackStatus == STA_PREPARED) {
                        start();
                    }else if (m_PlaybackStatus == STA_STARTED) {
                        pause();
                    }else if (m_PlaybackStatus == STA_ERROR || m_PlaybackStatus == STA_IDLE) {
                        preparingUrl(curr_url);
                    }
                }
                return 0;
            }
        });

        m_Hm.addFunc(Msg.PAUSE, new IHandler() {
            @Override
            public int handleFunc(BaseEvent event) {
                if (canPause()) {
                    pause();
                }
                return 0;
            }
        });

        m_Hm.addFunc(Msg.SEEKTO, new IHandler() {
            @Override
            public int handleFunc(BaseEvent event) {
                if (canSeekto()) {
                    seekTo((Integer) event.data);
                }
                return 0;
            }
        });

        m_Hm.addFunc(Msg.PLAY_LOCAL, new IHandler() {
            @Override
            public int handleFunc(BaseEvent event) {
                Uri uri = Uri.parse((String) event.data);
                preparingUri(uri);
                return 0;
            }
        });
    }

    public void preparingUrl(String url) {
        try {
            m_MediaPlayer.reset();
            m_PlaybackStatus = STA_IDLE;

            m_MediaPlayer.setDataSource(url);
            m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            m_MediaPlayer.prepareAsync();
            m_PlaybackStatus = STA_PREPARING;
            curr_url = url;
        } catch (IOException e) {
            m_PlaybackStatus = STA_ERROR;
        }
    }

    public void preparingUri (Uri uri){
        try {
            m_MediaPlayer.reset();
            m_PlaybackStatus = STA_IDLE;

            m_MediaPlayer.setDataSource(PlaybackService.this, uri);
            m_MediaPlayer.prepareAsync();
            m_PlaybackStatus = STA_PREPARING;
        }catch (IOException e) {
            e.printStackTrace();
            m_PlaybackStatus = STA_ERROR;
        }
    }

    public void start() {
        if (canStart()) {
            m_MediaPlayer.start();
            m_PlaybackStatus = STA_STARTED;
        }
    }

    public void pause() {
        if (canPause()) {
            m_MediaPlayer.pause();
            m_PlaybackStatus = STA_PAUSED;
        }
    }

    public void seekTo (int seekTo) {
        if (canSeekto()) {
            m_MediaPlayer.seekTo(seekTo);
        }
    }

    public boolean canStart() {
        return m_PlaybackStatus == STA_PREPARED
                || m_PlaybackStatus == STA_PAUSED;
    }

    public boolean canPause () {
        return m_PlaybackStatus == STA_PREPARED
                || m_PlaybackStatus == STA_STARTED;
    }

    public boolean canSeekto () {

        return m_PlaybackStatus == STA_PREPARED
                || m_PlaybackStatus == STA_STARTED
                || m_PlaybackStatus == STA_PAUSED;
    }

    MediaPlayer m_MediaPlayer;
    int m_PlaybackStatus = STA_IDLE;

    final static int STA_IDLE = 0;
    final static int STA_PREPARED = 1;
    final static int STA_PAUSED = 2;
    final static int STA_PREPARING = 3;
    final static int STA_STARTED = 4;
    final static int STA_ERROR = 5;
    //final static int STA_COMPLETED = 6;
    String curr_url = "";
    HandlerMapping m_Hm = new HandlerMapping(new SparseArray<IHandler>());
    Timer m_UpdateStatusTimer;
}