package com.a51work6.myaudioplayer.ui;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.*;
//LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

import com.a51work6.myaudioplayer.R;
import com.a51work6.myaudioplayer.service.AudioService;

public class AudioPlayerActivity extends AppCompatActivity {

    private ImageButton play;
    private ImageButton stop;
    private ImageButton pre;
    private ImageButton next;
    private SeekBar bar;
    private TextView currentTextView;
    private TextView totalTextView;
    private TextView artistTextView;
    private TextView albumTextView;
    private AudioService mService;
    private boolean mBound = false;
    private boolean isRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        // 播放按钮
        play = (ImageButton) findViewById(R.id.play);
        play.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (mService.getSong().getState() == AudioService.PLAYING) {
                    mService.pause();
                    play.setImageResource(R.mipmap.pause);
                } else {
                    mService.start();
                    play.setImageResource(R.mipmap.play);
                }
            }
        });

        // 停止按钮
        stop = (ImageButton) findViewById(R.id.stop);
        stop.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mService.stop();
                bar.setProgress(0);
                play.setImageResource(R.mipmap.play);

            }
        });
        // 上一首按钮
        pre = (ImageButton) findViewById(R.id.pre);
        pre.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mService.previous();
            }
        });
        // 下一首按钮
        next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mService.next();
            }
        });
        // 设置进度栏
        bar = (SeekBar) findViewById(R.id.progress);
        bar.setMax(1000);
        bar.setProgress(0);
        bar.setOnSeekBarChangeListener(seekListener);//

        currentTextView = (TextView) findViewById(R.id.current);
        totalTextView = (TextView) findViewById(R.id.total);

        artistTextView = (TextView) findViewById(R.id.artist);
        albumTextView = (TextView) findViewById(R.id.album);

        Intent intent1 = getIntent();
        int position = intent1.getIntExtra("position", -1);

        if (mService == null) {//启动和绑定audioservice服务
            Intent intent = new Intent(this, AudioService.class);//创建启动audioservice意图
            intent.putExtra("position", position);//
            startService(intent);//启动服务
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);//绑定服务
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除绑定BinderService
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        handler.removeMessages(0);
        // 停止UI更新线程
        isRunning = false;
    }

    //进度栏事件监听器
    private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            if (fromTouch) {
                mService.seek(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    };

    //服务连接
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // 强制类型转换IBinder→BinderService
            AudioService.LocalBinder binder = (AudioService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            // 更新播放屏
            isRunning = true;
            thread.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    //线程负责100毫秒发送一次消息
    private Thread thread = new Thread() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    sleep(100);
                    handler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //消息处理器
    private Handler handler = new Handler() {
        //更新主线程的UI界面，100毫秒刷新一次
        @Override
        public void handleMessage(Message msg) {

            if (mService.getSong().getState() == AudioService.PLAYING) {
                //歌曲的总长度
                long duration = mService.getSong().getDuration();
                //歌曲的当前时间
                long pos = mService.getSong().getCurrentPosition();
                //更新进度栏
                bar.setProgress((int) (1000 * pos / duration));

                currentTextView.setText(Util.timeToString(pos));
                totalTextView.setText(Util.timeToString(duration));
                artistTextView
                        .setText(mService.getSong().getArtist());
                albumTextView.setText(mService.getSong().getAlbum());
                setTitle(mService.getSong().getTitle());
                //更新播放按钮图标→暂停图标
                play.setImageResource(R.mipmap.pause);
            } else if (mService.getSong().getState() == AudioService.PAUSE) {
                //更新暂停按钮→按钮图标
                play.setImageResource(R.mipmap.play);
            }
            //调用该方法更新专辑图片
            updateAlbumIamge();
        }
    };

    //更新专辑图片
    private void updateAlbumIamge() {
        ImageView albumIamge = (ImageView) findViewById(R.id.album_iamge);
        try {
            //从歌曲中获得专辑图片URI
            Uri uri = mService.getAlbumImage();
            if (uri == null) {
                return;
            }
            //获得当前的内容解析器
            ContentResolver resolver = getContentResolver();
            //获得输出流
            InputStream in = resolver.openInputStream(uri);
            //获得位图图片
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            //设置专辑图片到界面上
            albumIamge.setImageBitmap(bitmap);
        } catch (Exception e) {
            //没有专辑图片情况，界面还是使用默认图片
            albumIamge.setImageResource(R.mipmap.disc1);
        }
    }
}

