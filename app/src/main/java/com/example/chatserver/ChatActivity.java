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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ChatActivity extends AppCompatActivity {

    private static int SERVER_PORT = 6000;
    private ServerThread serverThread;
    private List<Socket> clientList = new ArrayList<Socket>();
    private ServerSocket server = null;
    Handler handler = new Handler();
    private Handler mHandler = null;
    private boolean flag = true;
    private HashMap<String, String> currentMemberIpMap = new HashMap<>();
    String NAME;
    TextView txtServer;
    TextView txtMemberList;
    TextView chat;
    EditText inputMessage;
    Button btnDisconnect;
    Button btnSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        InitViewElement();
        Intent it = this.getIntent();
        if (it != null){
            Bundle bundle = it.getExtras();
            if (bundle!=null){
                NAME = bundle.getString("name");
                serverThread = new ServerThread();
                serverThread.start();
//                txtServer.append(getLocalAddress());
            }
        }

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    flag = false;
                    HashMap<String, String> msgMap = new HashMap<>();
                    msgMap.put("name", NAME);
                    msgMap.put("message", inputMessage.getText().toString());
                    msgMap.put("state", "server-disconnect");
                    SendMessage sendMessage = new SendMessage(msgMap);
                    sendMessage.start();
                    server.close();
                    Intent it = new Intent();
                    it.setClass(ChatActivity.this, MainActivity.class);
                    startActivity(it);
                } catch (IOException ex) {
                    Log.v("Disconnect", ex.toString());
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat.append(NAME + "(" + getLocalAddress() + ") : " + inputMessage.getText().toString() + "\n");
                HashMap<String, String> msgMap = new HashMap<>();
                msgMap.put("name", NAME);
                msgMap.put("message", inputMessage.getText().toString());
                msgMap.put("state", "");
                SendMessage sendMessage = new SendMessage(msgMap);
                sendMessage.start();
                inputMessage.setText("");
            }
        });

//        try {
//            msg = new Message("{\"name\" : \"Kevin\", \"message\":\"Hello!\", \"state\" : \"connect\"}");
//            msg = new Message(msg);
//            Log.d("Message Test", msg.getName());
//            Log.d("Message Test", msg.getMessage());
//            Log.d("Message Test", msg.getJsonString());
//
//        } catch (JSONException e) {
//            Log.v("Message Construct",e.toString());
//            throw new RuntimeException(e);
//        }


    }

    class Message{
        private JSONObject jsonObject = new JSONObject();
        public Message(String strMsg) throws JSONException {
            this.jsonObject = new JSONObject(strMsg);
        }
        public Message(HashMap<String, String> mapMsg) throws JSONException {
            for(String key:mapMsg.keySet()) {
                this.jsonObject.put(key, mapMsg.get(key));
            }
        }
        public Message(Message msg) throws JSONException {
            this.jsonObject = new JSONObject(msg.getJsonString());
        }
        public String getName() throws JSONException {
            return this.jsonObject.getString("name");
        }
        public String getMessage() throws JSONException {
            return this.jsonObject.getString("message");
        }
        public String getState() throws JSONException {
            return this.jsonObject.getString("state");
        }
        public String getJsonString() {
            return this.jsonObject.toString();
        }
    }

    class ServerThread extends Thread {
        @Override
        public void run() {
            try{
                server = new ServerSocket(SERVER_PORT);
            } catch (IOException ex) {
                Log.v("ServerThread", ex.toString());
                return;
            }

            while(flag) {
                try{
                    Socket client = server.accept();
                    if(client != null) {
                        SocketThread socketThread = new SocketThread(client);
                        socketThread.start();
                        clientList.add(client);
                    }

                }catch (IOException ex) {
                    Log.v("ServerThread", ex.toString());
                    break;
                }
            }

        }
    }

    class SocketThread extends Thread {
        private Socket socket;
        private BufferedReader in;
        private boolean isConnected = true;
        public SocketThread(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            } catch (IOException ex) {
                Log.v("Client SocketThread Construct", ex.toString());
            }
        }

        @Override
        public void run(){
            while (isConnected) {
                try{
                    String str = in.readLine();
                    if(str != null){
                        Message msg = new Message(str);
                        Log.d("SocketThread InputStream Read", msg.getJsonString());
                        if (msg.getState().equals("disconnect")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        chat.append(msg.getName() + " left!\n");
                                        UpdateMemberList();
                                        SendMessage sendMessage = new SendMessage(msg);
                                        sendMessage.start();
                                    } catch (JSONException ex) {
                                        Log.v("Show Client Joined", ex.toString());
                                    }
                                }
                            });
                            currentMemberIpMap.remove(msg.getName());
                            clientList.remove(socket);
                            isConnected = false;
                        } else if (msg.getState().equals("connect")) {
                            currentMemberIpMap.put(msg.getName(), this.socket.getRemoteSocketAddress().toString());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        chat.append(msg.getName() + "(" + currentMemberIpMap.get(msg.getName()) + ") joined!\n");
                                        UpdateMemberList();
                                        SendMessage sendMessage = new SendMessage(msg);
                                        sendMessage.start();
                                    } catch (JSONException ex) {
                                        Log.v("Show Client Joined", ex.toString());
                                    }

                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        chat.append(msg.getName() + "(" + currentMemberIpMap.get(msg.getName()) + ") : " + msg.getMessage() + "\n");
                                        SendMessage sendMessage = new SendMessage(msg);
                                        sendMessage.start();
                                    } catch (JSONException ex) {
                                        Log.v("Show Client Joined", ex.toString());
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception ex) {
                    Log.v("SocketThread InputStream Read", ex.toString());
                    break;
                }
            }
        }
    }

    class SendMessage extends Thread {
        Message msg;
        public SendMessage(HashMap<String,String> msgMap){
            try {
                this.msg = new Message(msgMap);
            } catch (Exception ex){
                Log.v("SendMessage", ex.toString());
            }
        }
        public SendMessage(Message msg) {
            try {
                this.msg = new Message(msg);
            } catch (Exception ex) {
                Log.v("SendMessage", ex.toString());
            }
        }

        @Override
        public void run() {
            try {
                for (Socket client: clientList) {
                    OutputStreamWriter outputSW = new OutputStreamWriter(client.getOutputStream());
                    BufferedWriter bufferedWriter = new BufferedWriter(outputSW);
                    bufferedWriter.write(this.msg.getJsonString() + "\n");
                    bufferedWriter.flush();
                    Log.d("SendMessage", this.msg.getJsonString());
                }

            } catch (IOException ex) {
                Log.v("SendMessage", ex.toString());
            }
        }
    }

    public static String getLocalAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference IpAddress",ex.toString());
        }
        return null;
    }
    void UpdateMemberList(){
        txtMemberList.setText("Online Member :\n");
        for (String name:
             currentMemberIpMap.keySet()) {
            txtMemberList.append(name + "\n");
        }
    }
    private void InitViewElement() {
        txtServer = (TextView) findViewById(R.id.txtServer);
        txtMemberList = (TextView) findViewById(R.id.txtMemberList);
        chat = (TextView) findViewById(R.id.chat);
        inputMessage = (EditText) findViewById(R.id.inputMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
    }

}