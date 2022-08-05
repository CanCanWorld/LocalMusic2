package com.et.localmusic2.utils;

public class Constants {
    public static final String DATABASE_NAME = "db_song";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "Song";
    public static final String ID = "id";
    public static final String SONG_NAME = "song_name";
    public static final String SINGER = "singer";
    public static final String ALBUM = "album";
    public static final String PATH = "path";
    public static final String DURATION = "duration";
    public static final String SIZE = "size";
    public static final String IS_CHECK = "is_check";
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SONG_NAME + " TEXT,"
            + SINGER + " TEXT,"
            + ALBUM + " TEXT,"
            + PATH + " TEXT,"
            + DURATION + " INTEGER,"
            + SIZE + " INTEGER"
            + ")";

    /**
     * 歌曲播放
     */
    public static final String PLAY = "play";
    /**
     * 歌曲暂停
     */
    public static final String PAUSE = "pause";
    /**
     * 上一曲
     */
    public static final String PREV = "prev";
    /**
     * 下一曲
     */
    public static final String NEXT = "next";
    /**
     * 关闭通知栏
     */
    public static final String CLOSE = "close";
    /**
     * 进度变化
     */
    public static final String PROGRESS = "progress";

}
