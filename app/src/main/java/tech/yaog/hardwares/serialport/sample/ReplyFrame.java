package tech.yaog.hardwares.serialport.sample;

/**
 * Created by ygl_h on 2017/9/10.
 */

public interface ReplyFrame {
    byte RESULT_OK = 0;
    byte RESULT_NG = 1;

    PayReplyFrame toFrame();
}
