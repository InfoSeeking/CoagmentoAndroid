package com.coagmento.mobile;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.ChatListResponse;
import com.coagmento.mobile.models.CreateChatMessageResponse;
import com.coagmento.mobile.models.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.devcenter.android.ChatView;
import co.devcenter.android.models.ChatMessage;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ChatActivity extends AppCompatActivity {

    private String host, email, password;
    private int project_id;
    private String permissionLevel, title;
    private Bundle userInfo;
    private Retrofit retrofit;
    private EndpointsInterface apiService;
    private ChatView chatView;
    private Coagmento app;
    private Socket socket;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        app = (Coagmento) getApplication();
        socket = app.getSocket();

        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        chatView = (ChatView) findViewById(R.id.chat_view);

        userInfo = getIntent().getExtras();

        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");
        project_id = userInfo.getInt("project_id");
        title = userInfo.getString("project_title");
        permissionLevel = userInfo.getString("project_permission");

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Group Chat (" + title + ")");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(EndpointsInterface.class);

        chatView.setChatListener(new ChatView.ChatListener() {

            @Override
            public void userIsTyping() {

            }

            @Override
            public void userHasStoppedTyping() {

            }

            @Override
            public void onMessageReceived(String message, long timestamp) {
            }

            @Override
            public boolean sendMessage(String message, long timestamp) {
                final EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

                Call<CreateChatMessageResponse> sendMessage = apiService.sendMessage(message, project_id, email, password);

                Response<CreateChatMessageResponse> response = null;

                try {
                    response = sendMessage.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(response == null) {
                    return false;
                } else {
                    if(response.code() == 200) {
                        return true;
                    } else {
                        Log.e("Chat Message Error", "HTTP Error Code: " + String.valueOf(response.code()));
                        return false;
                    }
                }
            }
        });

        populateMessageView();

        if(permissionLevel.equals("r")) {
            chatView.getInputEditText().setHint("Users with READ permission levels cannot send messages.");
            chatView.getInputEditText().setEnabled(false);
        } else {
            chatView.getInputEditText().setHint("Type your message");
            chatView.getInputEditText().setEnabled(true);
        }

        try {
            socket.emit("subscribe", new JSONObject("{ projectID:" + String.valueOf(project_id) + " }"));
            socket.on("data", onData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.connect();

    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean x = super.onNavigateUp();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
        return x;
    }

    @Override
    public void onBackPressed() {
        super.onNavigateUp();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    public void populateMessageView() {

        chatView.removeAllMessages();

        Call<ChatListResponse> getChatMessages = apiService.getChatMessages(project_id, email, password);

        ChatListResponse response = null;
        try {
            response = getChatMessages.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(response == null) return;

        for(Result message : response.getResult()) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date date = formatter.parse(message.getcreated_at());
                ChatMessage.Type type;
                if(message.getUser().getEmail().equals(email)) {
                    type = ChatMessage.Type.SENT;
                } else {
                    type = ChatMessage.Type.RECEIVED;
                }
                chatView.newMessage(new ChatMessage(message.getUser().getName(), message.getMessage(), date.getTime(), type));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        socket.disconnect();

        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off("data", onData);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = true;
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            new Error("Socket failed to connect.").printStackTrace();
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = false;
        }
    };

    private Emitter.Listener onData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(args.length > 0) {
                JSONObject response = (JSONObject) args[0];
                try {
                    final String sender = response.getJSONArray("data").getJSONObject(0).getJSONObject("user").getString("name");

                    final String message = response.getJSONArray("data").getJSONObject(0).getString("message");

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = formatter.parse(response.getJSONArray("data").getJSONObject(0).getString("created_at"));
                    final long time = date.getTime();

                    final ChatMessage.Type type =
                            (response.getJSONArray("data").getJSONObject(0).getJSONObject("user").getString("email").equals(email) ? ChatMessage.Type.SENT : ChatMessage.Type.RECEIVED);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatView.newMessage(new ChatMessage(sender, message, time, type));
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
