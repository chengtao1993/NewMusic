package com.archermind.music;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class MusicWidgetBroadcastReceiver extends BroadcastReceiver {
   public  Intent outIntent = new Intent();

    @Override
    public void onReceive(Context context, Intent intent) {
        outIntent.setComponent(new ComponentName("com.archermind.c26poccontrol.tachograph","com.archermind.c26poccontrol.tachograph.MusicAppWidget"));
        String action= intent.getAction();
        if (action.equals("MediaPlaybackService.START_ACTION")) {//播放
            outIntent.setAction("MediaPlaybackService.START_ACTION");
            outIntent.putExtra("songName",MusicFragment.bean.getText_song());
            outIntent.putExtra("songer",MusicFragment.bean.getText_singer());
            Log.d("ttt","action = "+action);
            MusicFragment.musicService.playOrPause();
            MusicFragment.musicService.tag = true;
            MusicFragment.music_play.setSelected(true);
        }
        if (action.equals("MediaPlaybackService.SUSPEND_ACTION")) {//暂停
            outIntent.setAction("MediaPlaybackService.SUSPEND_ACTION");
            outIntent.putExtra("songName",MusicFragment.bean.getText_song());
            outIntent.putExtra("songer",MusicFragment.bean.getText_singer());
            MusicFragment.musicService.playOrPause();
            MusicFragment.musicService.tag = false;
            MusicFragment.music_play.setSelected(false);
        }
        if (action.equals("MediaPlaybackService.PREV_ACTION")) {//上一首
            outIntent.setAction("MediaPlaybackService.PREV_ACTION");
            if (MusicFragment.currentNumber>0){
                MusicFragment.currentNumber=MusicFragment.currentNumber-1;
            }else {
                MusicFragment.currentNumber= MusicFragment.beans.size()-1;
            }
            MusicFragment.bean=MusicFragment.fileInfo.get(MusicFragment.currentNumber);
            MusicFragment.changeView();
            outIntent.putExtra("songName",MusicFragment.bean.getText_song());
            outIntent.putExtra("songer",MusicFragment.bean.getText_singer());
        }
        if (action.equals("MediaPlaybackService.NEXT_ACTION")) {//下一首
            outIntent.setAction("MediaPlaybackService.NEXT_ACTION");
            if(MusicFragment.currentNumber<MusicFragment.beans.size()-1){
                MusicFragment.currentNumber=MusicFragment.currentNumber+1;
            }else {
                MusicFragment.currentNumber=0;
            }
            MusicFragment.bean=MusicFragment.fileInfo.get(MusicFragment.currentNumber);
            MusicFragment.changeView();
            outIntent.putExtra("songName",MusicFragment.bean.getText_song());
            outIntent.putExtra("songer",MusicFragment.bean.getText_singer());
        }
        context.sendBroadcast(outIntent);
    }

}
