package servlet;

import db.DAL;
import model.AccountManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Keenon on 4/12/14.
 */
public class RatingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        int userId = Integer.parseInt(request.getParameter("uid"));
        int activityId = Integer.parseInt(request.getParameter("activityId"));
        int rating = Integer.parseInt(request.getParameter("rating"));

        DAL.setUserRating(userId,activityId,rating);

        JSONObject uidJSON = new JSONObject();
        try {
            uidJSON.put("request","success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response.getWriter().write(uidJSON.toString());
    }
}
