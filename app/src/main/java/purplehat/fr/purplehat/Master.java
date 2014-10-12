package purplehat.fr.purplehat;

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

import purplehat.fr.purplehat.game.Ball;
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

    public Map<String, PhysicalScreen> getScreenMap() {
        return screenMap;
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
                try {
                    JSONObject obj = new JSONObject(message);
                    try {
                        String action = obj.getString("action");
                        if (action.equals("create ball")) {
                            FullscreenActivity.getInstance().addBallInWorld(Action.parseJson(obj).getBall());
                            broadcast(obj);
                        }
                    } catch (JSONException e) {
                        Log.w(LOG_TAG, "no action given");
                    }
                } catch (JSONException e) {
                    Log.w(LOG_TAG, "unhandled non-json message");
                }
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
        try {
            JSONObject data = new JSONObject();
            data.put("action", "world:balls");
            JSONArray ballList = new JSONArray();
            for (Ball ball : FullscreenActivity.getInstance().getWorld().getBalls()) {
                JSONObject ballData = new JSONObject();
                ballData.put("x", ball.getPosition().getX());
                ballData.put("y", ball.getPosition().getY());
                ballData.put("vx", ball.getVelocity().getX());
                ballData.put("vx", ball.getVelocity().getY());
                ballData.put("r", ball.getRadius());
                ballList.put(ballData);
            }
            data.put("balls", ballList);
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

    public String getScreenId() {
        return screenId;
    }
}
