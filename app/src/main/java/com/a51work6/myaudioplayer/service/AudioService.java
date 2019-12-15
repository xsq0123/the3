package com.a51work6.myaudioplayer.service;
import android.app.Notification.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
//LocalBroadcastManager;
import com.a51work6.myaudioplayer.R;
import com.a51work6.myaudioplayer.ui.AudioPlayerActivity;

public class AudioService extends Service {
    private Cursor mCursor;
    private MediaPlayer mMediaPlayer;
    private int mState = STOP;
    public static final int PLAYING = 0;
    public static final int PAUSE = 1;
    public static final int STOP = 2;
    private NotificationManager mNotificationManager; // 通知管理器
    private int notificationRef = 123;// 通知引用id
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        // 从Content Provider中读取音乐列表,从SD卡获取音频
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//启动服务器
        int position = intent.getIntExtra("position", -1);
        if (position != -1) {
            mCursor.moveToPosition(position);
            play();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.release();
        mState = STOP;
        // 清除通知
        mNotificationManager.cancel(notificationRef);
    }

    // 预处理监听器OnPreparedListener
    // MediaPlayer进入prepared状态开始播放
    private OnPreparedListener mPreparedListener = new OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mMediaPlayer.start();
            mState = PLAYING;
            sendNotification();
        }

    };

    // 播放结束监听器
    // 当前歌曲播放结束后，播放下一首歌曲
    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mState = STOP;
            if (!mCursor.moveToNext())
                mCursor.moveToFirst();
            play();
        }
    };

    //播放方法
    private void play() {
        String path = mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        try {
            if (mMediaPlayer == null) {
                // 创建MediaPlayer对象并设置Listener
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnPreparedListener(mPreparedListener);
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
            } else {
                // 复用MediaPlayer对象
                mMediaPlayer.reset();
            }
            mMediaPlayer.setDataSource(path);//设置本地路径
            mMediaPlayer.prepare();//预处理

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 发出通知
    private void sendNotification() {

        String title = mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

        String message = mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                + "\n"
                + mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

        Intent intent = new Intent(this, AudioPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

//        Notification notification = new Notification.Builder(this)
//                .setSmallIcon(R.mipmap.stat_notify_musicplayer)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setContentIntent(pendingIntent)
//                .build();
//
//
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        // 通知用户，歌曲已经开始播放
//        mNotificationManager.notify(notificationRef, notification);
    }

    public void next() {
        mState = STOP;
        if (!mCursor.moveToNext())
            mCursor.moveToFirst();
        play();
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mState = PAUSE;
        }
    }

    public void previous() {
        mState = STOP;
        if (!mCursor.moveToPrevious())
            mCursor.moveToLast();
        play();
    }

    public void release() {
        mMediaPlayer.release();
    }

    public void seek(int time) {
        int media = mMediaPlayer.getDuration() * time / 1000;
        mMediaPlayer.seekTo(media);
    }

    public void start() {
        if (mState == STOP) {
            play();
        } else if (mState == PAUSE) {
            mMediaPlayer.start();
            mState = PLAYING;
        }
    }

    public void stop() {
        mMediaPlayer.stop();
        mState = STOP;
        // 取消Notification
        mNotificationManager.cancel(notificationRef);
    }

    public Song getSong() {

        Song song = new Song();
        String album = mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        song.setAlbum(album);
        String artist = mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        song.setArtist(artist);
        song.setDuration(mMediaPlayer.getDuration());
        song.setCurrentPosition(mMediaPlayer.getCurrentPosition());
        String songname = mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        song.setTitle(songname);
        song.setState(mState);

        return song;
    }


    public Uri getAlbumImage() {
        // 重新获得专辑图片
        long album_id = mCursor.getLong(mCursor
                .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        //MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        return uri;
    }

}