package model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Keenon on 4/12/14.
 */
public class Suggestion {
    public String reason;
    public Activity activity;

    public JSONObject toJSON() {
        // Send back a result

        JSONObject json = new JSONObject();

        try {
            json.put("reason", reason);
            json.put("suggestion", activity.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public String toString() {
        return toJSON().toString();
    }
}
