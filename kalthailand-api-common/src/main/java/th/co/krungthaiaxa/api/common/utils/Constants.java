package th.co.krungthaiaxa.api.common.utils;

/**
 * Application constants.
 */
public final class Constants {

    //Regex for acceptable logins
    public static final String USERNAME_REGEX = "^[_'.@A-Za-z0-9-]*$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    private Constants() {
    }
}
