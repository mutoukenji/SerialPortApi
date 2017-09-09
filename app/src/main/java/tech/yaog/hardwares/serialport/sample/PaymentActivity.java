package tech.yaog.hardwares.serialport.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;

import tech.yaog.hardwares.serialport.AbstractDecoder;
import tech.yaog.hardwares.serialport.AbstractEncoder;
import tech.yaog.hardwares.serialport.AbstractHandler;
import tech.yaog.hardwares.serialport.Bootstrap;
import tech.yaog.hardwares.serialport.SerialPort;

public class PaymentActivity extends Activity {

    private Bootstrap bootstrap;
    private Handler handler;
    private TextView textView;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        textView = findViewById(R.id.textView);
        scrollView = findViewById(R.id.scrollView);
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
                    .decode(new AbstractDecoder<HelloCommand>() {
                        @Override
                        public HelloCommand decode(byte[] data) throws Exception {
                            PayFrame frame = PayFrame.fromBytes(data);
                            if (frame != null && frame.getCommand() == HelloCommand.ID) {
                                return new HelloCommand();
                            }
                            return null;
                        }
                    })
                    .encode(new AbstractEncoder<PayReplyFrame>() {
                        @Override
                        public byte[] encode(PayReplyFrame message) throws Exception {
                            return message.toBytes();
                        }
                    })
                    .handle(new AbstractHandler<HelloCommand>() {
                        @Override
                        public boolean handle(HelloCommand message, Bootstrap client) throws Exception {
                            client.send(new HelloReplyCommand("0000", HelloReplyCommand.RESULT_OK).toFrame());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textView.append(new Date().toString());
                                    textView.append(" ");
                                    textView.append("Hello\n");
                                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
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
