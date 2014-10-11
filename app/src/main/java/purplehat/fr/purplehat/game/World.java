package purplehat.fr.purplehat.game;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by jmcomets on 11/10/14.
 */
public class World {
    private static final String LOG_TAG = "WORLD";

    private Collection<Ball> balls;
    private Collection<Rect2<Integer>> rects;

    public World() {
        balls = new ArrayList<Ball>();
        rects = new ArrayList<Rect2<Integer>>();

        Ball ball = new Ball();
        ball.setPosition(new Vector2<Integer>(300, 400));
        ball.setRadius(200);
        balls.add(ball);
    }

    public Collection<Ball> getBalls() {
        return balls;
    }

    public Collection<Rect2<Integer>> getRects() {
        return rects;
    }

    public void updateFromJson(JSONObject data) {
        // Rects
        try {
            JSONArray rectList = data.getJSONArray("rects");
            rects = new ArrayList<Rect2<Integer>>();
            for (int i = 0; i < rectList.length(); i++) {
                JSONObject rect = rectList.getJSONObject(i);
                rects.add(new Rect2<Integer>(
                        rect.getInt("x1"), rect.getInt("y1"),
                        rect.getInt("x2"), rect.getInt("y2")
                ));
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "rects is badly formed");
        }

        // Balls
        try {
            JSONArray ballList = data.getJSONArray("balls");
            balls = new ArrayList<Ball>();
            for (int i = 0; i < ballList.length(); i++) {
                JSONObject ballData = ballList.getJSONObject(i);
                Ball ball = new Ball();
                ball.setRadius(ballData.getInt("radius"));
                JSONObject ballPosData = ballData.getJSONObject("position");
                ball.setPosition(new Vector2<Integer>(
                        ballPosData.getInt("x"), ballPosData.getInt("y")
                ));
                balls.add(ball);
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "balls is badly formed");
        }


    }
}
