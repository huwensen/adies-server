package com.xst.aedis;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author joker
 */
public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            et_console.append(msg.obj.toString());
        }
    };

    EditText et_console;
    boolean flag;
    SocketService server;
    /**
     * 如果这个变量是static修饰的
     * 那么当应用onDestroy之后也不会销毁
     */
    volatile boolean runWrite = true;
    Button viewById;
    int i = 0;
    final String TAG = "Redis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.btn_start);
        et_console = findViewById(R.id.et_console);
        et_console.setMovementMethod(ScrollingMovementMethod.getInstance());
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        setOutPutStream();
        start();
    }

    private void start() {
        if (!flag) {
            i++;
            flag = true;
            server = new SocketService(8088);
            server.start();
            server.setName("service thread#" + i);
            System.out.println("服务已启动");
            viewById.setText("stop");
            System.out.println("active thread count: " + Thread.activeCount());
            Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
            Set<Thread> threads = allStackTraces.keySet();
            Iterator<Thread> iterator = threads.iterator();
            while (iterator.hasNext()) {
                Thread next = iterator.next();
                System.out.println(next.getId() + ":" + next.getName() + ":" + next.getState());
            }
        } else {
            flag = false;
            server.close();
            server = null;
            viewById.setText("start");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        server.close();
        runWrite = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    final PipedInputStream pipedInputStream = new PipedInputStream();
    PipedOutputStream pipedOutputStream;
    Thread printerThread;

    private void setOutPutStream() {
        Log.i(TAG, "setOutPutStream");
        Log.i(TAG, "printerThread:" + printerThread);
        try {
            pipedOutputStream = new PipedOutputStream(pipedInputStream);
            System.setOut(new PrintStream(pipedOutputStream, true));
            byte[] bytes = new byte[1024];
            printerThread = new Thread(() -> {
                Log.i(TAG, "++++++++++++++++++++" + runWrite);
                while (runWrite) {
                    try {
                        int len;
                        while ((len = pipedInputStream.read(bytes)) > 0) {
                            Log.i(TAG, "++++++++++++++++++++");
                            String str = new String(bytes, 0, len);
                            handler.obtainMessage(1, str).sendToTarget();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println();
                    }
                }
            });
            Log.i(TAG, "printerThread:" + printerThread);
            printerThread.setName("printer thread");
            printerThread.start();
            Log.i(TAG, "printerThread:" + printerThread.getState() + "," + printerThread.isAlive());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
