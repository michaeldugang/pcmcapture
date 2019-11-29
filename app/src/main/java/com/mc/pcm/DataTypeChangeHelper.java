package com.mc.pcm;

/**
 * Created by xueqian.zhang on 2017/11/13.
 */

public class DataTypeChangeHelper {
    /**
     * 将一个单字节的byte转换成32位的int
     *
     * @param b
     *      byte
     * @return convert result
     */
    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * 将一个单字节的Byte转换成十六进制的数
     *
     * @param b
     *      byte
     * @return convert result
     */
    public static String byteToHex(byte b) {
        int i = b & 0xFF;
        return Integer.toHexString(i);
    }

    /**
     * 将一个4byte的数组转换成32位的int
     *
     * @param buf
     *      bytes buffer
     * @param  pos  byte[]中开始转换的位置
     * @return convert result
     */
    public static long unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        index = index + 4;
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }

    /**
     * 将16位的short转换成byte数组
     *
     * @param s
     *      short
     * @return byte[] 长度为2
     * */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * 将32位整数转换成长度为4的byte数组
     *
     * @param s
     *      int
     * @return byte[]
     * */
    public static byte[] intToByteArray(int s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 4; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * long to byte[]
     *
     * @param s
     *      long
     * @return byte[]
     * */
    public static byte[] longToByteArray(long s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 8; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**32位int转byte[]*/
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    /**
     * 将长度为2的byte数组转换为16位int
     *
     * @param res
     *      byte[]
     * @return int
     * */
    public static int byte2int(byte[] res) {
        // res = InversionByte(res);
        // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00); // | 表示安位或
        return targets;
    }

    /**
     * byte数组转short数组   小端储存
     * @param src
     * @return
     */
    public static short[] byteArrayToShortArray(byte[] src) {
        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) ((src[2 * i + 1] << 8) | (src[2 * i] & 0xff));
        }
        return dest;
    }

    /**
     * short数组转byte数组  小端储存
     * @param src
     * @return
     */
    public static byte[] shortArrayToByteArray(short[] src) {

        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i]&0xff);
            dest[i * 2 + 1] = (byte) (src[i] >> 8 &0xff);
        }

        return dest;
    }

    /**
     * convert double signals to 16-bit pcm
     * @param d
     * double signals
     * @param s
     * 16-bit pcm
     * @param numsamples
     * no. of samples to be converted
     */
    public static void double2Short(double[] d, short[] s, int numsamples)
    {
        for(int i=0;i<numsamples;i++) s[i]=double2Short(d[i]);
    }

    /**
     * convert a double signal sample to 16 bits pcm sample
     * @param d
     * a double sample
     * @return
     */
    public static short double2Short(double d)
    {
        if(d>1.0) d=1.0;
        else if(d<-1.0) d=-1.0;

        return (short)(d*Short.MAX_VALUE);
    }

    /**
     * convert a 2-byte short sample to double sample
     * @param s
     * 16 bits pcm sample
     * @return
     */
    public static double short2Double(short s)
    {
        return (double)s/Short.MAX_VALUE;
    }

    /**
     * convert 2-byte short samples to double samples
     * @param s
     * 16-bit samples
     * @param d
     * double samples
     * @param numsamples
     * no. of samples to be converted
     */
    public static void short2Double(short[] s, double[] d, int numsamples)
    {
        for(int i=0;i<numsamples;i++) d[i]=short2Double(s[i]);
    }

    /**
     * convert a 2-byte short sample to float sample
     * @param s
     * 16 bits pcm sample
     * @return
     */
    public static float short2Float(short s)
    {
        return (float)s/Short.MAX_VALUE;
    }

    /**
     * convert 16-bit pcm to float
     * @param s
     * 16-bit pcm data
     * @param f
     * float data
     * @param numsamples
     * no. of samples to be converted
     */
    public static void short2Float(short[] s, float[] f, int numsamples)
    {
        for(int i=0;i<numsamples;i++) f[i]=short2Float(s[i]);
    }

    /**
     * convert a float signal sample to 16 bits pcm sample
     * @param f
     * a float sample
     * @return
     */
    public static short float2Short(float f)
    {
        if(f>1.0) f=1.0f;
        else if(f<-1.0) f=-1.0f;

        return (short)(f*Short.MAX_VALUE);
    }

    /**
     * convert float signal to 16-bit pcm
     * @param f
     * float signal
     * @param s
     * 16-bit pcm
     * @param numsamples
     * no. of samples to be converted
     */
    public static void float2Short(float[] f, short[] s, int numsamples)
    {
        for(int i=0;i<numsamples;i++) s[i]=float2Short(f[i]);
    }

}


