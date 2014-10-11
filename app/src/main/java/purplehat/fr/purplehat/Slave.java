package purplehat.fr.purplehat;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Slave {

    public interface Listener {
        public void notify(JSONObject data);
    }

    private static final String LOG_TAG = "MASTER_CLIENT";
    private WebSocketClient client;
    private Map<String, Collection<Listener>> allListeners;

    public Slave() {
        allListeners = new HashMap<String, Collection<Listener>>();
    }

    public void addListener(String action, Listener listener) {
        Collection<Listener> listeners = allListeners.get(action);
        if (listeners == null) {
            listeners = new ArrayList<Listener>();
            allListeners.put(action, listeners);
        }
        listeners.add(listener);
    }

    public boolean isConnected() {
        return client != null && client.getConnection().isOpen();
    }

    public void connect(String address) {
        URI uri = URI.create("ws://" + address);
        client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
            }

            @Override
            public void onMessage(String message) {
                try {
                    JSONObject obj = new JSONObject(message);
                    try {
                        Collection<Listener> listeners = allListeners.get(obj.getString("action"));
                        if (listeners != null) {
                            for (Listener listener : listeners) {
                                listener.notify(obj);
                            }
                        }
                    } catch (JSONException e) {
                        Log.w(LOG_TAG, "no action given");
                    }
                } catch (JSONException e) {
                    Log.w(LOG_TAG, "unhandled non-json message");
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onError(Exception ex) {
            }
        };
    }

    // TODO handle when not connected
    public void send(JSONObject obj) {
        if (isConnected()) {
            client.send(obj.toString());
        } else {
            Log.w(LOG_TAG, "cannot send, client not connected");
        }
    }

    public void close() {
        if (isConnected()) {
            client.close();
        }
    }
}
