package com.mc.pcm;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "pcm";

    //是否在录制
    private boolean isRecording = false;
    //开始录音
    private Button startAudio;
    //结束录音
    private Button stopAudio;
    //播放录音
    private Button playAudio;
    //删除文件
    private Button deleteAudio;

    private ScrollView mScrollView;
    private TextView tv_audio_succeess;

    //pcm文件
    private File file;
    private static final int INPUT_SOURCE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        initView();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_F12){
            Log.d(TAG,"get keycode:" + event.getKeyCode());
        }else
        {
            Log.d(TAG,"get keycode:" + event.getKeyCode());
        }
        return super.dispatchKeyEvent(event);
    }

    //初始化View
    private void initView() {

        mScrollView = (ScrollView) findViewById(R.id.mScrollView);
        tv_audio_succeess = (TextView) findViewById(R.id.tv_audio_succeess);
        printLog("初始化成功");
        startAudio = (Button) findViewById(R.id.startAudio);
        startAudio.setOnClickListener(this);
        stopAudio = (Button) findViewById(R.id.stopAudio);
        stopAudio.setOnClickListener(this);
        playAudio = (Button) findViewById(R.id.playAudio);
        playAudio.setOnClickListener(this);
        deleteAudio = (Button) findViewById(R.id.deleteAudio);
        deleteAudio.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startAudio:
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                        StartRecord();
                        Log.e(TAG,"start");
                    }
                });
                thread.setName("NiceTest");
                thread.start();
                printLog("开始录音");
                ButtonEnabled(false, true, false);
                break;
            case R.id.stopAudio:
                isRecording = false;
                ButtonEnabled(true, false, true);
                printLog("停止录音");
                break;
            case R.id.playAudio:
                PlayRecord();
                ButtonEnabled(true, false, false);
                printLog("播放录音");
                break;
            case R.id.deleteAudio:
                deleFile();
                break;
        }
    }

    //打印log
    private void printLog(final String resultString) {
        tv_audio_succeess.post(new Runnable() {
            @Override
            public void run() {
                tv_audio_succeess.append(resultString + "\n");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    //获取/失去焦点
    private void ButtonEnabled(boolean start, boolean stop, boolean play) {
        startAudio.setEnabled(start);
        stopAudio.setEnabled(stop);
        playAudio.setEnabled(play);
    }

    short change(short value){
        return (short) (((value & 0x00ff) << 8 ) | ((value & 0xff00) >> 8));
    }
    //开始录音
    public void StartRecord() {
        Log.i(TAG,"startRecord");
        //16K采集率
        int frequency = 16000;
        //格式
        //int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        //16Bit
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
/*       //生成PCM文件
        file = new File("/sdcard/reverseme.pcm");
        //如果存在，就先删除再创建
        if (file.exists()){
            file.delete();
            Log.i(TAG,"reverseme.pcm existed,rm it");
        }
        try {
            file.createNewFile();
            Log.i(TAG,"create file reverseme.pcm");
        } catch (IOException e) {
            Log.i(TAG,"create reverseme.pcm failed");
            throw new IllegalStateException("failed" + file.toString());
        }*/
        try {
/*            //输出流
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);*/
            Log.e(TAG,"=========-1========");
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

            //AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

            byte[] buffer = new byte[bufferSize];
            Log.e(TAG,"=========2========");
            audioRecord.startRecording();
            Log.e(TAG,"=========3========");
            Log.i(TAG, "begin to record:userSize:" + bufferSize);
            isRecording = true;
            MyFile myFile = MyFile.creatMyFileForWrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm");
            while (isRecording) {
                long  now = System.currentTimeMillis();
                int bufferReadResult = audioRecord.read(buffer, 0, buffer.length);

                //Log.d(TAG,"begin write record data");
                myFile.writeData(buffer,buffer.length);
                long latency = System.currentTimeMillis() - now;
                Log.d(TAG,"read latency:" + latency);
                //Log.d(TAG,"finish write record data");
            }
            myFile.stopWrite();
            //audioRecord.stop();
            audioRecord.release();


        } catch (Exception e) {
            Log.e(TAG, "record failed:" + e);
        }


    }

    //播放文件
    public void PlayRecord() {
        if(file == null){
            return;
        }
        //读取文件
        int musicLength = (int) (file.length() / 2);
        short[] music = new short[musicLength];
        try {
            InputStream is = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            DataInputStream dis = new DataInputStream(bis);
            int i = 0;
            while (dis.available() > 0) {
                music[i] = dis.readShort();
                i++;
            }
            dis.close();
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    16000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    musicLength * 2,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();
            audioTrack.write(music, 0, musicLength);
            audioTrack.stop();
        } catch (Throwable t) {
            Log.e(TAG, "播放失败");
        }
    }

    //删除文件
    private void deleFile() {
        if(file == null){
            return;
        }
        file.delete();
        printLog("文件删除成功");
    }

}

