package suggest;

import model.Activity;
import model.LocationData;
import model.Suggestion;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import servlet.BrainServlet;
import util.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Keenon on 4/12/14.
 */
public class SuggestionEngine {
    public static Suggestion getSuggestion(List<Activity> activities, BrainServlet.TransportType transportType, BrainServlet.Feeling feeling) {
        double minCost = Double.POSITIVE_INFINITY;
        Suggestion suggestion = new Suggestion();

        for (Activity activity : activities) {
            String reasonToSuggest = assignCostToActivity(activity,transportType,feeling);
            if (activity.cost < minCost) {
                minCost = activity.cost;
                suggestion.activity = activity;
                suggestion.reason = reasonToSuggest;
            }
        }

        suggestion.reason += " "+RemarkEngine.getSimilarCategoryRemark(suggestion.activity, activities);

        if (suggestion.activity == null) {
            suggestion.reason = RemarkEngine.getNoResultsRemark();
        }

        return suggestion;
    }

    private static boolean isActivityQualified(Activity activity) {

        // If the activity doesn't have a thumbnail, don't send it back

        try {
            activity.metadata.getString("yelp_thumbnail");
        } catch (JSONException e) {
            return false;
        }

        // If we have recorded hours in the DB, and this place is closed, then
        // don't suggest it. Duh.

        try {
            JSONArray open = activity.metadata.getJSONArray("open");
            boolean openHours = false;

            LocalDateTime time = new LocalDateTime();
            int currentDayOfWeek = time.getDayOfWeek()-1;
            int currentMillis = time.getMillisOfDay();
            String[] dayArr = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
            List<String> days = new ArrayList<String>(Arrays.asList(dayArr));

            for (int i = 0; i < open.length(); i++) {
                JSONObject timeRecord = open.getJSONObject(i);
                String daySpec = timeRecord.getString("days");
                if (daySpec.contains(" - ")) {
                    String startDay = daySpec.split(" - ")[0].trim();
                    String endDay = daySpec.split(" - ")[1].trim();
                    int startDayIndex = days.indexOf(startDay);
                    int endDayIndex = days.indexOf(endDay);
                    if (currentDayOfWeek < startDayIndex || currentDayOfWeek > endDayIndex) continue;
                }
                else {
                    if (currentDayOfWeek != days.indexOf(daySpec)) continue;
                }
                String hours = timeRecord.getString("hours");
                if (!hours.contains(" - ")) continue; // Can't parse the time. Probably "Closed"
                String startHour = hours.split(" - ")[0].trim();
                String endHour = hours.split(" - ")[1].trim();

                DateTimeFormatter timeFormat = DateTimeFormat.forPattern("hh:mm aa");
                DateTime startTime = timeFormat.parseDateTime(startHour);
                DateTime endTime = timeFormat.parseDateTime(endHour);

                if (currentMillis >= startTime.getMillisOfDay() && currentMillis <= endTime.getMillisOfDay()) {
                    openHours = true;
                }
            }
            if (!openHours) return false;
        } catch (JSONException e) {
            // If no hours recorded, then don't bother to qualify. Seems better to assume we're a go.
        }
        return true;
    }

    private static double getDistanceCost(Activity activity, BrainServlet.TransportType transportType) {
        double distanceMultiplier = 0.5;

        switch (transportType) {
            case WALK:
                distanceMultiplier = 4.0;
                break;
            case BIKE:
                distanceMultiplier = 1.0;
                break;
            case DRIVE:
                distanceMultiplier = 0.25;
                break;
            case TRANSIT:
                distanceMultiplier = 0.25;
                break;
        }
        return activity.distanceFromQuery*distanceMultiplier;
    }

    private static double getYelpScoreCost(Activity activity) {
        double yelpMultiplier = 1.5;

        double yelpScore = -1;
        try {
            yelpScore = activity.metadata.getDouble("yelp_score");
        } catch (JSONException e) {
            // do nothing
        }
        if (yelpScore != -1) return yelpScore * yelpMultiplier;
        return 0;
    }

    private static double getRedundancyCost(Activity activity) {
        return activity.shows*2;
    }

    private static double getWeatherCost(Activity activity) {
        double weatherOpinion = 0;
        // If we're outside
        if (activity.theme.equals("trail") || activity.theme.equals("pool") || activity.theme.equals("golf")) weatherOpinion = 1;
        else if (activity.theme.equals("movie-theatre")) weatherOpinion = -1;

        try {
            double tempF = activity.nearestLocationData.data.getJSONObject("current_condition").getDouble("temp_F");
            // If we're going outside, consider anything above 60 a bonus, and anything below a penalty.
            // Of course, our "weatherOpinion" can flip this. On crumby days, we'll favor movies.
            tempF -= 60;
            tempF /= 20;
            return tempF*weatherOpinion;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Weather plays no part if we're not outside
        return 0;
    }

    private static String assignCostToActivity(Activity activity, BrainServlet.TransportType transportType, BrainServlet.Feeling feeling) {

        activity.cost = 0;
        activity.cost += getDistanceCost(activity,transportType);
        activity.cost += getYelpScoreCost(activity);
        activity.cost += getRedundancyCost(activity);
        activity.cost += getWeatherCost(activity);

        if (!isActivityQualified(activity)) activity.cost = Double.POSITIVE_INFINITY;
        String reasonToSuggest = RemarkEngine.getBaseRemark();
        return reasonToSuggest;
    }
}
