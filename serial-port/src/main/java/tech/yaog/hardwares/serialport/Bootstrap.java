package tech.yaog.hardwares.serialport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import tech.yaog.utils.aioclient.AbstractDecoder;
import tech.yaog.utils.aioclient.AbstractEncoder;
import tech.yaog.utils.aioclient.AbstractHandler;
import tech.yaog.utils.aioclient.AbstractSplitter;
import tech.yaog.utils.aioclient.io.IO;

/**
 * 串口通讯启动器
 * Created by mutoukenji on 17-8-12.
 */
public class Bootstrap extends tech.yaog.utils.aioclient.Bootstrap {

    private static final String TAG = Bootstrap.class.getName();

    private String path;
    private int baudrate;
    private int csize;
    private int parity;
    private int stopbits;
    private int flags;
    private boolean rtscts;
    private boolean xonxoff;

    @Override
    public Bootstrap onEvent(Event eventListener) {
        return (Bootstrap) super.onEvent(eventListener);
    }

    @Override
    public Bootstrap exceptionHandler(ExceptionHandler exceptionHandler) {
        return (Bootstrap) super.exceptionHandler(exceptionHandler);
    }

    @Override
    public Bootstrap addDecoder(AbstractDecoder<?> decoder) {
        return (Bootstrap) super.addDecoder(decoder);
    }

    @Override
    public Bootstrap decoders(AbstractDecoder<?>... decoders) {
        return (Bootstrap) super.decoders(decoders);
    }

    @Override
    public Bootstrap addEncoder(AbstractEncoder<?> encoder) {
        return (Bootstrap) super.addEncoder(encoder);
    }

    @Override
    public Bootstrap encoders(AbstractEncoder<?>... encoders) {
        return (Bootstrap) super.encoders(encoders);
    }

    @Override
    public Bootstrap addHandler(AbstractHandler<?> handler) {
        return (Bootstrap) super.addHandler(handler);
    }

    @Override
    public Bootstrap handlers(AbstractHandler<?>... handlers) {
        return (Bootstrap) super.handlers(handlers);
    }

    @Override
    public Bootstrap splitter(AbstractSplitter splitter) {
        return (Bootstrap) super.splitter(splitter);
    }

    /**
     * Get device path
     * @return device path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set device path
     * @param path device path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get baudrate
     * @return baudrate
     */
    public int getBaudrate() {
        return baudrate;
    }

    /**
     * Set baudrate
     * @param baudrate baudrate
     */
    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    /**
     * Get character size
     * @return character size
     */
    public int getCsize() {
        return csize;
    }

    /**
     * Set character size
     * @param csize character size. Use {@link SerialPort#CSIZE_5}, {@link SerialPort#CSIZE_6}, {@link SerialPort#CSIZE_7}, {@link SerialPort#CSIZE_8}
     */
    public void setCsize(int csize) {
        this.csize = csize;
    }

    /**
     * Get parity method
     * @return parity method
     */
    public int getParity() {
        return parity;
    }

    /**
     * Set parity method
     * @param parity parity method. Use {@link SerialPort#PARITY_NONE}, {@link SerialPort#PARITY_ODD}, {@link SerialPort#PARITY_EVEN},{@link SerialPort#PARITY_SPACE}
     */
    public void setParity(int parity) {
        this.parity = parity;
    }

    /**
     * Get stop bit
     * @return stop bit
     */
    public int getStopbits() {
        return stopbits;
    }

    /**
     * Set stop bit
     * @param stopbits stop bit. Use {@link SerialPort#STOP_BIT_1} or {@link SerialPort#STOP_BIT_2}
     */
    public void setStopbits(int stopbits) {
        this.stopbits = stopbits;
    }

    /**
     * Get extra flags while opening the device
     * @return extra flags while opening the device
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Set extra flags while opening the device
     * @param flags extra flags while opening the device
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Get whether auto hardware flow control is enabled
     * @return whether auto hardware flow control is enabled
     */
    public boolean isRtscts() {
        return rtscts;
    }

    /**
     * Get whether auto hardware flow control is enabled
     * @param rtscts whether auto hardware flow control is enabled
     */
    public void setRtscts(boolean rtscts) {
        this.rtscts = rtscts;
    }

    /**
     * Enable or disable verbose level log
     * @param vvv {@code true} for enable
     * @return self
     */
    public Bootstrap vvv(boolean vvv) {
        Config.vvv = vvv;
        return this;
    }

    /**
     * Set logger adapter
     * @param logger logger adapter
     * @return self
     */
    public Bootstrap logger(Logger logger) {
        Config.logger = logger;
        return this;
    }

    /**
     * Create {@code Bootstrap} with parameters
     * Flow control is disabled
     * @param path Device path
     * @param baudrate Baudrate
     * @param csize Characters size
     * @param parity Parity method
     * @param stopbits Stop bit
     * @param flags Extra flags while opening device
     * @return new Bootstrap instance
     */
    public Bootstrap configure(String path, int baudrate, int csize, int parity, int stopbits, int flags) {
        return configure(path, baudrate, csize, parity, stopbits, false, false, flags);
    }

    /**
     * Create {@code Bootstrap} with parameters
     * @param path Device path
     * @param baudrate Baudrate
     * @param csize Characters size
     * @param parity Parity method
     * @param stopbits Stop bit
     * @param rtscts Hardware flow control enable or not
     * @param xonxoff Software flow control enable or not
     * @param flags Extra flags while opening device
     * @return new Bootstrap instance
     */
    public Bootstrap configure(String path, int baudrate, int csize, int parity, int stopbits, boolean rtscts, boolean xonxoff, int flags) {
        this.path = path;
        this.baudrate = baudrate;
        this.csize = csize;
        this.parity = parity;
        this.stopbits = stopbits;
        this.flags = flags;
        this.rtscts = rtscts;
        this.xonxoff = xonxoff;
        return this;
    }

    /**
     * Open serial port and start handling
     * @return self
     */
    public Bootstrap start() {

        String url = path+":"+baudrate+":"+csize+":"+stopbits+":"+parity+":"+(rtscts?"1":(xonxoff?"2":"0"))+":"+flags;
        ioClass(SerialPortIO.class);

        connect(url);

        return this;
    }

    /**
     * Get whether software flow control is enabled
     * @return whether software flow control is enabled
     */
    public boolean isXonxoff() {
        return xonxoff;
    }

    /**
     * Set whether software flow control is enabled
     * @param xonxoff whether software flow control is enabled
     */
    public void setXonxoff(boolean xonxoff) {
        this.xonxoff = xonxoff;
    }
}
