package tech.yaog.hardwares.serialport;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 串口通讯启动器
 * Created by mutoukenji on 17-8-12.
 */
public class Bootstrap {
    private static final String TAG = Bootstrap.class.getName();

    private SerialPort serialPort;
    private Thread receiveThread;
    private ThreadPoolExecutor workgroup;
    private String path;
    private int baudrate;
    private int csize;
    private int parity;
    private int stopbits;
    private int flags;
    private List<AbstractDecoder> decoders = new ArrayList<>();
    private List<AbstractEncoder> encoders = new ArrayList<>();
    private List<AbstractHandler> handlers = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public int getCsize() {
        return csize;
    }

    public void setCsize(int csize) {
        this.csize = csize;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getStopbits() {
        return stopbits;
    }

    public void setStopbits(int stopbits) {
        this.stopbits = stopbits;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Bootstrap decode(AbstractDecoder... decoders) {
        for (AbstractDecoder decoder : decoders) {
            if (!this.decoders.contains(decoder)) {
                this.decoders.add(decoder);
            }
        }
        return this;
    }

    public Bootstrap encode(AbstractEncoder... encoders) {
        for (AbstractEncoder encoder : encoders) {
            if (!this.encoders.contains(encoder)) {
                this.encoders.add(encoder);
            }
        }
        return this;
    }

    public Bootstrap handle(AbstractHandler... handlers) {
        for (AbstractHandler handler : handlers) {
            if (!this.handlers.contains(handler)) {
                this.handlers.add(handler);
            }
        }
        return this;
    }

    public Bootstrap configure(String path, int baudrate, int csize, int parity, int stopbits, int flags) {
        this.path = path;
        this.baudrate = baudrate;
        this.csize = csize;
        this.parity = parity;
        this.stopbits = stopbits;
        this.flags = flags;
        return this;
    }

    public Bootstrap send(Object message) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "cannot encode message: " + message);
        }
        return this;
    }

    public Bootstrap start() throws IOException {
        //创建等待队列
        BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<>(20);
        workgroup = new ThreadPoolExecutor(2, 10, 5, TimeUnit.SECONDS, bqueue);
        serialPort = new SerialPort(new File(path), baudrate, csize, parity, stopbits, flags);
        final InputStream is = serialPort.getInputStream();
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

        return this;
    }

    public void stop() {
        receiveThread.interrupt();
        workgroup.shutdown();
        serialPort.close();
    }

    private void handle(byte[] data) {
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
                if (types.length == 1 && message.getClass().equals(types[0])) {
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
        if (!handled) {
            Log.e(TAG, "Message not handled: data=" + message);
        }
    }
}
