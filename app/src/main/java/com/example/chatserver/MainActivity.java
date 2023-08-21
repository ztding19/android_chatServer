package com.example.chatserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private static int SERVER_PORT = 6000;
    private List<Socket> clientList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService mExecutorService = null;
    private String hostip;
    Handler handler = new Handler();
    private Handler mHandler = null;
    private boolean flag = true;
    String tmp;
    Socket socket;
    BufferedReader reader;
    OutputStreamWriter outputSW;
    EditText inputName;
    Button btnStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewElement();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", inputName.getText().toString());
                Intent it = new Intent();
                it.putExtras(bundle);
                it.setClass(MainActivity.this, ChatActivity.class);
                startActivity(it);
            }
        });
    }

    /*
    class ServerThread extends Thread{
        @Override
        public void run() {
            //super.run();
            try {
                server = new ServerSocket(SERVER_PORT);
            }catch (IOException ex) {
                Log.v("ServerThread", ex.toString());
                return;
            }
            mExecutorService = Executors.newCachedThreadPool();
            Socket client = null;
            while(flag){
                try {
                    client = server.accept();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            chat.setText("Connected.");
                        }
                    });

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            chat.setText("Set message");
                        }
                    });
                    clientList.add(client);

                } catch (IOException ex){

                }
            }
        }
    }




    class serverThread implements Runnable {
        @Override
        public void run(){
            try {
                ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
//                msg = new MsgObj("Server");
                try {
                    socket = serverSocket.accept();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chat.setText("Connected");
                        }
                    });
                    reader = new BufferedReader(new InputStreamReader((socket.getInputStream())));
                    outputSW = new OutputStreamWriter(socket.getOutputStream());
                    while ((tmp = reader.readLine()) != null){
                        JSONObject jsonObj = new JSONObject(tmp);
                        runOnUiThread(new Runnable() {
                            JSONObject _jsonObj;
                            @Override
                            public void run() {
                                try {
                                    String name = _jsonObj.getString(MsgObj.LABEL_NAME);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }catch (IOException e) {
                    //JSONException
                    e.printStackTrace();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

     */









    private void initViewElement() {
        inputName = (EditText) findViewById(R.id.inputName);
        btnStart = (Button) findViewById(R.id.btnStart);
    }
}

