package model;

import db.DAL;
import util.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AccountManager {

    /* Default behavior if such device exists: Create one */
    public static int getOrCreateDeviceUid(String deviceId) {
        System.out.println("Attempting create or discover device "+deviceId);
        if (!DAL.deviceExists(deviceId)) {
            System.out.println("Device is being created");
            DAL.insertDevice(deviceId);
        }
        return DAL.getUserId(User.getUserWithDeviceId(deviceId));
    }

    /* Default behavior if no such user exists, return guest uid */
    public static int getAccountUid(String loginName, String passwordClear) {
        if (DAL.accountExists(loginName) && DAL.isPasswordForAccount(loginName, Util.hashPassword(passwordClear))) {
            return DAL.getUserId(User.getUserWithLoginName(loginName));
        }
        return 0;
    }

    public static boolean createAccount(String loginName, String email, String passwordClear) {
        if (DAL.accountExists(loginName)) {
            return false;
        }
        DAL.insertUser(loginName, email, Util.hashPassword(passwordClear));
        return true;
    }
}
