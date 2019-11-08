package com.coolweather.webmobilechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.coolweather.webmobilechat.utils.Utils;
import com.coolweather.webmobilechat.websocket.WebSocketClient;

import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private String name = null;
    private WebSocketClient client;
    private Utils utils;

    private final String TAG_SELF = "self", TAG_NEW = "new", TAG_MESSAGE = "message", TAG_EXIT="exit";

    private List<Message> listMessages;
    private MessageAdapter adapter;
    private RecyclerView listViewMessages;

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSend = (Button) findViewById(R.id.btnSend);
        final EditText inputMsg = (EditText) findViewById(R.id.inputMsg);
        listViewMessages = (RecyclerView) findViewById(R.id.list_view_messages);

        utils = new Utils(this);

        Intent i = getIntent();
        name = i.getStringExtra("name");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send message to web socket server
                sendMessageToServer(utils.getSendMessageJSON(inputMsg.getText().toString()));

                // clear the input field
                inputMsg.setText("");
            }
        });

        listMessages = new ArrayList<>();
        adapter = new MessageAdapter(listMessages);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        listViewMessages.setLayoutManager(manager);
        listViewMessages.setAdapter(adapter);

        client = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET + URLEncoder.encode(name)),
                new WebSocketClient.Listener() {
                    @Override
                    public void onConnect() {

                    }

                    @Override
                    public void onMessage(String message) {
                        Log.d(TAG, String.format("Got string message! %s", message));
                        parseMessage(message);
                    }

                    @Override
                    public void onMessage(byte[] data) {
                        Log.d(TAG, String.format("Got binary message!", data));
                        parseMessage(bytesToHex(data));
                    }

                    @Override
                    public void onDisconnect(int code, String reason) {
                        String message = String.format(Locale.CHINA, "Disconnected! " +
                                "Code: %d Reason: %s", code, reason);

                        showToast(message);

                        // clear the session id from shared preferences
                        utils.storeSessionId(null);
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "onError: " + error);
                        showToast(error+"");
                    }
                }, null);

        client.connect();
    }

    // send message to web socket server
    private void sendMessageToServer(String message){
        if(client != null && client.isConnected()){
            client.send(message);
        }
    }

    /**
     *  Parsing the JSON message from the server
     *  The Intent of the message will be identified by JSON node 'flag'
     *  flag = self, message belongs to the server
     *  flag = new, a new person joined the conversation
     *  flag = exit, a person left the conversation
     *  flag = message, message belongs to other person
    **/
    private void parseMessage(final String message){
        try{
            JSONObject jObj = new JSONObject(message);

            String flag = jObj.getString("flag");

            if(flag.equalsIgnoreCase(TAG_SELF)){
                String sessionId = jObj.getString("sessionId");
                utils.storeSessionId(sessionId);
                Log.e(TAG, "parseMessage: you sessionId is" + utils.getSessionId());
            }else if(flag.equalsIgnoreCase(TAG_NEW)){
                String name = jObj.getString("name");
                String nsg = jObj.getString("message");

                String onlineCount = jObj.getString("onlineCount");
                showToast(name + message + ". Currently " + onlineCount + "people online.");
            }else if(flag.equalsIgnoreCase(TAG_MESSAGE)){
                String fromName = name;
                String msg = jObj.getString("message");
                String sessionId = jObj.getString("sessionId");
                boolean isSelf = true;

                if(!sessionId.equals(utils.getSessionId())){
                    fromName = jObj.getString("name");
                    isSelf = false;
                }

                Message m = new Message(fromName, msg, isSelf);
                appendMessage(m);
            }else if(flag.equalsIgnoreCase(TAG_EXIT)){
                String name = jObj.getString("name");
                String msg = jObj.getString("message");
                showToast(name + msg);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client != null && client.isConnected()){
            client.disconnect();
        }
    }

    private void appendMessage(final Message m){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listMessages.add(m);
                adapter.notifyItemInserted(listMessages.size() - 1);
                listViewMessages.scrollToPosition(listMessages.size() - 1);
                playBeep();
            }
        });
    }

    private void showToast(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void playBeep(){
        try{
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone((getApplicationContext()), notification);
            r.play();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String bytesToHex(byte[] bytes){
        char[] hexChars = new char[bytes.length * 2];
        for(int i = 0; i < bytes.length; i++){
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
