package purplehat.fr.purplehat;

import org.json.JSONException;
import org.json.JSONObject;

import purplehat.fr.purplehat.game.Ball;
import purplehat.fr.purplehat.game.Vector2;

/**
 * Created by vcaen on 12/10/2014.
 */
public class Action {

    Double init_x;
    Double init_y;

    Double mmPerMilis;
    Double direction_x;
    Double direction_y;

    public Action(Double init_x, Double init_y, Double mmPerMilis, Double direction_x, Double direction_y) {
        this.init_x = init_x;
        this.init_y = init_y;
        this.mmPerMilis = mmPerMilis;
        this.direction_x = direction_x;
        this.direction_y = direction_y;
    }

    public JSONObject getJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("init_x", init_x);
            json.put("init_y", init_y);
            json.put("mmPerMilis", mmPerMilis);
            json.put("direction_x", direction_x);
            json.put("direction_y", direction_y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static Action parseJson(JSONObject json) {
        try {
            return  new Action(
            json.getDouble("direction_y"),
            json.getDouble("init_x"),
            json.getDouble("init_y"),
            json.getDouble("mmPerMilis"),
            json.getDouble("direction_x"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public Ball getBall() {
        Ball ball = new Ball();
        ball.setPosition(new Vector2<Double>(init_x,init_y));
        ball.setRadius(10.0);
        ball.setVelocity(new Vector2<Double>(direction_x, direction_y));
        return ball;
    }

}
