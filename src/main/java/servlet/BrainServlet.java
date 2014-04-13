package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import db.DAL;
import model.Activity;
import model.LocationData;
import model.Suggestion;
import org.json.JSONException;
import suggest.SuggestionEngine;
import util.Util;

/**
 * Created by Keenon on 4/12/14.
 */
public class BrainServlet extends HttpServlet {

    public static enum TransportType { WALK, BIKE, DRIVE, TRANSIT };
    public static enum Feeling { HUNGRY, ADVENTUROUS, BORED };

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Declare params

        int uid = 0;
        double lat = 0;
        double lng = 0;
        TransportType transportType = TransportType.DRIVE;
        Feeling feeling = Feeling.HUNGRY;

        // optimization param

        double boxSize = 2.0;

        // Read parameters

        uid = Integer.parseInt(request.getParameter("uid"));
        lat = Double.parseDouble(request.getParameter("lat"));
        lng = Double.parseDouble(request.getParameter("lng"));
        if (request.getParameter("transport") != null) {
            if (request.getParameter("transport").equals("walking")) transportType = TransportType.WALK;
            if (request.getParameter("transport").equals("bicycling")) transportType = TransportType.BIKE;
            if (request.getParameter("transport").equals("driving")) transportType = TransportType.DRIVE;
            if (request.getParameter("transport").equals("transit")) transportType = TransportType.TRANSIT;
        }
        if (request.getParameter("feeling") != null) {
            if (request.getParameter("feeling").equals("hungry")) feeling = Feeling.HUNGRY;
            if (request.getParameter("feeling").equals("adventurous")) feeling = Feeling.ADVENTUROUS;
            if (request.getParameter("feeling").equals("bored")) feeling = Feeling.BORED;
        }

        // Pick an activity

        Map<Integer,Integer> userRatings = DAL.getUserRankings(uid);
        Map<Integer,Double> userRatingsPredicted = DAL.getPredictedUserRankings(uid);
        Map<Integer,Integer> showCount = DAL.getShowCount(uid);
        List<LocationData> locationData = DAL.getLocationData(lat,lng,boxSize);

        List<Activity> activities = new ArrayList<Activity>();

        try {
            switch (feeling) {
                case HUNGRY:
                    activities = DAL.getSuggestions(lat,lng,boxSize,false,false,true,false,false,false);
                    break;
                case ADVENTUROUS:
                    activities = DAL.getSuggestions(lat,lng,boxSize,true,true,false,false,false,true);
                    break;
                case BORED:
                    activities = DAL.getSuggestions(lat,lng,boxSize,false,false,false,true,true,true);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (Activity activity : activities) {

            // Get user related scratch data

            if (userRatings.containsKey(activity.id)) activity.userRating = userRatings.get(activity.id);
            if (userRatingsPredicted.containsKey(activity.id)) activity.predictedRating = userRatingsPredicted.get(activity.id);
            if (showCount.containsKey(activity.id)) activity.shows = showCount.get(activity.id);

            // Get distance from query

            activity.distanceFromQuery = Util.latLngToMiles(lat, lng, activity.latitude, activity.longitude);

            // Get closest location data

            double minDistance = Double.POSITIVE_INFINITY;
            for (LocationData data : locationData) {
                double dataDistance = Util.latLngToMiles(activity.latitude,activity.longitude,data.lat,data.lng);
                if (dataDistance < minDistance) {
                    minDistance = dataDistance;
                    activity.nearestLocationData = data;
                }
            }
        }

        Suggestion suggestion = SuggestionEngine.getSuggestion(activities,transportType);
        DAL.setShowCount(uid,suggestion.activity.id,suggestion.activity.shows+1);

        response.setContentType("application/json");
        response.getWriter().write(suggestion.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write("GET not supported on this API");
    }
}
