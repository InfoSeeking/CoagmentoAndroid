package com.coagmento.mobile;

import android.app.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by yash on 6/2/16.
 */
public class Coagmento extends Application {

    private Manager manager;
    private Socket socket;
    {
        try {
            manager = new Manager(new URI("http://new.coagmento.org:8000"));
            socket = manager.socket("/feed");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

}
