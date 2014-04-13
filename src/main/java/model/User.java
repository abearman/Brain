package model;

public class User {
	
	public String loginName;
    public String deviceId;

    public static User getUserWithLoginName(String loginName) {
        User user = new User();
        user.loginName = loginName;
        return user;
    }

    public static User getUserWithDeviceId(String deviceId) {
        User user = new User();
        user.deviceId = deviceId;
        return user;
    }

}
