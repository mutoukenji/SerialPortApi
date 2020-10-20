package tech.yaog.hardwares.serialport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import tech.yaog.utils.aioclient.io.IO;

public class SerialPortIO extends IO {

    private SerialPort serialPort;
    private Thread receiveThread;

    public SerialPortIO(Callback callback) {
        super(callback);
    }

    @Override
    public boolean connect(String remote) {
        String[] blocks = remote.split(":");
        if (blocks.length >= 7) {
            String path = blocks[0];
            int baudrate = Integer.parseInt(blocks[1]);
            int csize = Integer.parseInt(blocks[2]);
            int stopbit = Integer.parseInt(blocks[3]);
            int parity = Integer.parseInt(blocks[4]);
            int flowcontrol = Integer.parseInt(blocks[5]);
            int flags = Integer.parseInt(blocks[6]);

            try {
                serialPort = new SerialPort(new File(path), baudrate, csize, parity, stopbit, flowcontrol == 1, flowcontrol == 2, flags);
                callback.onConnected();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public void disconnect() {
        serialPort.close();
    }

    @Override
    public void beginRead() {
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = serialPort.getInputStream();
                byte[] buffer = new byte[1024 * 1024];
                while (!Thread.interrupted()) {
                    try {
                        int read = is.read(buffer);
                        if (read > 0) {
                            byte[] data = Arrays.copyOf(buffer, read);
                            if (data.length > 0) {
                                if (Config.vvv && Config.logger != null) {
                                    Config.logger.v("SerialPortIO", "Rx: %s", bytesToHex(data));
                                }
                                callback.onReceived(data);
                            }
                        }
                        else if (read < 0) {
                            callback.onDisconnected();
                            break;
                        }
                    } catch (IOException e) {
                        callback.onDisconnected();
                        break;
                    }
                }
            }
        });
        receiveThread.setName("Serial Receiver");
        receiveThread.setPriority(Thread.MAX_PRIORITY);
        receiveThread.start();
    }

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

    @Override
    public void stopRead() {
        receiveThread.interrupt();
    }

    @Override
    public void write(byte[] bytes) {
        try {
            OutputStream os = serialPort.getOutputStream();
            os.write(bytes);
            os.flush();
            if (Config.vvv && Config.logger != null) {
                Config.logger.v("SerialPortIO", "Tx: %s", bytesToHex(bytes));
            }
        } catch (IOException e) {
            callback.onException(e);
        }
    }
}