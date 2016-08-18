package com.stalker.geiger.omen.geiger.services;

import android.content.Context;
import android.media.MediaPlayer;

import com.stalker.geiger.omen.geiger.R;
import com.stalker.geiger.omen.geiger.common.MediaPlayerExt;

/**
 * Created by p.yurkin on 17.08.16.
 */
public class Constants {
        public static final int SOUND_LOW_COUNT = 1;
        public static final int SOUND_HIGH_COUNT = 2;
        public static final int SOUND_DEAD = 3;

        public static final void initMediaPlayerExt(Context context, MediaPlayerExt pPlayerExt){
            pPlayerExt.addUri(SOUND_LOW_COUNT, context.getString(R.string.AssetsLowSndCount));
            pPlayerExt.addUri(SOUND_HIGH_COUNT, context.getString(R.string.AssetsSndCount));
            pPlayerExt.addUri(SOUND_DEAD, context.getString(R.string.AssetsSndDeath));
        }
}
