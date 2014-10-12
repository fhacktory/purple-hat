package purplehat.fr.purplehat;

import org.json.JSONException;
import org.json.JSONObject;

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

}
