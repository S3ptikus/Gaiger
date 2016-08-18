package com.stalker.geiger.omen.geiger.common;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by omen on 18.08.2016.
 */
public class MediaPlayerExt extends MediaPlayer implements MediaPlayer.OnPreparedListener{

    private String TAG = this.getClass().getSimpleName();
    private Context context;
    private Hashtable<Integer, String> mapSound;
    private Boolean isLoaded = false;
    private Integer curPlayId;

    public MediaPlayerExt(Context pCntx) {
        super();
        context = pCntx;
        mapSound = new Hashtable<Integer, String>();
        this.setVolume(1f, 1f);
        this.setOnPreparedListener(this);
    }

    public void addUri(Integer pKey ,String pAssetsUri){
        if (!mapSound.containsKey(pKey))
            mapSound.put(pKey,pAssetsUri);
    }

    public void playSound(Integer pSoundId){
        if (curPlayId == pSoundId) {
            if (!isPlaying())
                this.start();
            else
                return;
        } else {
            this.reset();
            try {
                AssetFileDescriptor descriptor = context.getAssets().openFd(mapSound.get(pSoundId));
                this.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                this.prepareAsync();
                curPlayId = pSoundId;
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isLoaded = true;
    }
}
