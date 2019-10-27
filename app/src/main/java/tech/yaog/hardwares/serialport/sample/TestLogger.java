package tech.yaog.hardwares.serialport.sample;

import tech.yaog.hardwares.serialport.LogcatLogger;
import tech.yaog.hardwares.serialport.Logger;

/**
 * Created by ygl_h on 2017/10/9.
 */

public class TestLogger {
    private Logger logger = new LogcatLogger();

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void anyMethod() {
        //xxxxxxxxxxxxxx
        //1111111111
        //222222
        //x = x +1;
        logger.d("XXX","message");
        //xxxxxxxxxxxxxx
    }

}
