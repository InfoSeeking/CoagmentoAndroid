package com.coagmento.mobile.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coagmento.mobile.Coagmento;
import com.coagmento.mobile.R;
import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.ChatListResponse;
import com.coagmento.mobile.models.CreateChatMessageResponse;
import com.coagmento.mobile.models.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.devcenter.android.ChatView;
import co.devcenter.android.models.ChatMessage;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private String host, email, password;
    private int project_id;
    private Bundle userInfo;
    private Retrofit retrofit;
    private EndpointsInterface apiService;
    private ChatView chatView;
    private FragmentActivity context;
    private Coagmento app;
    private Socket socket;
    private boolean isConnected = false;

    public ChatFragment() {
        // Required empty public constructor
    }

    //TODO: Implement method to update when project is changed
    public static ChatFragment newInstance(Bundle userInfo) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(userInfo);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        context = getActivity();
        app = (Coagmento) context.getApplication();
        socket = app.getSocket();

        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        chatView = (ChatView) rootView.findViewById(R.id.chat_view);

        userInfo = getArguments();

        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");
        project_id = userInfo.getInt("project_id");

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
        initializeSocket(project_id);

        return rootView;
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

    public void initializeSocket(int project_id) {

        // disconnect from previous room if socket is already connected
        if(isConnected) socket.disconnect();

        try {
            socket.emit("subscribe", new JSONObject("{ projectID:" + String.valueOf(project_id) + " }"));
            socket.on("data", onData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.connect();

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

                    context.runOnUiThread(new Runnable() {
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
