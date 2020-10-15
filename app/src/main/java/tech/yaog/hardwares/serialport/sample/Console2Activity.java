package tech.yaog.hardwares.serialport.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.EditText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import tech.yaog.hardwares.serialport.Bootstrap;
import tech.yaog.hardwares.serialport.SerialPort;
import tech.yaog.utils.aioclient.AbstractHandler;
import tech.yaog.utils.aioclient.StringDecoder;
import tech.yaog.utils.aioclient.encoder.StringEncoder;

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
        bootstrap = new Bootstrap()
                .configure(path, baudrate, csize, parity, stopbits, 0)
                .decoders(new StringDecoder(Charset.forName("UTF-8")))
                .encoders(new StringEncoder(Charset.forName("UTF-8")))
                .handlers(new AbstractHandler<String>() {
                    @Override
                    public boolean handle(final String msg) {
                        final String re = "recv: "+msg;
                        bootstrap.send(re);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mReception.append(new Date().toString() + " Rx:" + msg + "\n");
                                mReception.append(new Date().toString() + " Tx:" + re + "\n");
                            }
                        });
                        return true;
                    }
                })
                .onEvent(new tech.yaog.utils.aioclient.Bootstrap.Event() {
                    @Override
                    public void onConnected() {

                    }

                    @Override
                    public void onDisconnected() {

                    }

                    @Override
                    public void onSent() {

                    }

                    @Override
                    public void onReceived() {

                    }
                })
                .start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bootstrap != null) {
            bootstrap.disconnect();
        }
    }
}
