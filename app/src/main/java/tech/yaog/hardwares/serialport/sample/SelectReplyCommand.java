package tech.yaog.hardwares.serialport.sample;

/**
 * Created by ygl_h on 2017/9/10.
 */

public class SelectReplyCommand implements ReplyFrame {
    private static final byte ID = 0x1B;

    private byte result;
    private byte cabinetNo;
    private byte cargoNo;

    public SelectReplyCommand(byte result, byte cabinetNo, byte cargoNo) {
        this.result = result;
        this.cabinetNo = cabinetNo;
        this.cargoNo = cargoNo;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public byte getCabinetNo() {
        return cabinetNo;
    }

    public void setCabinetNo(byte cabinetNo) {
        this.cabinetNo = cabinetNo;
    }

    public byte getCargoNo() {
        return cargoNo;
    }

    public void setCargoNo(byte cargoNo) {
        this.cargoNo = cargoNo;
    }

    @Override
    public PayReplyFrame toFrame() {
        return PayReplyFrame.fromData(ID, result, cabinetNo, cargoNo);
    }
}
