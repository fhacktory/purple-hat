package purplehat.fr.purplehat;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Slave {

    public interface Listener {
        public void emit(JSONObject data);
    }

    private static final String LOG_TAG = "MASTER_CLIENT";
    private final WebSocketClient client;
    private Map<String, Collection<Listener>> listeners;

    public Slave(String address) {
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
                        Collection<Listener> lstns = listeners.get(obj.getString("action"));
                        if (lstns != null) {
                            for (Listener lstn : lstns) {
                                lstn.emit(obj);
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
        listeners = new HashMap<String, Collection<Listener>>();

        addListener("views changes", new Listener() {
            @Override
            public void emit(JSONObject data) {
                Log.d(LOG_TAG, "views changed" + data);
            }
        });

        addListener("white hit", new Listener() {
            @Override
            public void emit(JSONObject data) {
                Log.d(LOG_TAG, "white hit" + data);
            }
        });
    }

    public void addListener(String action, Listener lstn) {
        Collection<Listener> lstns = listeners.get(action);
        if (lstns == null) {
            lstns = new ArrayList<Listener>();
            listeners.put(action, lstns);
        }
        lstns.add(lstn);
    }

    // TODO handle when not connected
    public void send(JSONObject obj) {
        client.send(obj.toString());
    }
}
