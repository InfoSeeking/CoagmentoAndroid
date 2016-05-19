package com.coagmento.mobile.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.coagmento.mobile.R;
import com.coagmento.mobile.data.EndpointsInterface;
import com.coagmento.mobile.models.ChatListResponse;
import com.coagmento.mobile.models.CreateChatMessageResponse;
import com.coagmento.mobile.models.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.devcenter.android.ChatView;
import co.devcenter.android.models.ChatMessage;
import io.socket.client.IO;
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

    private ChatView chatView;

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

        chatView = (ChatView) rootView.findViewById(R.id.chat_view);

        userInfo = getArguments();

        host = userInfo.getString("host");
        email = userInfo.getString("email");
        password = userInfo.getString("password");
        project_id = userInfo.getInt("project_id");


        // Retrieve previous messages
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final EndpointsInterface apiService = retrofit.create(EndpointsInterface.class);

        Call<ChatListResponse> getChatMessages = apiService.getChatMessages(project_id, email, password);

        getChatMessages.enqueue(new Callback<ChatListResponse>() {
            @Override
            public void onResponse(Response<ChatListResponse> response, Retrofit retrofit) {
                if(response.code() == 200) {
                    populateMessageView(response.body());
                } else {
                    Log.e("HTTP Error: ", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Error", t.getMessage());
            }
        });

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
                Call<CreateChatMessageResponse> sendMessage = apiService.sendMessage(message, project_id, email, password);
                sendMessage.enqueue(new Callback<CreateChatMessageResponse>() {
                    @Override
                    public void onResponse(Response<CreateChatMessageResponse> response, Retrofit retrofit) {
                        if(response.code() == 200) {

                        } else {
                            Log.e("HTTP Error", String.valueOf(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Error", t.getMessage());
                    }
                });
                return false;
            }
        });

        initializeSocket(project_id);

        return rootView;
    }

    private void populateMessageView(ChatListResponse response) {
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
                chatView.newMessage(new ChatMessage(message.getMessage(), date.getTime(), type));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void initializeSocket(final int id) {
        try {

            Log.i("SOCKET", "trying connection");
            final Socket socket = IO.socket("http://new.coagmento.org:8000");

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i("SOCKET", "connected");
                    try {
                        socket.emit("subscribe", new JSONObject("{ projectID:" + String.valueOf(id) + " }"));
                        Log.i("SOCKET", "emmited subscirbe");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i("SOCKET", "Error connecting");
                }
            });

            socket.on("data", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if(args.length > 0) {
                        Log.i("SOCKET", "response: " + args[0]);
                    }
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
