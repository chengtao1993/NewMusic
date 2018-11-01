package com.archermind.newmusic;

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
            outIntent.putExtra("songName", MainActivity.bean.getText_song());
            outIntent.putExtra("songer", MainActivity.bean.getText_singer());
            Log.d("ttt","action = "+action);
            MainActivity.musicService.playOrPause();
            MainActivity.musicService.tag = true;
            MainActivity.music_play.setSelected(true);
        }
        if (action.equals("MediaPlaybackService.SUSPEND_ACTION")) {//暂停
            outIntent.setAction("MediaPlaybackService.SUSPEND_ACTION");
            outIntent.putExtra("songName", MainActivity.bean.getText_song());
            outIntent.putExtra("songer", MainActivity.bean.getText_singer());
            MainActivity.musicService.playOrPause();
            MainActivity.musicService.tag = false;
            MainActivity.music_play.setSelected(false);
        }
        if (action.equals("MediaPlaybackService.PREV_ACTION")) {//上一首
            outIntent.setAction("MediaPlaybackService.PREV_ACTION");
            if (MainActivity.currentNumber>0){
                MainActivity.currentNumber= MainActivity.currentNumber-1;
            }else {
                MainActivity.currentNumber= MainActivity.beans.size()-1;
            }
            MainActivity.bean= MainActivity.beans.get(MainActivity.currentNumber);
            MainActivity.changeView();
            outIntent.putExtra("songName", MainActivity.bean.getText_song());
            outIntent.putExtra("songer", MainActivity.bean.getText_singer());
        }
        if (action.equals("MediaPlaybackService.NEXT_ACTION")) {//下一首
            outIntent.setAction("MediaPlaybackService.NEXT_ACTION");
            if(MainActivity.currentNumber< MainActivity.beans.size()-1){
                MainActivity.currentNumber= MainActivity.currentNumber+1;
            }else {
                MainActivity.currentNumber=0;
            }
            MainActivity.bean= MainActivity.beans.get(MainActivity.currentNumber);
            MainActivity.changeView();
            outIntent.putExtra("songName", MainActivity.bean.getText_song());
            outIntent.putExtra("songer", MainActivity.bean.getText_singer());
        }
        context.sendBroadcast(outIntent);
    }

}
