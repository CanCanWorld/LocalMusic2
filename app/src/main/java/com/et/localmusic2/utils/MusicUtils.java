package com.et.localmusic2.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import com.et.localmusic2.bean.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicUtils {

    @SuppressLint("Range")
    public static List<Song> getMusicData(Context context) {
        List<Song> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.setSong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                song.setSinger(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                song.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                song.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                song.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                song.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                song.setCheck(false);
                if (song.getSize() > 800 * 1024) {
                    list.add(song);
                }
            }
            cursor.close();
        }
        return list;
    }

    public static Bitmap getAlbumPic(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap albumPic = null;
        if (data != null) {
            albumPic = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return albumPic;
    }
}
