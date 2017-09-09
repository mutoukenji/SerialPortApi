package tech.yaog.hardwares.serialport.sample;

import android.support.annotation.Nullable;

/**
 * 支付通信数据包
 * Created by ygl_h on 2017/9/9.
 */
public class PayFrame {
    private byte command;
    private byte[] feature;
    private int length;
    private byte[] data;
    private byte checksum;

    private PayFrame() {

    }

    /**
     * 从原始数据流生成数据包
     * @param bytes 原始数据流
     * @return 生成的数据包，如果校验不对则为 {@code null}
     * @throws ArrayIndexOutOfBoundsException 数据流长度错误或数据流内格式错误
     */
    @Nullable
    public static PayFrame fromBytes(byte[] bytes) throws ArrayIndexOutOfBoundsException {
        int offset = 0;
        int checksum = 0;
        PayFrame frame = new PayFrame();
        frame.command = bytes[offset++];
        checksum += frame.command;
        frame.feature = new byte[3];
        for (int i = 0;i<3;i++) {
            frame.feature[i] = bytes[offset++];
            checksum += frame.feature[i];
        }
        frame.length = bytes[offset++] & 0x00FF;
        checksum += frame.length;
        frame.data = new byte[frame.length];
        for (int i = 0;i<frame.length;i++) {
            frame.data[i] = bytes[offset++];
            checksum += frame.data[i];
        }
        frame.checksum = bytes[offset];
        if (frame.feature[0] == (byte)0xE3
            && frame.feature[1] == (byte)0xE6
            && frame.feature[2] == (byte)0xE9
            && (checksum & 0xFF) == (frame.checksum & 0xFF)) {
            return frame;
        }
        return null;
    }

    public byte getCommand() {
        return command;
    }

    public byte[] getFeature() {
        return feature;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public byte getChecksum() {
        return checksum;
    }
}
