package model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Keenon on 4/12/14.
 */
public class LocationData {
    public double lat;
    public double lng;
    public JSONObject data;

    public JSONObject toJSON() throws JSONException {
        data.put("lat",lat);
        data.put("lng",lng);
        return data;
    }
}
