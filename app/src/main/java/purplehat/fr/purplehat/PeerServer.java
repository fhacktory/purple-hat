package purplehat.fr.purplehat;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;

/**
 * Created by jmcomets on 11/10/14.
 */
public class PeerServer extends WebSocketServer {
    public PeerServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // TODO gérer les connexions
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // TODO gérer les déconnexions
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject msg = new JSONObject(message);
            String action = msg.getString("action");
            if (action != null) {
                System.out.println("remote '" + conn.getRemoteSocketAddress().getHostName()
                        + "', action: '" + action + "'");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // TODO gérer les erreurs
    }
}
