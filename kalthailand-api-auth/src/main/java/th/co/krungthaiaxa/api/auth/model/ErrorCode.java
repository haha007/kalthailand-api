package th.co.krungthaiaxa.api.auth.model;

import java.util.function.Function;

public class ErrorCode {
    public static final Error GENERIC_NOT_AUTHORIZED = new Error("0001", "You are not authorized to use this API.", "You are not authorized to use this API.");
    public static final Error TOKEN_EMPTY = new Error("0002", "Empty token.", "Empty token.");
    public static final Error TOKEN_EXPIRED = new Error("0003", "Expired token.", "Expired token.");
    public static final Error NO_ROLE = new Error("0004", "No roles in received token.", "No roles in received token.");
    public static final Function<String, Error> ROLE_NOT_ALLOWED = msg -> new Error("0005", "Role is not allowed.", "Role [" + msg + "] is not allowed.");

}
