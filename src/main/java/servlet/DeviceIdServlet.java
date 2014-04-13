package servlet;

import db.DAL;
import model.AccountManager;
import model.User;
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
public class DeviceIdServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String deviceId = request.getParameter("deviceid");
        if (deviceId == null) {
            JSONObject error = new JSONObject();
            try {
                error.put("error","no 'deviceid' specified");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            response.getWriter().write(error.toString());
            return;
        }

        int uid = AccountManager.getOrCreateDeviceUid(deviceId);
        JSONObject uidJSON = new JSONObject();
        try {
            uidJSON.put("uid",uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response.getWriter().write(uidJSON.toString());

    }

}
