package suggest;

import model.Activity;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Keenon on 4/12/14.
 */
public class RemarkEngine {

    public static String getSimilarCategoryRemark(Activity suggestion, List<Activity> activities) {
        String reason = "";
        if (suggestion == null) return reason;
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
        if (suggestion.metadata.has("categories")) {
            try {
                JSONArray categories = suggestion.metadata.getJSONArray("categories");
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
            reason += "This joint is a";
            if (startsWithVowel) reason += "n";
            reason += " "+proofCategory+", which you've enjoyed in the past.";
        }

        return reason;
    }

    public static String getBaseRemark() {
        return "I have a good feeling about this one.";
    }

    public static String getNoResultsRemark() {
        return "I haven't found anything that matches your request.";
    }
}
