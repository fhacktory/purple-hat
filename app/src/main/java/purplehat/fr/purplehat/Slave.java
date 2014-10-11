package purplehat.fr.purplehat;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Slave extends WebSocketClient {

    private static final String LOG_TAG = "MASTER_CLIENT";

    public Slave(String address) {
        super(URI.create("ws://" + address));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            onJsonMessage(obj);
        } catch (JSONException e) {
            Log.w(LOG_TAG, "unhandled non-json message");
        }
    }

    private void onJsonMessage(JSONObject obj) {
        try {
            String action = obj.getString("action");
            if (action.equals(SlaveActions.VIEWS_CHANGED)) {
                viewsChanged(obj);
            } else if (action.equals(SlaveActions.HIT_WHITE)) {
                hitWhite(obj);
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "no action given");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
    }

    @Override
    public void onError(Exception ex) {
    }

    private void hitWhite(JSONObject obj) {
        Log.d(LOG_TAG, "white hit:" + obj);
    }

    private void viewsChanged(JSONObject obj) {
        Log.d(LOG_TAG, "views changed" + obj);
    }
}
