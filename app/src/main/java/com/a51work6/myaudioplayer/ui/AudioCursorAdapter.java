package com.a51work6.myaudioplayer.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.a51work6.myaudioplayer.R;
//自定义游标适配器
public class AudioCursorAdapter extends CursorAdapter {
    private int layout;
    private LayoutInflater inflater;
//初始化游标适配器
    public AudioCursorAdapter(Context context, int layout) {
        super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);//注册内容监听器，监听游标内容变化
        inflater = LayoutInflater.from(context);
        this.layout = layout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {//获取要显示视图的内容，将游标的数据绑定到列表项
        // 设置
        String title = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.TITLE));
        TextView titletview = (TextView) view.findViewById(R.id.title);
        titletview.setText(title);
        // 设置
        String artist = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.ARTIST));
        TextView artistview = (TextView) view.findViewById(R.id.artist);
        artistview.setText("演唱者：" + artist);
        // 设置
        long duration = cursor.getLong(cursor
                .getColumnIndex(MediaStore.Audio.Media.DURATION));
        String time = Util.timeToString(duration);
        TextView durationview = (TextView) view.findViewById(R.id.duration);
        durationview.setText("时长：" + time);
    }

    //创建一个列表项
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = inflater.inflate(layout, parent, false);
        return view;

    }
}
