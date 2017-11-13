package com.zzhoujay.richtext.ig;

import android.graphics.Rect;

import com.zzhoujay.richtext.ImageHolder;
import com.zzhoujay.richtext.ext.Debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zhou on 2017/10/2.
 */

public class DrawableSizeHolder {

    public Rect rect;
    @ImageHolder.ScaleType
    public int scaleType;
    public String name;
    public ImageHolder.BorderHolder borderHolder;

    public DrawableSizeHolder(String name, Rect rect, @ImageHolder.ScaleType int scaleType, ImageHolder.BorderHolder borderHolder) {
        this.rect = rect;
        this.scaleType = scaleType;
        this.name = name;
        this.borderHolder = borderHolder;
    }

    public DrawableSizeHolder(ImageHolder holder) {
        this(
                holder.getKey(),
                new Rect(0, 0, holder.getWidth(), holder.getHeight()),
                holder.getScaleType(),
                new ImageHolder.BorderHolder()
        );
    }

    public void save(OutputStream fos) {
        try {
            writeInt(fos, rect.left);
            writeInt(fos, rect.top);
            writeInt(fos, rect.right);
            writeInt(fos, rect.bottom);
            writeInt(fos, scaleType);
            writeBoolean(fos, borderHolder.isShowBorder());
            writeInt(fos, borderHolder.getBorderColor());
            writeFloat(fos, borderHolder.getBorderSize());
            writeFloat(fos, borderHolder.getRadius());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Debug.e(e);
        }
    }

    public static DrawableSizeHolder read(InputStream fis, String name) {
        try {
            int left = readInt(fis);
            int top = readInt(fis);
            int right = readInt(fis);
            int bottom = readInt(fis);
            int scaleType = readInt(fis);
            boolean showBorder = readBoolean(fis);
            int color = readInt(fis);
            float borderSize = readFloat(fis);
            float borderRadius = readFloat(fis);
            fis.close();
            Rect rect = new Rect(left, top, right, bottom);
            ImageHolder.BorderHolder borderHolder = new ImageHolder.BorderHolder(showBorder, borderSize, color, borderRadius);
            return new DrawableSizeHolder(name, rect, getScaleType(scaleType), borderHolder);
        } catch (IOException e) {
            Debug.e(e);
        }
        return null;
    }


    @ImageHolder.ScaleType
    private static int getScaleType(int value) {
        switch (value) {
            case ImageHolder.ScaleType.CENTER:
                return ImageHolder.ScaleType.CENTER;
            case ImageHolder.ScaleType.CENTER_CROP:
                return ImageHolder.ScaleType.CENTER_CROP;
            case ImageHolder.ScaleType.CENTER_INSIDE:
                return ImageHolder.ScaleType.CENTER_INSIDE;
            case ImageHolder.ScaleType.FIT_CENTER:
                return ImageHolder.ScaleType.FIT_CENTER;
            case ImageHolder.ScaleType.FIT_START:
                return ImageHolder.ScaleType.FIT_START;
            case ImageHolder.ScaleType.FIT_END:
                return ImageHolder.ScaleType.FIT_END;
            case ImageHolder.ScaleType.FIT_XY:
                return ImageHolder.ScaleType.FIT_XY;
            case ImageHolder.ScaleType.FIT_AUTO:
                return ImageHolder.ScaleType.FIT_AUTO;
            default:
                return ImageHolder.ScaleType.NONE;
        }
    }


    private static void writeBoolean(OutputStream stream, boolean b) throws IOException {
        stream.write(b ? 1 : 0);
    }

    private static boolean readBoolean(InputStream stream) throws IOException {
        return stream.read() != 0;
    }

    private static int readInt(InputStream stream) throws IOException {
        byte[] bs = new byte[4];
        //noinspection ResultOfMethodCallIgnored
        stream.read(bs);
        return byte2int(bs);
    }

    private static void writeInt(OutputStream stream, int i) throws IOException {
        stream.write(int2byte(i));
    }

    private static void writeFloat(OutputStream stream, float f) throws IOException {
        stream.write(int2byte(Float.floatToIntBits(f)));
    }

    private static float readFloat(InputStream stream) throws IOException {
        return Float.intBitsToFloat(readInt(stream));
    }


    private static byte[] int2byte(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    private static int byte2int(byte[] res) {
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
    }

}