package tech.yaog.hardwares.serialport.sample;

/**
 * Created by ygl_h on 2017/9/10.
 */

public class PayReplyFrame {
    private byte command;
    private byte[] feature;
    private int length;
    private byte result;
    private byte[] data;
    private byte checksum;

    private PayReplyFrame() {

    }

    public static PayReplyFrame fromData(byte command, byte result, byte... data) {
        int checksum = 0;
        PayReplyFrame payReplyFrame = new PayReplyFrame();
        payReplyFrame.command = command;
        checksum += command;
        payReplyFrame.feature = new byte[]{(byte) 0xE3, (byte) 0xE6, (byte) 0xE9};
        for (byte feature : payReplyFrame.feature) {
            checksum += feature;
        }
        payReplyFrame.length = 1 + data.length;
        checksum += payReplyFrame.length;
        payReplyFrame.result = result;
        checksum += result;
        payReplyFrame.data = data;
        for (byte d : data) {
            checksum += d;
        }
        payReplyFrame.checksum = (byte) (checksum & 0x00FF);
        return payReplyFrame;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[1 + feature.length + 1 + 1 + data.length + 1];
        int offset = 0;
        bytes[offset++] = command;
        for (byte b : feature) {
            bytes[offset++] = b;
        }
        bytes[offset++] = (byte) (length & 0x00FF);
        bytes[offset++] = result;
        for (byte b : data) {
            bytes[offset++] = b;
        }
        bytes[offset] = checksum;
        return bytes;
    }
}
