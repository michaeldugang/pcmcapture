package com.mc.pcm;


/**
 * Created by xueqian.zhang on 2017/11/13.
 */

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mr Chen on 2017/6/5.
 * This class is created primarily to facilitate reading and writing audio files.
 * It shields all exceptions from throwing and handling
 */

public class MyFile {
    private static final String LOG_TAG = MyFile.class.getSimpleName();
    private static final int WRITE = 0;
    private static final int READ = 1;

    private File file = null;
    private String fileName = null;
    private FileInputStream fileInputStream = null;
    private FileOutputStream fileOutputStream = null;
    private static ArrayList<MyFile> myFilesForRead = new ArrayList<MyFile>();
    private static ArrayList<MyFile> myFilesForWrite = new ArrayList<MyFile>();

    private LinkedList<short[]> savingBufferlist = new LinkedList<short[]>();
    private Integer savingDataInPromise = new Integer(1);
    private Semaphore savingData = new Semaphore(0);

    private int fileTag = READ;
    private boolean isEnd = false;
    private boolean isUsed = false;

    public boolean isUsed() {
        return isUsed;
    }

    private MyFile(String fileName, int fileTag) throws Exception {
        this.fileTag = fileTag;
        file = new File(fileName);
        this.fileName = file.getAbsolutePath();
        if(fileTag == READ){
            isEnd = false;
            fileInputStream = new FileInputStream(file);
        }else if(fileTag == WRITE){
            fileOutputStream = new FileOutputStream(file);
        }else {
            throw new Exception("no file tag!");
        }

        isUsed = true;
    }


    /**
     *  打开一个可读的文件，并返回该文件（@MyFile）。
     *  当文件已经打开并被其它调用者使用时，返回该文件（@MyFile），打开文件失败时，
     *  返回null；
     * @param fileName
     * @return
     */
    public static synchronized MyFile creatMyFileForRead(String fileName){
        if(null == myFilesForRead) {
            MyFile myFile = null;
            try {
                myFile = new MyFile(fileName,READ);
            }catch (Exception e){
                Log.e(LOG_TAG,"can't open "+fileName+" with error:!"+e.toString());
                return null;
            }
            myFilesForRead.add(myFile);
            return myFile;
        }else{
            for (MyFile myflie :
                    myFilesForRead) {
                if (myflie.fileName() == fileName && !myflie.isUsed()){
                    return myflie;
                }
            }
            MyFile myFile = null;
            try {
                myFile = new MyFile(fileName,READ);
            }catch (Exception e){
                Log.e(LOG_TAG,"can't open "+fileName+" with error:!"+e.toString());
                return null;
            }
            myFilesForRead.add(myFile);
            return myFile;
        }
    }

    /**
     * 创建一个文件用来写数据。写数据单独建立线程。
     * @param fileName
     * @return
     */
    public static synchronized MyFile creatMyFileForWrite(String fileName){
        if(null == myFilesForWrite) {
            MyFile myFile = null;
            try {
                myFile = new MyFile(fileName,WRITE);
            }catch (Exception e){
                Log.e(LOG_TAG,"can't creat "+fileName+" with error:!"+e.toString());
                return null;
            }
            if(myFile.waitForWriteData()) {
                myFilesForWrite.add(myFile);
            }else{
                myFile = null;
            }
            return myFile;
        }else{
            for (MyFile myflie :
                    myFilesForWrite) {
                if (myflie.fileName() == fileName && !myflie.isUsed()){
                    return myflie;
                }
            }
            MyFile myFile = null;
            try {
                myFile = new MyFile(fileName,WRITE);
            }catch (Exception e){
                Log.e(LOG_TAG,"can't creat "+fileName+" with error:!"+e.toString());
                return null;
            }
            if(myFile.waitForWriteData()) {
                myFilesForWrite.add(myFile);
            }else{
                myFile = null;
            }
            myFilesForWrite.add(myFile);
            return myFile;
        }
    }

    public synchronized int writeData(short[] buffer, int len){
        short [] value = buffer.clone();
        setSavingBuffer(value);
        return 1;
    }
    public synchronized int writeData(byte[] buffer, int len){
        setSavingBuffer(DataTypeChangeHelper.byteArrayToShortArray(buffer));
        return 1;
    }

    private int writeDataToFile(short[] buffer, int len){
        int curlen = 0;
        curlen = len<buffer.length?len:buffer.length;
        byte[] writebuffer = new byte[curlen*2];
        for(int i=0; i<curlen; i++){
            writebuffer[2 * i] = (byte) (buffer[i] & 0xff);
            writebuffer[2 * i + 1] = (byte) (buffer[i] >> 8 & 0xff);
        }
        curlen = writeDataToFile(writebuffer,curlen*2);
        return curlen/2;
    }

    private int writeDataToFile(byte[] buffer, int len){
        if(len<0 || null == fileOutputStream)
            return -1;
        int curlen = 0;
        if(fileTag == WRITE){
            try {
                fileOutputStream.write(buffer);
                fileOutputStream.flush();
                curlen = len;
            }catch (Exception e){
                return -1;
            }
        }else {
            return -2;
        }
        return curlen;
    }

    public synchronized short[] readData(int len){
        if(len<0 || null == fileInputStream)
            return null;
        int curlen = len;
        short[] buffer = null;
        if(fileTag == READ){
            byte[] valueB = new byte[curlen * 2];
            int bufferSize;
            try {
                bufferSize = fileInputStream.read(valueB, 0, curlen * 2);
            } catch (Exception e) {
                return null;
            }
            if(bufferSize >0){
                curlen = bufferSize/2;
                buffer = new short[curlen];
                for (int i = 0; i < curlen; i++) {
                    buffer[i] = (short) ((valueB[2 * i + 1] << 8) | (valueB[2 * i] & 0xff));
                }
            }else if (bufferSize == -1) {
                isEnd = true;
            } else {
                return null;
            }
        }else {
            return null;
        }
        return buffer;
    }

    public boolean seek(int len){
        if(null == fileInputStream)
            return false;
        if(fileTag == READ){
            try {
                fileInputStream.skip(len);
            }catch (Exception e){
                Log.e(LOG_TAG,"can't seek file!");
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    public String fileName(){
        return fileName;
    }

    public boolean isEnd(){
        return this.isEnd;
    }

    public boolean close(){
        if(fileTag == READ){
            try {
                fileInputStream.close();
            }catch (Exception e){
                Log.e(LOG_TAG,"close file failed!");
                return false;
            }
        }else if(fileTag == WRITE){
            try {
                fileOutputStream.close();
            }catch (Exception e){
                Log.e(LOG_TAG,"close file failed!");
                return false;
            }
        }
        this.isUsed = false;
        if(!myFilesForRead.remove(this)) {
            Log.e(LOG_TAG,"remove file failed!");
            return false;
        }
        return true;
    }

    private void setSavingBuffer(short[] array){
        synchronized (savingDataInPromise) {
            if (savingBufferlist.size() > Constants.MAX_DELAY_FRAMES) {
                savingData.drainPermits();
                savingBufferlist.clear();
            }
            savingBufferlist.add(array);
            savingData.release();
        }
    }

    private short[] getSavingBuffer(){
        try {
            boolean getAcquire = savingData.tryAcquire(1, 10, TimeUnit.MILLISECONDS);
            if (!getAcquire) {
                return null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        short[] value;
        synchronized (savingDataInPromise) {
            value = savingBufferlist.poll();
        }
        return value;
    }
    private volatile boolean iswritedatarun = false;
    private boolean waitForWriteData(){
        iswritedatarun = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (iswritedatarun) {
                    short[] value = getSavingBuffer();
                    if (value != null) {
                        writeDataToFile(value, value.length);
                    }else {
                        try {
                            Thread.currentThread().sleep(2);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                if(fileOutputStream!= null){
                    try {
                        fileOutputStream.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        return true;
    }
    public int stopWrite(){
        if(iswritedatarun){
            iswritedatarun = false;
        }
        return 0;
    }

}