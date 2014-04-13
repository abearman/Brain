package suggest;

import model.Activity;
import model.LocationData;
import model.Suggestion;
import org.json.JSONArray;
import org.json.JSONException;
import servlet.BrainServlet;
import util.Util;

import java.util.*;

/**
 * Created by Keenon on 4/12/14.
 */
public class SuggestionEngine {
    public static Suggestion getSuggestion(List<Activity> activities, BrainServlet.TransportType transportType) {
        System.out.println("Shown: "+activities.get(0).shows);
        try {
            System.out.println("Yelp Review: "+activities.get(0).metadata.getDouble("yelp_rating"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("User Rating: "+activities.get(0).userRating);
        System.out.println("Predicted Rating: "+activities.get(0).predictedRating);

        double minCost = Double.POSITIVE_INFINITY;
        Suggestion suggestion = new Suggestion();

        for (Activity activity : activities) {
            String reasonToSuggest = assignCostToActivity(activity,transportType);
            if (activity.cost < minCost) {
                minCost = activity.cost;
                suggestion.activity = activity;
                suggestion.reason = reasonToSuggest;
            }
        }

        Set<String> categoriesLiked = new HashSet<String>();
        for (Activity activity : activities) {
            if (activity.userRating == 1) {
                if (activity.metadata.has("categories")) {
                    try {
                        JSONArray categories = activity.metadata.getJSONArray("categories");
                        for (int i = 0; i < categories.length(); i++) {
                            String likedCategory = categories.getString(i);
                            if (!categoriesLiked.contains(likedCategory)) categoriesLiked.add(likedCategory);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<String> categoriesLikedPresent = new ArrayList<String>();
        if (suggestion.activity.metadata.has("categories")) {
            try {
                JSONArray categories = suggestion.activity.metadata.getJSONArray("categories");
                for (int i = 0; i < categories.length(); i++) {
                    String likedCategory = categories.getString(i);
                    if (likedCategory.equals("Restaurants")) continue;
                    if (categoriesLiked.contains(likedCategory)) categoriesLikedPresent.add(likedCategory);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (categoriesLikedPresent.size() > 0) {
            String proofCategory = categoriesLikedPresent.get(0).toLowerCase();
            if (proofCategory.endsWith("s")) proofCategory = proofCategory.substring(0,proofCategory.length()-1);
            boolean startsWithVowel = proofCategory.matches("[aAeEiIoOuU].*");
            suggestion.reason += " This joint is a";
            if (startsWithVowel) suggestion.reason += "n";
            suggestion.reason += " "+proofCategory+", which you've enjoyed in the past.";
        }

        return suggestion;
    }

    private static String assignCostToActivity(Activity activity, BrainServlet.TransportType transportType) {
        double distanceMultiplier = 0.5;
        switch (transportType) {
            case WALK:
                distanceMultiplier = 2.0;
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
        double yelpScore = -1;
        try {
            yelpScore = activity.metadata.getDouble("yelp_score");
        } catch (JSONException e) {
            // do nothing
        }
        double yelpMultiplier = 1.5;
        activity.cost = (activity.distanceFromQuery*distanceMultiplier) + (activity.shows*2);
        if (yelpScore != -1) activity.cost -= yelpScore*yelpMultiplier;
        String reasonToSuggest = "I have a good feeling about this one.";
        if (transportType == BrainServlet.TransportType.WALK) {
            reasonToSuggest = "Since you're on foot, here's a restaurant that's only "+activity.distanceFromQuery+" miles away";
        }
        return reasonToSuggest;
    }
}
