package com.mc.pcm;

/**
 * Created by xueqian.zhang on 2017/11/13.
 */

import android.os.Environment;

import java.io.File;

/**
 * Created by Mr Chen on 2017/5/2.
 */

public class Constants {
    public static final String ASSETS_RES_DIR = "SoundConnect";
    public static final String DEFAULT_WORK_SPACE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundConnect/";
    public static final String ACTIVE_UMDL = "soundconnectwakeup.umdl";
    public static final String ACTIVE_RES = "common.res";
    public static final String SD_VAD_CONF = "soundconnect_vad.conf";
    public static final String SAVE_AUDIO = Constants.DEFAULT_WORK_SPACE + File.separatorChar + "recording.pcm";
    public static final int SAMPLE_RATE_16K = 16000;
    public static final int MAX_DELAY_FRAMES = 200;//10ms一组，算出是2s

    public static final String FOR_TIME_DELAY = "FOR_TIME_DELAY";
}
