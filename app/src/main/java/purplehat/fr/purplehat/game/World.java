package purplehat.fr.purplehat.game;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by jmcomets on 11/10/14.
 */
public class World {
    private static final String LOG_TAG = "WORLD";

    private Collection<Ball> balls;

    public World() {
        balls = new ArrayList<Ball>();

        for (int i = 0; i < 10; i++) {
            Ball ball = new Ball();
            ball.setPosition(new Vector2<Double>(30.0, 40.0));
            ball.setRadius(1.0);
            balls.add(ball);
        }
    }

    public Collection<Ball> getBalls() {
        return balls;
    }

    public void updateFromJson(JSONObject data) {
        // Balls
        try {
            JSONArray ballList = data.getJSONArray("balls");
            balls = new ArrayList<Ball>();
            for (int i = 0; i < ballList.length(); i++) {
                JSONObject ballData = ballList.getJSONObject(i);
                Ball ball = new Ball();
                ball.setRadius(ballData.getInt("radius"));
                JSONObject ballPosData = ballData.getJSONObject("position");
                ball.setPosition(new Vector2<Double>(
                        ballPosData.getDouble("x"), ballPosData.getDouble("y")
                ));
                balls.add(ball);
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "balls is badly formed");
        }


    }
}
