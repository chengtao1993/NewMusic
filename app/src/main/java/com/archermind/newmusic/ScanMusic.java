package com.archermind.newmusic;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by n009654 on 2018/1/18.
 */

public class ScanMusic {
    //获得专辑封面的uri
    private static  final Uri albumArtUri=Uri.parse("content://media/external/audio/albumart");
    public static ArrayList<MusicBean> data = new ArrayList<MusicBean>();
    private static Cursor cursor;


    public static ArrayList getData(Context context,String path){
        data.clear();
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            Log.d("hct","cursor.getcount = "+cursor.getCount());
            while (cursor.moveToNext()){
                MusicBean musicBean = new MusicBean();
                musicBean.setText_song(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                musicBean.setText_singer(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                musicBean.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                musicBean.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                musicBean.setDisplay_name(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                musicBean.setAlbumID(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                musicBean.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                musicBean.setUri(null);
                if(path.equals("external")){
                    Log.d("hct","musicBean.getPath() = "+musicBean.getPath());
                    if(musicBean.getPath().contains("storage/emulated")){
                        Log.d("hct","进来了");
                        data.add(musicBean);
                    }
                }
            }
            cursor.close();
        }catch (Exception e){
            Log.d("hct","出现了异常"+e);
        }

        return data;
    }


    //获得默认专辑的图片
    @SuppressWarnings("ResourceType")
    public static Bitmap getDefaultArtwork(Context context, boolean small){
        Options opts=new Options();
        opts.inPreferredConfig=Bitmap.Config.RGB_565;
        if (small){//返回小图片
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.default_image),null,opts);
        }
        return  BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.default_image),null,opts);
    }




    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid){
        Bitmap bm = null;
        if(albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            Options options = new Options();
            FileDescriptor fd = null;
            if(albumid < 0){
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFileDescriptor(fd, null, options);
            options.inSampleSize = 100;
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 获取专辑图片
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefalut
     * @return
     */
    public  static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small){
        if(album_id < 0) {
            if(song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if(bm != null) {
                    return bm;
                }
            }
            if(allowdefalut) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if(uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                Options options = new Options();

                options.inSampleSize = 1;

                options.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(in, null, options);

                if(small){
                    options.inSampleSize = computeSampleSize(options, 40);
                } else{
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if(bm != null) {
                    if(bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if(bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if(allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if(in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 缩放
     * @param options
     * @param target
     * @return
     */
    public static int computeSampleSize(Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if(candidate == 0) {
            return 1;
        }
        if(candidate > 1) {
            if((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if(candidate > 1) {
            if((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }
}
