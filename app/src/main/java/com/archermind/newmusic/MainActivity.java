package com.archermind.newmusic;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private static final String STRING_NULL = "";
    public static String current_source_path = "external";
    private ImageButton music_next;
    private ImageButton music_pre;
    public static ImageButton music_play;
    public static SeekBar seekBar;
    public static TextView musictotal;
    public static TextView current_time;
    public static ArrayList<MusicBean> beans;
    public static TextView song;
    private ImageButton setting;
    private ImageButton playMode;
    public TextView data_source;
    private ImageButton source_switch;
    private ListView music_lv;
    public static MusicAdapter musicAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private int[] layouts = new int[]{R.layout.all_list,R.layout.favorite_list};
    public ArrayList<View> view_list = new ArrayList<>();
    private static final int ALL_MUSIC_PAGE = 0;
    private static LrcView music_words;
    private static int currentTime;
    private static List<LrcContent> lrcList;
    private static LrcProcess mlrcProcess;
    private static ImageView song_pic;
    private static Context mContext;
    private static TextView songer;
    public Intent outIntent = new Intent();
    private boolean tag1 = false;
    public static boolean tag2 = false;
    public static MusicBean bean;
    public static MusicService musicService;
    public static int[] images={R.drawable.play_mode1,R.drawable.play_mode2,R.drawable.play_mode3};
    public static SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    public static int currentNumber=0;
    public static String current_source_name = "U盘";
    public static int modeNumber=0;


    //  通过 Handler 更新 UI 上的组件状态
    public static Handler handler = new Handler();
    public  static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setMax(musicService.mediaPlayer.getDuration());
            if (bean != null) {
                song.setText(bean.getText_song());
                songer.setText(bean.getText_singer());
                song_pic.setImageBitmap(ScanMusic.getArtwork(mContext,bean.getId(),bean.getAlbumID(),true,false));
                current_time.setVisibility(View.VISIBLE);
                musictotal.setVisibility(View.VISIBLE);

            }else {
                song.setText(STRING_NULL);
                songer.setText(STRING_NULL);
                song_pic.setImageBitmap(ScanMusic.getDefaultArtwork(mContext,false));
                current_time.setVisibility(View.INVISIBLE);
                musictotal.setVisibility(View.INVISIBLE);
            }
            current_time.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            musictotal.setText(time.format(musicService.mediaPlayer.getDuration()));
            handler.postDelayed(runnable, 200);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        outIntent.setComponent(new ComponentName("com.archermind.c26poccontrol.tachograph","com.archermind.c26poccontrol.tachograph.MusicAppWidget"));
        for (int i = 0; i <layouts.length ; i++) {
            View v = getLayoutInflater().inflate(layouts[i],null);
            view_list.add(v);
        }
        scanMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(serviceConnection);
        }catch (Exception e){
            Log.d("hct","MainActivity:"+e);
        }
    }

    //  在Activity中调用 bindService 保持与 Service 的通信
    private void bindServiceConnection() {
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        Log.d("hct","绑定服务");
        if (musicService!=null) {
            setdata();
        }else {
            startService(intent);
        }
    }

    //  回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MusicBinder)(service)).getService();
            Log.d("hct","绑定服务成功");
            //更新ＵＩ线程
            setdata();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("hct","绑定服务失败");
            musicService = null;

        }
    };
    private void setdata() {
        musictotal.setText(time.format(musicService.mediaPlayer.getDuration()));
        if (bean != null) {
            song.setText(bean.getText_song());
            songer.setText(bean.getText_singer());
            song_pic.setImageBitmap(ScanMusic.getArtwork(mContext,bean.getId(),bean.getAlbumID(),true,false));
            current_time.setVisibility(View.VISIBLE);
            musictotal.setVisibility(View.VISIBLE);
        }else {
            song.setText(STRING_NULL);
            songer.setText(STRING_NULL);
            song_pic.setImageBitmap(ScanMusic.getDefaultArtwork(mContext,false));
            current_time.setVisibility(View.INVISIBLE);
            musictotal.setVisibility(View.INVISIBLE);
        }
    }


    public static void changeView() {
        bean = beans.get(currentNumber);
        if(musicService==null){
            return;
        }
        musicAdapter.notifyDataSetChanged();
        musicService.lastOrnext();
        musicService.tag = true;
        music_play.setSelected(true);
        musictotal.setText(time.format(musicService.mediaPlayer.getDuration()));
        song.setText(bean.getText_song());
        songer.setText(bean.getText_singer());
        song_pic.setImageBitmap(ScanMusic.getArtwork(mContext,bean.getId(),bean.getAlbumID(),true,false));
        if (tag2 == false) {
            handler.post(runnable);
            tag2 = true;
        }
        initLrc();
    }

    private void initView() {
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(new ViewPagerAdapter(view_list,this));
        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mViewPager,true);
        data_source = findViewById(R.id.data_source);
        data_source.setText(current_source_name);
        source_switch = findViewById(R.id.arrow_down);
        source_switch.setOnClickListener(this);
        data_source.setOnClickListener(this);

        music_words = findViewById(R.id.music_words);
        music_lv = view_list.get(ALL_MUSIC_PAGE).findViewById(R.id.music_lv);
        musicAdapter = new MusicAdapter(this, beans);
        music_lv.setAdapter(musicAdapter);
        music_lv.setOnItemClickListener(this);
        music_next =  findViewById(R.id.music_next);
        music_next.setOnClickListener(this);
        music_pre =  findViewById(R.id.music_pre);
        music_pre.setOnClickListener(this);
        music_play = findViewById(R.id.music_play);
        music_play.setOnClickListener(this);
        if (musicService!=null) {
            if (musicService.mediaPlayer.isPlaying()) {
                musicService.tag = true;
                music_play.setSelected(true);
            } else {
                musicService.tag = false;
                music_play.setSelected(false);
            }
        }
        seekBar = findViewById(R.id.seekBar);
        if(musicService!=null){
            if (tag2 == false) {
                handler.post(runnable);
                tag2 = true;
            }
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true){
                    if(musicService!=null) {
                        musicService.mediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        musictotal = findViewById(R.id.music_time);
        current_time = findViewById(R.id.current_time);

        current_time.setVisibility(View.INVISIBLE);
        musictotal.setVisibility(View.INVISIBLE);
        song =  findViewById(R.id.song);
        songer = findViewById(R.id.songer);
        song_pic = findViewById(R.id.song_pic);
        setting = findViewById(R.id.setting);
        setting.setOnClickListener(this);
        playMode = findViewById(R.id.playMode);
        playMode.setOnClickListener(this);
        Log.d("hct","初始化视图完毕");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentNumber=position;
        changeView();
        outIntent.setAction("MediaPlaybackService.START_ACTION");
        outIntent.putExtra("songName",bean.getText_song());
        outIntent.putExtra("songer",bean.getText_singer());
        mContext.sendBroadcast(outIntent);
    }

    @Override
    public void onClick(View v) {
        if (musicService==null){
            return;
        }
        switch (v.getId()){
            case R.id.music_pre:
                if(bean==null||beans.size()==0){
                    return;
                }
                if (currentNumber>0){
                    currentNumber=currentNumber-1;
                }else {
                    currentNumber=beans.size()-1;
                }
                bean=beans.get(currentNumber);
                changeView();
                outIntent.setAction("MediaPlaybackService.PREV_ACTION");
                outIntent.putExtra("songName",bean.getText_song());
                outIntent.putExtra("songer",bean.getText_singer());
                mContext.sendBroadcast(outIntent);
                break;
            case R.id.music_next:
                if(bean==null||beans.size()==0){
                    return;
                }
                if(currentNumber<beans.size()-1){
                    currentNumber=currentNumber+1;

                }else {
                    currentNumber=0;

                }
                bean=beans.get(currentNumber);
                changeView();
                outIntent.setAction("MediaPlaybackService.NEXT_ACTION");
                outIntent.putExtra("songName",bean.getText_song());
                outIntent.putExtra("songer",bean.getText_singer());
                mContext.sendBroadcast(outIntent);
                break;
            case R.id.music_play:
                if(bean==null||beans.size()==0){
                    return;
                }
                if (music_play.isSelected()) {
                    musicService.tag = false;
                    music_play.setSelected(false);
                    outIntent.setAction("MediaPlaybackService.SUSPEND_ACTION");
                    outIntent.putExtra("songName",bean.getText_song());
                    outIntent.putExtra("songer",bean.getText_singer());
                }else {
                    musicService.tag = true;
                    music_play.setSelected(true);
                    outIntent.setAction("MediaPlaybackService.START_ACTION");
                    outIntent.putExtra("songName",bean.getText_song());
                    outIntent.putExtra("songer",bean.getText_singer());
                    //开始播放
                    if (tag1 == false) {
                        tag1 = true;
                    } else {
                        tag1=false;
                    }
                }
                //控制刷新当前时间
                if (tag2 == false) {
                    handler.post(runnable);
                    tag2 = true;
                }
                musicService.playOrPause();
                Log.d("hct",""+musicService.mediaPlayer.isPlaying());
                mContext.sendBroadcast(outIntent);
                break;
            case R.id.playMode:
                //播放模式的切换单曲循环，随机播放，顺序播放
                if (modeNumber<2){
                   modeNumber=modeNumber+1;
                }else {
                    modeNumber=0;
                }
                playMode.setBackground(getResources().getDrawable(images[modeNumber]));


                break;
            case R.id.setting:
                //弹出音频调节的框
                //包名 包名+类名（全路径）
                /*Intent intent= new Intent();
                intent.setClassName("com.archermind.carSettings", "com.archermind.carSettings.activity.CarSettingActivity");
                intent.putExtra("app","media");
                startActivity(intent);*/
                Toast.makeText(this,"等待CarSettings加到桌面实现该功能",Toast.LENGTH_LONG).show();
                break;
            case R.id.arrow_down:
            case R.id.data_source:
                break;

        }

    }

    /*处理歌词begin*/
    public static void initLrc() {
        mlrcProcess = new LrcProcess();
        if(bean!=null) {
            mlrcProcess.readLRC(bean.getPath());
        }
        //传回处理后的歌词
        lrcList = mlrcProcess.getLrcList();
        music_words.setmLrcList(lrcList);
        Log.d("hct",""+lrcList.size());
        refreshLrcHandler.post(mRunnable);

    }
    public static Handler refreshLrcHandler = new Handler();
    //刷新歌词
    public static Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            music_words.setIndex(lrcIndex());
            music_words.invalidate();
            refreshLrcHandler.postDelayed(mRunnable,100);
        }
    };
    /**
     * 根据时间获取歌词显示的索引值
     * */
    private static int index=0;
    private static int duration;

    public static int  lrcIndex(){

        if (musicService!=null&&musicService.mediaPlayer.isPlaying()){
            currentTime = musicService.mediaPlayer.getCurrentPosition();
            duration = musicService.mediaPlayer.getDuration();
        }if (currentTime<duration){
            for (int i = 0; i <lrcList.size() ; i++) {
                if(i<lrcList.size()-1){
                    if (currentTime<lrcList.get(i).getLrcTime()&&i==0){
                        index=i;
                    }
                    if (currentTime>lrcList.get(i).getLrcTime()&&currentTime<lrcList.get(i+1).getLrcTime()){
                        index=i;
                    }
                    if (i==lrcList.size()-1){
                        index=i;
                    }
                }
            }
        }
        return  index;
    }
    /*处理歌词end*/

    private void   scanMusic() {
        int hasReadExternalStoragePermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            Activity activty=this;//1的话要进行询问，０的话不会询问
            ActivityCompat.requestPermissions(activty,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }else {
            beans = ScanMusic.getData(this,current_source_path);
            initView();
        }

        Log.i("hct","加载到了歌曲数据"+beans);

        if (beans.size()>0) {
            bean = beans.get(currentNumber);
            Log.d("hct","bean"+ beans.size());
            current_time.setVisibility(View.VISIBLE);
            musictotal.setVisibility(View.VISIBLE);
            Log.d("hct","加载到了歌曲数据"+beans);
        }else {
            Log.d("hct","没有歌曲");
            current_time.setVisibility(View.INVISIBLE);
            musictotal.setVisibility(View.INVISIBLE);

        }
        bindServiceConnection();
    }


    //发送歌曲信息给仪表
    private void sendMusicInfo(){
        Intent intent = new Intent();
        intent.setAction("com.archermind.music.MusicInfo");
        intent.putExtra("song_name",bean.getText_song());
        intent.putExtra("song_id",bean.getId());
        mContext.sendBroadcast(intent);
    }
}
