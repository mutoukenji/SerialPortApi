package tech.yaog.hardwares.serialport.sample;

import java.io.UnsupportedEncodingException;

/**
 * Created by ygl_h on 2017/9/10.
 */

public class HelloReplyCommand implements ReplyFrame {
    private static final byte ID = 0x18;

    private String ipcNo;
    private byte result;

    public HelloReplyCommand(String ipcNo, byte result) {
        this.ipcNo = ipcNo;
        this.result = result;
    }

    @Override
    public PayReplyFrame toFrame() {
        try {
            return PayReplyFrame.fromData(ID, result, ipcNo.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getIpcNo() {
        return ipcNo;
    }

    public void setIpcNo(String ipcNo) {
        this.ipcNo = ipcNo;
    }

    public int getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }
}
