package tech.yaog.hardwares.serialport.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;

import tech.yaog.hardwares.serialport.AbstractDecoder;
import tech.yaog.hardwares.serialport.AbstractEncoder;
import tech.yaog.hardwares.serialport.AbstractHandler;
import tech.yaog.hardwares.serialport.Bootstrap;
import tech.yaog.hardwares.serialport.SerialPort;

public class PaymentActivity extends Activity implements View.OnClickListener {

    private Bootstrap bootstrap;
    private Handler handler;
    private TextView textView;
    private ScrollView scrollView;
    private Button selectButton;

    private int sel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        textView = findViewById(R.id.textView);
        scrollView = findViewById(R.id.scrollView);
        selectButton = findViewById(R.id.Select);
        selectButton.setOnClickListener(this);
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
                    .decode(new AbstractDecoder<SelectCommand>() {
                        @Override
                        public SelectCommand decode(byte[] data) throws Exception {
                            PayFrame frame = PayFrame.fromBytes(data);
                            if (frame != null && frame.getCommand() == SelectCommand.ID) {
                                return new SelectCommand();
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
                    .handle(new AbstractHandler<SelectCommand>() {
                        @Override
                        public boolean handle(SelectCommand message, Bootstrap client) throws Exception {
                            if (sel > 0) {
                                client.send(new SelectReplyCommand(SelectReplyCommand.RESULT_OK, (byte) 0x00, (byte) sel).toFrame());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.append(new Date().toString());
                                        textView.append(" ");
                                        textView.append("Do Select\n");
                                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                });
                            }
                            else {
                                client.send(new SelectReplyCommand(SelectReplyCommand.RESULT_NG, (byte) 0x00, (byte) 0x00).toFrame());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.append(new Date().toString());
                                        textView.append(" ");
                                        textView.append("Check Select\n");
                                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                });
                            }
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

    @Override
    public void onClick(View view) {
        sel = 5;
    }
}
