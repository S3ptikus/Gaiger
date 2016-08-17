package com.stalker.geiger.omen.geiger.services;

import android.content.Context;

/**
 * Created by p.yurkin on 17.08.16.
 */
public class Constants {
        public static final int SOUND_LOW_COUNT = 1;
        public static final int SOUND_HIGH_COUNT = 2;
        public static final int SOUND_DEAD = 3;

        public static final void initSoundManager(Context context, SoundManager soundManager){
            soundManager.addSound(context, SOUND_LOW_COUNT);
            soundManager.addSound(context, SOUND_HIGH_COUNT);
            soundManager.addSound(context, SOUND_DEAD);
        }
}
