package purplehat.fr.purplehat;

import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import purplehat.fr.purplehat.screen.ScreenUtilitiesService;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Master {
    private static final String LOG_TAG = "MASTER_CLIENT";

    private PhysicalScreen baseScreen;

    public PhysicalScreen getScreen(String id) {
        return screenMap.get(id);
    }

    private Map<String, PhysicalScreen> screenMap;
    private WebSocketServer server;

    public Master(int port, String screenId, PhysicalScreen baseScreen) {
        if (baseScreen == null) {
            baseScreen = new PhysicalScreen(0, 0, 0, 0);
            Point p = ScreenUtilitiesService.pixel2mm(ScreenUtilitiesService.getDisplayCenter());
            baseScreen.setX2(p.x * 2);
            baseScreen.setY2(p.y * 2);
        }
        this.baseScreen = baseScreen;
        screenMap = new HashMap<String, PhysicalScreen>();
        screenMap.put(screenId, baseScreen);
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                Log.d(LOG_TAG, "connection opened");
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                Log.d(LOG_TAG, "connection closed");
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                Log.e(LOG_TAG, "error", ex);
            }
        };
    }

    public void addSlaveScreen(String screenId, PhysicalScreen slaveScreen) {
        Log.d(LOG_TAG, "Nombre de client : " + String.valueOf(screenMap.size()));
        screenMap.put(screenId, slaveScreen);
        broadcastWorld();
    }

    public void broadcastWorld() {
        try {
            JSONObject data = new JSONObject();
            data.put("action", "view changed");
            JSONArray screenList = new JSONArray();
            for (PhysicalScreen screen : screenMap.values()) {
                JSONObject screenData = new JSONObject();
                screenData.put("x1", screen.getX1());
                screenData.put("y1", screen.getY1());
                screenData.put("x2", screen.getX2());
                screenData.put("y2", screen.getY2());
                screenList.put(screenData);
            }
            data.put("rects", screenList);
            broadcast(data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "new screen json couldn't be constructed");
        }
    }

    public void broadcast(JSONObject obj) {
        broadcast(obj.toString());
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text
     *            The String to send across the network.
     * @throws InterruptedException
     *             When socket related I/O errors occur.
     */
    private void broadcast(String text) {
        Collection<WebSocket> con = server.connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }

    public void stop() throws IOException, InterruptedException {
        server.stop();
    }
}
