package db;

import com.mongodb.*;
import model.Activity;
import model.Event;
import model.LocationData;
import model.User;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DAL {

	/* Private instance variables */
	public static DBCollection collUsers;
    private static DBCollection collUsersActivitiesLog;
    private static DBCollection collUsersActivitiesPredicted;
    private static DBCollection collUsersActivitiesSeen;
    private static DBCollection collLatLngMetadata;
    private static DBCollection collActivities;

	/* Constants */
	public static final double EARTH_CIRCUMFERENCE = 24901;

    static {
        DBConnection dbUsers = new DBConnection("users");
        collUsers = dbUsers.getCollection();

        DBConnection dbUsersActivitiesLog = new DBConnection("users_activities_log");
        collUsersActivitiesLog = dbUsers.getCollection();

        DBConnection dbUsersActivitiesPredicted = new DBConnection("users_activities_predicted");
        collUsersActivitiesPredicted = dbUsersActivitiesPredicted.getCollection();

        DBConnection dbUsersActivitiesSeen = new DBConnection("users_activities_seen");
        collUsersActivitiesSeen = dbUsersActivitiesSeen.getCollection();

        DBConnection dbLatLngMetadata = new DBConnection("lat_lng_metadata");
        collLatLngMetadata = dbLatLngMetadata.getCollection();

        DBConnection dbActivities = new DBConnection("activities");
        collActivities = dbActivities.getCollection();
    }

	public static boolean accountExists(String loginName) {
        DBCursor cursor = collUsers.find(new BasicDBObject("username", loginName));
        return (cursor.hasNext()) ? true : false; // If there exists a record with this loginName in the database return true
	}

    public static boolean deviceExists(String deviceId) {
        DBCursor cursor = collUsers.find(new BasicDBObject("deviceid", deviceId));
        return (cursor.hasNext()) ? true : false; //If there exists a record with this loginName in the database return true
    }

	public static boolean isPasswordForAccount(String loginName, String hashOfAttemptedPassword) {
        DBCursor cursor = collUsers.find(new BasicDBObject("username", loginName));
        DBObject document = cursor.next(); // Get first (and only) user with this loginName
        String databasePasswordHash = (String)document.get("salt");
        if (hashOfAttemptedPassword.equals(databasePasswordHash)) {
            return true;
        }
        return false;
	}

    public static Map<Integer,Integer> getUserRankings(int uid) {
        DBCursor cursor = collUsersActivitiesLog.find(new BasicDBObject("user_id", uid));
        Map<Integer,Integer> rankings = new HashMap<Integer, Integer>();
        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            rankings.put((Integer)document.get("activity_id"), (Integer)document.get("rating"));
        }
        return rankings;
    }

    public static Map<Integer,Double> getPredictedUserRankings(int uid) {
        DBCursor cursor = collUsersActivitiesPredicted.find(new BasicDBObject("user_id", uid));
        Map<Integer,Double> rankings = new HashMap<Integer, Double>();
        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            rankings.put((Integer)document.get("activity_id"), (Double)document.get("rating"));
        }
        return rankings;
    }

    public static Map<Integer,Integer> getShowCount(int uid) {
        DBCursor cursor = collUsersActivitiesSeen.find(new BasicDBObject("user_id", uid));
        Map<Integer,Integer> shows = new HashMap<Integer, Integer>();
        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            shows.put((Integer)document.get("activity_id"), (Integer)document.get("shows"));
        }
        return shows;
    }

    public static void setShowCount(int uid, int activityId, int count) {
        BasicDBObject update = new BasicDBObject("user_id", uid);
        update.append("activity_id", activityId);
        BasicDBObject query = update; // Without count field
        update.append("shows", count);
        collUsersActivitiesSeen.update(query, update, true, false);
    }

	public static void insertUser(String username, String email, String passwordHash) {
        BasicDBObject doc = new BasicDBObject("username", username);
        doc.append("email", email);
        doc.append("salt", passwordHash);
        collUsers.insert(doc);
	}

    public static void insertDevice(String deviceId) {
        BasicDBObject doc = new BasicDBObject("username", "");
        doc.append("email", "");
        doc.append("salt", "");
        doc.append("deviceid", deviceId);
        collUsers.insert(doc);
    }

	public static int getUserId(User user) {
        BasicDBObject query;
        if (user.loginName != null) {
            query = new BasicDBObject("username", user.loginName);
        }
        else {
            query = new BasicDBObject("deviceid", user.deviceId);
        }
        DBCursor cursor = collUsers.find(query);
        if (cursor.hasNext()) {
            DBObject document = cursor.next();
            return (Integer)document.get("id");
        }
        return 0;
	}
	
	public static void setUserRating(int userId, int activityId, int rating) {
        BasicDBObject update = new BasicDBObject("user_id", uid);
        update.append("activity_id", activityId);
        BasicDBObject query = update; // Without rating field
        update.append("rating", rating);
        collUsersActivitiesLog.update(query, update, true, false);
	}

    // Helper method
    public static DBObject buildLocationQuery(double lat, double lng, double boxSize) {
        DBObject condition1 = new BasicDBObject();
        condition1.put("lat", new BasicDBObject("$lt", lat + boxSize));

        DBObject condition2 = new BasicDBObject();
        condition1.put("lng", new BasicDBObject("$gt", lat - boxSize));

        DBObject condition3 = new BasicDBObject();
        condition1.put("lat", new BasicDBObject("$lt", lng + boxSize));

        DBObject condition4 = new BasicDBObject();
        condition1.put("lat", new BasicDBObject("$gt", lng - boxSize));

        BasicDBList and = new BasicDBList();
        and.add(condition1);
        and.add(condition2);
        and.add(condition3);
        and.add(condition4);
        DBObject query = new BasicDBObject("$and", and);
        return query;
    }

    public static List<LocationData> getLocationData(double lat, double lng, double boxSize) {
        ArrayList<LocationData> locationData = new ArrayList<LocationData>();
        DBObject query = buildLocationQuery(lat, lng, boxSize);
        DBCursor cursor = collLatLngMetadata.find(query);

        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            LocationData ld = new LocationData();
            ld.lat = (Double)document.get("lat");
            ld.lng = (Double)document.get("lng");
            //ld.data = new JSONObject() metadata
        }
        return locationData;
    }

	public static List<Activity> getSuggestions(double lat, double lng, double boxSize, boolean isHungry, boolean isOutdoors, boolean isBored) throws JSONException {
        DBObject query = buildLocationQuery(lat, lng, boxSize);
        coll

        try {
			String query = "SELECT * FROM activities where lat < " + (lat + boxSize) + " AND lat > " + (lat - boxSize) + " AND lng < " + (lng + boxSize) + " AND lng > " + (lng - boxSize)+ ";";
			ResultSet rs = stmt.executeQuery(query);
			ArrayList<Activity> activities = new ArrayList<Activity>();
			while(rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				String theme = rs.getString("theme");
				String address = rs.getString("address");
				double latitude = rs.getDouble("lat");
				double longitude = rs.getDouble("lng");
				String metadata = rs.getString("metadata");
				
				String website = null;
				String phoneNumber = null;
				ArrayList<String> categories = new ArrayList<String>();
				
				JSONObject json = new JSONObject(metadata);
				if (json.has("categories")) {
					JSONArray jsonArr = json.getJSONArray("categories");
					for (int i = 0; i < jsonArr.length(); i++) {
						categories.add(jsonArr.get(i).toString());
					}
				}
				if (json.has("website")) {
					website = json.getString("website");
				}
				if (json.has("phoneNumber")) {
					phoneNumber = json.getString("phoneNumber");
				}
				
				Activity activity = new Activity(title, id, theme, address, latitude, longitude, categories, website, phoneNumber, metadata);
				
				if (theme.equals("restaurant") && isFood) activities.add(activity);
				if ((theme.equals("nightlife") || theme.equals("casino")) && isBars) activities.add(activity);
				if ((theme.equals("trail") || theme.equals("pool") || theme.equals("golf") || theme.equals("zoo")) && isParks) activities.add(activity);
				if (theme.equals("movie-theatre") && isMovies) {
					activities.add(activity);
				}
				if (!isParks && !isBars && !isFood && !isMovies && !isShopping && !isOther) {
					activities.add(activity);
				}
				
			}
			return activities;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
