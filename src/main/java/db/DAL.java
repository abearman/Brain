package db;

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
	private static DBConnection db;
	public static Statement stmt;
	
	/* Constants */
	public static final double EARTH_CIRCUMFERENCE = 24901;

    static {
        try {
            db = new DBConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stmt = db.getStatement();
    }

	public static Statement getStatement() {
		return stmt;
	}

	public static boolean accountExists(String loginName) {
		String query = "SELECT * FROM users WHERE username = \"" + loginName + "\";";
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) { //If there exists a record with this loginName in the database return true
				return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

    public static boolean deviceExists(String deviceId) {
        String query = "SELECT * FROM users WHERE deviceid = \"" + deviceId + "\";";
        try {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) { //If there exists a record with this loginName in the database return true
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

	public static boolean isPasswordForAccount(String loginName, String hashOfAttemptedPassword) {
		String query = "SELECT * FROM users WHERE username = \"" + loginName + "\";";
		try {
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			String databasePasswordHash = rs.getString("salt"); //Retrieves the stored hash from the Database for this User
			if (hashOfAttemptedPassword.equals(databasePasswordHash)) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

    public static Map<Integer,Integer> getUserRankings(int uid) {
        String query = "SELECT * FROM users_activities_log WHERE user_id = \"" + uid + "\";";
        Map<Integer,Integer> rankings = new HashMap<Integer, Integer>();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                rankings.put(rs.getInt("activity_id"),rs.getInt("rating"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rankings;
    }

    public static Map<Integer,Double> getPredictedUserRankings(int uid) {
        String query = "SELECT * FROM users_activities_predicted WHERE user_id = \"" + uid + "\";";
        Map<Integer,Double> rankings = new HashMap<Integer, Double>();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                rankings.put(rs.getInt("activity_id"),rs.getDouble("rating"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rankings;
    }

    public static Map<Integer,Integer> getShowCount(int uid) {
        String query = "SELECT * FROM users_seen_activities WHERE user_id = \"" + uid + "\";";
        Map<Integer,Integer> shows = new HashMap<Integer, Integer>();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                shows.put(rs.getInt("activity_id"), rs.getInt("shows"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    public static void setShowCount(int uid, int activityId, int count) {
        try {
            String update = "INSERT INTO users_seen_activities(user_id, activity_id, shows) VALUES(\"" +  uid + "\", \"" + activityId + "\", \"" + count + "\") ON DUPLICATE KEY UPDATE shows="+count+";";
            stmt.executeUpdate(update);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static void insertUser(String username, String email, String passwordHash) {
		try {
			String update = "INSERT INTO users(username, email, salt) VALUES(\"" + username + "\", " + "\"" + email + "\", \"" + passwordHash + "\");";
			stmt.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

    public static void insertDevice(String deviceId) {
        try {
            String update = "INSERT INTO users(username,email,salt,deviceid) VALUES (\""+deviceId+"\",\""+deviceId+"\",\"\",\"" + deviceId + "\");";
            stmt.executeUpdate(update);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static int getUserId(User user) {
        String query;
        if (user.loginName != null) {
		    query = "SELECT id FROM users WHERE username=\""+user.loginName+"\";";
        }
        else {
            query = "SELECT id FROM users WHERE deviceid=\""+user.deviceId+"\";";
        }
		try {
			ResultSet rset = stmt.executeQuery(query);
			if (rset.next()) {
				return rset.getInt("id");
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void setUserRating(int userId, int activityId, int rating) {
		try {
			String update = "INSERT INTO users_activities_log(user_id, activity_id, rating) VALUES(\"" + userId + "\", " + "\"" + activityId + "\", \"" + rating + "\")"
					+ " ON DUPLICATE KEY UPDATE rating="+rating+";";
			stmt.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace(); 
		}
	}

    public static List<LocationData> getLocationData(double lat, double lng, double boxSize) {
        ArrayList<LocationData> locationData = new ArrayList<LocationData>();
        try {
            String query = "SELECT * FROM lat_lng_metadata where lat < " + (lat + boxSize) + " AND lat > " + (lat - boxSize) + " AND lng < " + (lng + boxSize) + " AND lng > " + (lng - boxSize)+ ";";
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                try {
                    LocationData ld = new LocationData();
                    ld.lat = rs.getDouble("lat");
                    ld.lng = rs.getDouble("lng");
                    ld.data = new JSONObject(rs.getString("metadata"));
                    locationData.add(ld);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locationData;
    }

    public static List<Event> getEventsForActivity(Activity activity) {
        List<Event> events = new ArrayList<Event>();
        try {
            LocalDateTime time = new LocalDateTime();
            String query = "SELECT * FROM events where venue = "+activity.id+" AND starthour >= "+time.getHourOfDay()+";";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Event event = new Event();
                event.activity = activity;
                event.starthour = rs.getInt("starthour");
                event.startminute = rs.getInt("startminute");
                event.endhour = rs.getInt("endhour");
                event.endminute = rs.getInt("endminute");
                event.metadata = new JSONObject(rs.getString("metadata"));
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return events;
    }

	public static List<Activity> getSuggestions(double lat, double lng, double boxSize, boolean isParks, boolean isBars, boolean isFood, boolean isMovies, boolean isShopping, boolean isOther) throws JSONException {
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
