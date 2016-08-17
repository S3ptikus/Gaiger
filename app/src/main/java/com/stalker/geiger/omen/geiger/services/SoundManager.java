package com.stalker.geiger.omen.geiger.services;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;

import com.stalker.geiger.omen.geiger.R;

import java.io.IOException;

/**
 * Created by p.yurkin on 17.08.16.
 */
public class SoundManager {
    private String TAG = this.getClass().getSimpleName();
    private int curPlay;
    private SoundPool mSoundPool;

    private SparseIntArray mSoundPoolMap = new SparseIntArray();

    private Handler mHandler = new Handler();

    private static final int MAX_STREAMS = 2;
    private static final int STOP_DELAY_MILLIS = 1000;

    public SoundManager() {
        mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    }

    /**
     * Put the sounds to their correspondig keys in sound pool.
     */
    public void addSound(Context context, int soundID) {
        try {
            switch (soundID) {
                case 1:
                    mSoundPoolMap.put(soundID, mSoundPool.load(context.getAssets().openFd(context.getString(R.string.AssetsLowSndCount)), 1));
                    break;
                case 2:
                    mSoundPoolMap.put(soundID, mSoundPool.load(context.getAssets().openFd(context.getString(R.string.AssetsSndCount)), 1));
                    break;
                case 3:
                    mSoundPoolMap.put(soundID, mSoundPool.load(context.getAssets().openFd(context.getString(R.string.AssetsSndDeath)), 1));
                    break;
            }
        } catch (IOException e){
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * Find sound with the key and play it
     */
    public void playSound(int soundID) {

        boolean hasSound = mSoundPoolMap.indexOfKey(soundID) >= 0;
        if(!hasSound){
            return;
        }

        final int soundId = mSoundPool.play(mSoundPoolMap.get(soundID), 1, 1, 1, -1, 1f);
        curPlay = soundId;
        scheduleSoundStop(soundId);
    }

    public void pauseSound(){
        mSoundPool.pause(curPlay);
    }

    /**
     * Schedule the current sound to stop after set milliseconds
     */
    private void scheduleSoundStop(final int soundId){
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mSoundPool.stop(soundId);
            }
        }, STOP_DELAY_MILLIS);
    }

    /**
     * Initialize the control stream with the activity to music
     */
    public static void initStreamTypeMedia(Activity activity){
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public static int getStreamMusicLevel(Activity activity){
        AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        return am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
}
