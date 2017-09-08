package tech.yaog.hardwares.serialport.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.EditText;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import tech.yaog.hardwares.serialport.AbstractDecoder;
import tech.yaog.hardwares.serialport.AbstractEncoder;
import tech.yaog.hardwares.serialport.AbstractHandler;
import tech.yaog.hardwares.serialport.Bootstrap;
import tech.yaog.hardwares.serialport.SerialPort;

/**
 * Created by mutoukenji on 17-8-12.
 */

public class Console2Activity extends Activity {

    private Bootstrap bootstrap;
    private EditText mReception;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);

        mReception = findViewById(R.id.EditTextReception);
        handler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = getSharedPreferences("tech.yaog.serialportapi_preferences", MODE_PRIVATE);
        String path = sp.getString("DEVICE", "");
        int baudrate = Integer.parseInt(sp.getString("BAUDRATE", "9600"));
        int csize = Integer.parseInt(sp.getString("CSIZE", SerialPort.CSIZE_8+""));
        int parity = Integer.parseInt(sp.getString("PARITY", SerialPort.PARITY_NONE+""));
        int stopbits = Integer.parseInt(sp.getString("STOPBITS", SerialPort.STOP_BIT_1+""));
        try {
            bootstrap = new Bootstrap()
                    .configure(path, baudrate, csize, parity, stopbits, 0)
                    .decode(new AbstractDecoder<String>() {
                        @Override
                        public String decode(byte[] data) throws Exception {
                            return new String(data, "UTF-8");
                        }
                    })
                    .encode(new AbstractEncoder<String>() {
                        @Override
                        public byte[] encode(String message) throws Exception {
                            return message.getBytes("UTF-8");
                        }
                    })
                    .handle(new AbstractHandler<String>() {
                        @Override
                        public boolean handle(final String message, Bootstrap client) throws Exception {
                            final String re = "recv: "+message;
                            client.send(re);
                            handler.post(new Runnable() {
                                             @Override
                                             public void run() {
                                                 mReception.append(new Date().toString() + " Rx:" + message + "\n");
                                                 mReception.append(new Date().toString() + " Tx:" + re + "\n");
                                             }
                                         });
                            return true;
                        }
                    })
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bootstrap != null) {
            bootstrap.stop();
        }
    }
}
