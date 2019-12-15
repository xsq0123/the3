package com.a51work6.myaudioplayer.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.a51work6.myaudioplayer.R;

//音频列表活动
public class AudioListActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private CursorAdapter mCursorAdapter;//游标适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建AudioCursorAdapter游标适配器对象
        mCursorAdapter = new AudioCursorAdapter(this, R.layout.songs_list);

        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener(this);
        LoaderManager loaderManager = getLoaderManager();//从活动中获得LoaderManager对象
        loaderManager.initLoader(0, null, this);//LoaderManager初始化

    }

    //创建CursorLoader时调用
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //创建CursorLoader对象
        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    //加载数据完成时调用
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        //采用新的游标与老游标交换，老游标不关闭
        mCursorAdapter.swapCursor(c);
    }

    //CursorLoader对象被重置时调用
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //采用新的游标与老游标交换，老游标不关闭
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("position", position);
        this.startActivity(intent);
    }
}