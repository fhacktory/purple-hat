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

import purplehat.fr.purplehat.game.Vector2;
import purplehat.fr.purplehat.screen.ScreenUtilitiesService;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Master {
    private static final String LOG_TAG = "MASTER_CLIENT";

    private PhysicalScreen baseScreen;
    private String screenId;

    public PhysicalScreen getScreen(String id) {
        return screenMap.get(id);
    }

    private Map<String, PhysicalScreen> screenMap;
    private WebSocketServer server;

    public void start() {
        Log.d(LOG_TAG, "master websocket server started");
        server.start();
    }

    public Master(int port, String screenId, PhysicalScreen baseScreen) {
        if (baseScreen == null) {
            baseScreen = new PhysicalScreen(0, 0, 0, 0);
            Vector2<Double> p = ScreenUtilitiesService.pixel2mm(ScreenUtilitiesService.getDisplayCenter());
            baseScreen.setX2(p.getX() * 2);
            baseScreen.setY2(p.getY() * 2);
        }
        this.baseScreen = baseScreen;
        this.screenId = screenId;
        screenMap = new HashMap<String, PhysicalScreen>();
        screenMap.put(screenId, baseScreen);
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                Log.d(LOG_TAG, "connection opened");
                // TODO send position
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
        // broadcastWorld();
    }

    public void broadcastWorld() {
        /*try {
            JSONObject data = new JSONObject();
            data.put("action", "world:virtual:updated");
            JSONArray screenList = new JSONArray();
            Double xMin = 0.0;
            Double xMax = 0.0;
            Double yMin = 0.0;
            Double yMax = 0.0;
            for (PhysicalScreen screen : screenMap.values()) {
                xMin = Math.min(xMin, screen.getX1());
                xMin = Math.min(xMin, screen.getX2());
                xMax = Math.max(xMax, screen.getX1());
                xMax = Math.max(xMax, screen.getX2());
                yMin = Math.min(yMin, screen.getY1());
                yMin = Math.min(yMin, screen.getY2());
                yMax = Math.max(yMax, screen.getY1());
                yMax = Math.max(yMax, screen.getY2());
            }
            for (Map.Entry<String, PhysicalScreen> entry : screenMap.entrySet()) {
                PhysicalScreen screen = entry.getValue();
                JSONObject screenData = new JSONObject();
                screenData.put("id", entry.getKey());
                screenData.put("x1", (screen.getX1() - xMin) / (xMax - xMin));
                screenData.put("y1", (screen.getY1() - yMin) / (yMax - yMin));
                screenData.put("x2", (screen.getX2() - xMin) / (xMax - xMin));
                screenData.put("y2", (screen.getY2() - yMin) / (yMax - yMin));
                screenList.put(screenData);
            }
            broadcast(data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "new screen json couldn't be constructed");
        }*/
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

    public String getScreenId() {
        return screenId;
    }
}
