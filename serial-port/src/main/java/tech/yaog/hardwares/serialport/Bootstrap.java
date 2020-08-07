package tech.yaog.hardwares.serialport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 串口通讯启动器
 * Created by mutoukenji on 17-8-12.
 */
public class Bootstrap {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public ReceiverStartEvent getReceiverStartEventListener() {
        return receiverStartEventListener;
    }

    /**
     * 注册接收进程开始事件监听
     * @param receiverStartEventListener 接收进程开始事件监听
     * @return self
     */
    public Bootstrap setReceiverStartEventListener(ReceiverStartEvent receiverStartEventListener) {
        this.receiverStartEventListener = receiverStartEventListener;
        return this;
    }

    /**
     * 接收进程开始事件
     */
    public interface ReceiverStartEvent {
        /**
         * 接收进程开始
         * @param bootstrap 对应的 Bootstrap
         */
        void receiverStarted(Bootstrap bootstrap);
    }

    private static final String TAG = Bootstrap.class.getName();

    private SerialPort serialPort;
    private Thread receiveThread;
    private Thread sendThread;
    private ThreadPoolExecutor workgroup;
    private String path;
    private int baudrate;
    private int csize;
    private int parity;
    private int stopbits;
    private int flags;
    private boolean rtscts;
    private boolean xonxoff;
    private Logger logger;
    private boolean vvv = false;
    private List<AbstractDecoder> decoders = new ArrayList<>();
    private List<AbstractEncoder> encoders = new ArrayList<>();
    private List<AbstractHandler> handlers = new ArrayList<>();

    private final Object sendLock = new Object();
    private Queue<Object> sendList = new ArrayBlockingQueue<>(50000);

    private ReceiverStartEvent receiverStartEventListener;

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
        this.vvv = vvv;
        return this;
    }

    /**
     * Set logger adapter
     * @param logger logger adapter
     * @return self
     */
    public Bootstrap logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Set decoder methods
     * @param decoders decoder methods
     * @return self
     */
    public Bootstrap decode(AbstractDecoder... decoders) {
        for (AbstractDecoder decoder : decoders) {
            if (!this.decoders.contains(decoder)) {
                this.decoders.add(decoder);
            }
        }
        return this;
    }

    /**
     * Set encoder methods
     * @param encoders encoder methods
     * @return self
     */
    public Bootstrap encode(AbstractEncoder... encoders) {
        for (AbstractEncoder encoder : encoders) {
            if (!this.encoders.contains(encoder)) {
                this.encoders.add(encoder);
            }
        }
        return this;
    }

    /**
     * Set data handlers
     * @param handlers data handlers
     * @return self
     */
    public Bootstrap handle(AbstractHandler... handlers) {
        for (AbstractHandler handler : handlers) {
            if (!this.handlers.contains(handler)) {
                this.handlers.add(handler);
            }
        }
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
     * Send message
     * @param message message to be sent
     */
    protected void doSend(Object message) {
        byte[] data = null;
        if (message instanceof byte[]) {
            data = (byte[]) message;
        } else {
            if (encoders != null) {
                for (AbstractEncoder encoder : encoders) {
                    java.lang.reflect.Type[] types = ((ParameterizedType) encoder.getClass().getGenericSuperclass()).getActualTypeArguments();
                    if (types.length == 1 && message.getClass().equals(types[0])) {
                        try {
                            byte[] tmp = encoder.encode(message);
                            if (tmp != null) {
                                data = tmp;
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (data != null) {
            try {
                serialPort.getOutputStream().write(data);
                if (vvv && logger != null) {
                    logger.v(TAG, "Tx: %s", bytesToHex(data));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (logger != null) {
                logger.e(TAG, "cannot encode message: %s", message);
            }
        }
    }

    /**
     * Send message
     * @param message message to be sent
     * @return self
     */
    public Bootstrap send(Object message) {
        synchronized (sendLock) {
            sendList.offer(message);
        }
        return this;
    }

    /**
     * Set rtx mark manually
     * @param mark rtx status
     */
    public void setRtx(boolean mark) {
        if (serialPort != null) {
            serialPort.setRtx(mark);
        }
    }

    /**
     * Get ctx mark manually
     * @return ctx status
     */
    public boolean getCtx() {
        if (serialPort != null) {
            return serialPort.getCtx();
        }
        return false;
    }

    /**
     * Open serial port and start handling
     * @return self
     * @throws IOException serial port open failed
     */
    public Bootstrap start() throws IOException {
        //创建等待队列
        BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<>(20);
        workgroup = new ThreadPoolExecutor(2, 10, 5, TimeUnit.SECONDS, bqueue);
        serialPort = new SerialPort(new File(path), baudrate, csize, parity, stopbits, rtscts, xonxoff, flags);

        final InputStream is = serialPort.getInputStream();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        Object toSend = null;
                        synchronized (sendLock) {
                            if (!sendList.isEmpty()) {
                                toSend = sendList.poll();
                            }
                        }
                        if (toSend != null) {
                            doSend(toSend);
                        }
                        else {
                            Thread.sleep(1);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        sendThread.setName(path + " Sender");
        sendThread.setPriority(8);
        sendThread.start();

        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024 * 1024];
                while (!Thread.interrupted()) {
                    try {
                        byte[] data = new byte[0];
                        int read;
                        boolean isPack = false;
                        int emptyCount = 0;
                        do {
                            if (is.available() > 0 && (read = is.read(buffer)) > 0) {
                                int position = data.length;
                                data = Arrays.copyOf(data, position + read);
                                System.arraycopy(buffer, 0, data, position, read);
                                emptyCount = 0;
                            }
                            else {
                                if (++emptyCount >= 10) {
                                    isPack = true;
                                }
                                TimeUnit.MILLISECONDS.sleep(1);
                            }
                        }
                        while (!isPack);
                        if (data.length > 0) {
                            if (vvv && logger != null) {
                                logger.v(TAG, "Rx: %s", bytesToHex(data));
                            }
                            final byte[] h_data = data;
                            workgroup.execute(new Runnable() {
                                @Override
                                public void run() {
                                    handle(h_data);
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        receiveThread.setName(path + " Receiver");
        receiveThread.setPriority(Thread.MAX_PRIORITY);
        receiveThread.start();
        if (receiverStartEventListener != null) {
            receiverStartEventListener.receiverStarted(this);
        }

        return this;
    }

    /**
     * Close serial port and stop all handling
     */
    public void stop() {
        receiveThread.interrupt();
        workgroup.shutdown();
        serialPort.close();
    }

    /**
     * 处理被拆包的数据
     * @param data raw data
     */
    protected void handle(byte[] data) {
        Object message = data;
        if (decoders != null) {
            for (AbstractDecoder decoder : decoders) {
                try {
                    Object tmp = decoder.decode(data);
                    if (tmp != null) {
                        message = tmp;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        boolean handled = false;
        if (handlers != null) {
            for (AbstractHandler handler : handlers) {
                java.lang.reflect.Type[] types = ((ParameterizedType) handler.getClass().getGenericSuperclass()).getActualTypeArguments();
                if (types[0] instanceof Class) {
                    Class typeClazz = (Class) (types[0]);
                    if (typeClazz.isAssignableFrom(message.getClass())) {
                        try {
                            if ((handled = handler.handle(message, this))) {
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (!handled) {
            if (logger != null) {
                logger.e(TAG, "Message not handled: data=" + Arrays.toString(data));
            }
        }
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
