package th.co.krungthaiaxa.api.signing.model;

import java.util.function.Function;

public class ErrorCode {
    public static final Function<String, Error> PDF_INVALID = msg -> new Error("0001", "Received PDF is not valid", "Received PDF is not valid. Error is:[" + msg + "].");
    public static final Error NOT_BASE_64_ENCODED = new Error("0002", "The document is not Base 64 encoded.", "The document is not Base 64 encoded.");
    public static final Function<String, Error> UNABLE_TO_SIGN = msg -> new Error("0003", "Unable to sign the document", "Unable to sign the document. Error is:[" + msg + "].");
    public static final Function<String, Error> UNAUTHORIZED = msg -> new Error("0004", "You are not authorized to use this API", msg);

}
