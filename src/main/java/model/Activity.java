package model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Activity {
	
	public int id;
	public String title;
	public String theme;
	public String address;
	public double latitude;
	public double longitude;
	public ArrayList<String> categories = new ArrayList<String>();
	public String phoneNumber;
	public String website;
	public JSONObject metadata;

    // Scratch variables for SuggestionEngine
    public int userRating = -1;
    public double predictedRating = -1;
    public int shows = 0;
    public double distanceFromQuery = 0;
    public double cost = 0;
    public LocationData nearestLocationData = null;
	
	public Activity() { }

	public Activity(String title, int id, String theme, String address, double latitude, double longitude, ArrayList<String> categories, String website, String phoneNumber, String metadata) {
		this.title = title;
		this.id = id;
		this.theme = theme;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.categories = categories;
		this.website = website;
		this.phoneNumber = phoneNumber;
        try {
            this.metadata = new JSONObject(metadata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title",title);
        json.put("theme",theme);
        json.put("address",address);
        json.put("lat",latitude);
        json.put("lng",longitude);
        JSONArray categoriesJSON = new JSONArray();
        for (String category : categories) categoriesJSON.put(category);
        json.put("categories",categoriesJSON);
        json.put("phoneNumber",phoneNumber);
        json.put("website",website);
        Iterator<String> keys = metadata.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            json.put(key,metadata.get(key));
        }
        if (this.nearestLocationData != null) {
            json.put("location_data",this.nearestLocationData.toJSON());
        }
        return json;
    }
}
