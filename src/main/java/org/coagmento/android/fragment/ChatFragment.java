package org.coagmento.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.coagmento.android.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ChatFragment extends Fragment {

    private int project_id = Integer.MIN_VALUE;
    private String email, password;
    private View rootView;

    public ChatFragment() {

    }

    public static ChatFragment newInstance(Bundle userInfo) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(userInfo);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Fragment Opened", "OK");

        Bundle args = getArguments();
        if(project_id == Integer.MIN_VALUE) project_id = args.getInt("project_id");
        email = args.getString("email");
        password = args.getString("password");


    }
}
