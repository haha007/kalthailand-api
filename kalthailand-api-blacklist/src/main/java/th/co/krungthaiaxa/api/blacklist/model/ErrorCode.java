package th.co.krungthaiaxa.api.blacklist.model;

import java.util.function.Function;

public class ErrorCode {	
	public static final Function<String, Error> INVALID_THAI_ID_FORMAT = msg -> new Error("00001", "Invalid thai ID to check blacklist. Error is [" + msg + "].", "Thai ID format must be number only. Error is [" + msg + "].");
	public static final Function<String, Error> INVALID_THAI_ID_LENGTH = msg -> new Error("00002", "Invalid thai ID length to check blacklist. Error is [" + msg + "].", "Thai ID length must be 13. Error is [" + msg + "].");
	public static final Function<String, Error> UNAUTHORIZED = msg -> new Error("00003", "You are not authorized to use this API", msg);

	public static final Function<String, Error> INVALID_BLACKLIST_FILE = msg -> new Error("9003", "Invalid Excel file. Error is: " + msg, "Inavlid Excel file. Error is: " + msg);
}
