package th.co.krungthaiaxa.api.auth.model;

import th.co.krungthaiaxa.api.common.model.error.Error;

import java.util.function.Function;

public class ErrorCode {
    public static final Error TOKEN_EMPTY = new Error("0001", "Empty token.", "Empty token.");
    public static final Error TOKEN_EXPIRED = new Error("0002", "Expired token.", "Expired token.");
    public static final Error NO_ROLE = new Error("0003", "No roles in received token.", "No roles in received token.");
    public static final Function<String, Error> ROLE_NOT_ALLOWED = msg -> new Error("0004", "Role is not allowed.", "Role [" + msg + "] is not allowed.");
    
    private ErrorCode(){
    }

}
