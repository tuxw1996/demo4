package com.gx.me.demo4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gx.me.demo4.model.Const;
import com.gx.me.demo4.model.Msg;
import com.gx.me.demo4.model.ServiceEvent;
import com.gx.me.demo4.utils.Tool;

import org.greenrobot.eventbus.EventBus;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlaybackActivity extends AppCompatActivity {
    private CircleImageView novelImgView = null;
    private RotateAnimation m_Animation = null;
    private TextView m_PlaybackStatus = null;
    private Button btn_start = null;
    boolean m_hasStartedAnimation = false;
    private SeekBar m_Progress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Const.PACKBACKUPDATEACTION);
        registerReceiver(m_Receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(m_Receiver);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        btn_start = (Button) findViewById(R.id.start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ServiceEvent(Msg.PLAY));
            }
        });

        novelImgView = (CircleImageView) findViewById(R.id.poster);//转动的图片
        m_PlaybackStatus = (TextView) findViewById(R.id.status_tx);//音频播放时间
        m_Progress = (SeekBar) findViewById(R.id.seekbar);//音频进度条
        m_Progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 只有是自己拽的才发msg给service去seekto
                if (fromUser) {
                    EventBus.getDefault().post(new ServiceEvent(Msg.SEEKTO, progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        String poster = getIntent().getStringExtra("novel_poster");
        Tool.getImageCache(PlaybackActivity.this).get(poster, novelImgView);
        // 定义一个旋转的动效
        m_Animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //m_Animation = new RotateAnimation(0, 360);
        //匀速转动
        LinearInterpolator lin = new LinearInterpolator();
        m_Animation.setInterpolator(lin);
        m_Animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        m_Animation.setDuration(1000 * 3);//设置动画持续时间
        m_Animation.setRepeatCount(-1);//设置重复次数
        m_Animation.setRepeatMode(Animation.RESTART); //Animation.REVERSE

    }

    public void updateUi(int status, int duration, int pos) {
        if (status == PlaybackService.STA_PREPARING) {
            btn_start.setBackgroundResource(R.mipmap.start);
            if (m_hasStartedAnimation == true) {
                novelImgView.clearAnimation();
                m_hasStartedAnimation = false;
            }
        } else if (status == PlaybackService.STA_PAUSED) {
            btn_start.setBackgroundResource(R.mipmap.start);
            m_Progress.setMax(duration);
            m_Progress.setProgress(pos);
            if (m_hasStartedAnimation == true) {
                novelImgView.clearAnimation();
                m_hasStartedAnimation = false;
            }
        } else if (status == PlaybackService.STA_STARTED) {
            btn_start.setBackgroundResource(R.mipmap.pause);
            m_Progress.setMax(duration);
            m_Progress.setProgress(pos);
            if (m_hasStartedAnimation == false) {
                novelImgView.startAnimation(m_Animation);
                m_hasStartedAnimation = true;
            }
        }

        //当前播放的音频时间
        int pos_m = (pos / 1000) / 60;//分
        int pos_s = (pos / 1000) % 60;//秒

        int duration_m = (duration / 1000) / 60;//分
        int duration_s = (duration / 1000) % 60;//秒

        m_PlaybackStatus.setText(String.format("%02d:%02d / %02d:%02d", pos_m, pos_s, duration_m, duration_s));
    }

    //广播类
    BroadcastReceiver m_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("status", -1);
            updateUi(status, intent.getIntExtra("duration", 0), intent.getIntExtra("current_position", 0));
        }
    };

}
