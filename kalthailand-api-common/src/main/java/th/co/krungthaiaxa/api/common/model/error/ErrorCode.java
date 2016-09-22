package th.co.krungthaiaxa.api.common.model.error;

import java.util.function.Function;

public class ErrorCode {
    public static final String ERROR_CODE_UNKNOWN_ERROR = "0000";
    public static final Function<String, Error> UNKNOWN_ERROR = msg -> new Error(ERROR_CODE_UNKNOWN_ERROR, "Unknown error: " + msg, "This is an unknown error. Please contact administrator to get more information.");
    public static final Function<String, Error> BAD_REQUEST = msg -> new Error("0001", "Bad request error: " + msg, "Your request is not compatible with server. Please recheck the API");

    //Bean Validation (0010 -> 0019)
    public static final String ERROR_CODE_BEAN_VALIDATION = "0010";

    //File Exception (0020 -> 0029)
    public static final String ERROR_CODE_FILE_NOT_FOUND = "0020";
    public static final String ERROR_CODE_FILE_IO = "0021";

    //Json Exception (0030 -> 0039)
    public static final String ERROR_CODE_JSON_CONVERTER = "0030";

    //Jasper Exception (0040 -> 0049)
    public static final String ERROR_CODE_JASPER = "0040";

    //Encrypt Exception (0090 -> 0091)
    public static final String ERROR_CODE_ENCRYPT = "0090";

    // Watermarking
    public static final Error WATERMARK_IMAGE_INPUT_NOT_READABLE = new Error("0101", "Unable to upload your picture", "The image cannot be read as stream");
    public static final Error INVALID_LINE_ID = new Error("0102", "Unable to get your user ID", "Unable to get mid out of decrypted value");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE = new Error("0103", "Unable to upload your picture", "The image cannot be read as an image");
    public static final Error WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN = new Error("0104", "Unable to save your picture", "The output image cannot be saved");
    public static final Error WATERMARK_IMAGE_INPUT_TOO_SMALL = new Error("0105", "Unable to save your picture, it is too small", "The input image is too small to apply the watermark");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_SUPPORTED = new Error("0106", "Unable to save your picture, it is not supported", "The input image is not supported by Java SDK with ImageIO.read");
    public static final Error WATERMARK_IMAGE_MIME_TYPE_UNKNOWN = new Error("0107", "Unable to get the type of image", "The input image has a mime type unknown from its Input Stream");
    //Policyquota
    public static final Error POLICY_QUOTA_DOES_NOT_EXIST = new Error("0201", "The policy quota does not exist", "The policy quota does not exist");
    public static final Error INVALID_POLICY_QUOTA_PROVIDED = new Error("0202", "Unable to update your policy quota", "The policy quota provided as JSon is not a valid policy quota, probably an incompatibility between client and server");
    public static final Error INVALID_POLICY_QUOTA_EMAIL_LIST = new Error("0203", "Unable to update your policy quota", "The policy quota email list is not valid");
    public static final Error INVALID_POLICY_QUOTA_PERCENT = new Error("0204", "Unable to update your policy quota", "The policy quota percent is not valid");
    public static final Error INVALID_POLICY_NUMBER_EXCEL_FILE = new Error("0205", "Invalid policy number excel file format", "An upload policy number excel file is invalid");
    // Quote
    public static final Error INVALID_QUOTE_PROVIDED = new Error("0301", "Unable to update your quote", "The quote provided as JSon is not a valid quote, probably an incompatibility between mobile and server");
    public static final Function<String, Error> QUOTE_NOT_CREATED = msg -> new Error("0302", "The quote has not been updated", msg);
    public static final Function<String, Error> QUOTE_NOT_UPDATED = msg -> new Error("0303", "The quote has not been updated", "The quote has not been updated. Error is: " + msg);
    public static final Error QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED = new Error("0304", "Unable to access given quote", "The quote could not be found or given sessionId does not have access to the quote");

    // Policy
    public static final String ERROR_CODE_POLICY_NOT_EXIST = "0401";

    public static final Error POLICY_DOES_NOT_EXIST = new Error(ERROR_CODE_POLICY_NOT_EXIST, "The policy does not exist", "The policy does not exist");
    public static final Error POLICY_DOES_NOT_CONTAIN_A_PAYMENT_WITH_TRANSACTION_ID = new Error("0402", "The policy does not contain a payment with a transaction id", "The policy does not contain a payment with a transaction id");
    public static final Function<String, Error> POLICY_CANNOT_BE_CREATED = msg -> new Error("0403", "Unable to create your policy. Error is: " + msg, "The policy could not be created out of the quote for validation reasons");
    public static final Function<String, Error> POLICY_IS_CANCELED = msg -> new Error("0404", "The policy [" + msg + "] is canceled", "The policy [" + msg + "] is canceled");
    public static final Function<String, Error> POLICY_IS_PENDING_PAYMENT = msg -> new Error("0405", "The policy [" + msg + "] is waiting for payment registration", "The policy [" + msg + "] is waiting for payment registration");
    public static final Function<String, Error> POLICY_IS_NOT_PENDING_FOR_PAYMENT = msg -> new Error("0407", "The policy [" + msg + "] is not pending for payment and cannot be updated to pending for validation",
            "The policy [" + msg + "] is not pending for payment and cannot be updated to pending for validation");
    public static final Function<String, Error> POLICY_IS_NOT_VALIDATED_FOR_PAYMENT = msg -> new Error("0407", "The policy [" + msg + "] is not validated for payment and cannot be updated to validated status",
            "The policy [" + msg + "] is not validated for payment and cannot be updated to validated status");
    public static final Function<String, Error> ORDER_ID_NOT_PROVIDED = msg -> new Error("0408", "The order id was not provided", "The order id was not provided");
    public static final Function<String, Error> POLICY_VALIDATION_ERROR = msg -> new Error("0409", "Payment is successful but there was an error while trying to validate the policy. Error is [" + msg + "].",
            "There was an error wh ile trying to validate the policy after validating the payment. Error is [" + msg + "].");
    public static final Error AGENT_CODE_FORMAT_ERROR = new Error("0410", "Agent code is not in format '123456-12-123456'.", "Agent code is not in format '123456-12-123456'.");
    public static final Error REAL_CAPTURE_API_HAS_TO_BE_USED = new Error("0411", "When validating a Policy in Production, only real call to Line Pay is allowed.", "When validating a Policy in Production, only real call to Line Pay is allowed.");

    // Line
    public static final Error UNABLE_TO_DECRYPT = new Error("0501", "Unable to get your user ID", "The provided text could not be decrypted");
    public static final Error UNABLE_TO_GET_LINE_BC = new Error("0502", "Unable to get line bc", "The provided mid is not valid or have no line bc data along with input mid");
    public static final String ERROR_CODE_UNABLE_TO_CAPTURE_PAYMENT = "0503";
    public static final Function<String, Error> UNABLE_TO_CAPTURE_PAYMENT = msg -> new Error(ERROR_CODE_UNABLE_TO_CAPTURE_PAYMENT, "Unable to confirm the payment. Error is [" + msg + "]", "Unable to confirm the payment. Error is [" + msg + "]");
    public static final String ERROR_CODE_LINE_TOKEN_NOT_EXIST = "0504";

    //Payment
    public static final String ERROR_CODE_PAYMENT_NOT_FOUND = "0510";
    public static final String ERROR_CODE_PAYMENT_HAS_NEWER_COMPLETED = "0511";

    // Document
    public static final Error POLICY_DOES_NOT_CONTAIN_DOCUMENT = new Error("0601", "Unable to locate the document", "The given document id does not exist for the given policy");
    public static final Error UNABLE_TO_DOWNLOAD_DOCUMENT = new Error("0602", "Unable to download the document", "Processing for create e-receipt fail");
    public static final String ERROR_CODE_DOCUMENT_ERECEIPT = "0610";

    // Email
    public static final String ERROR_CODE_EMAIL_SENDER = "0701";
    public static final Function<String, Error> UNABLE_TO_SEND_EMAIL = msg -> new Error(ERROR_CODE_EMAIL_SENDER, "Unable to send email", "Error message is:" + msg);

    // Watermarking
    public static final Function<String, Error> INVALID_COLLECTION_FILE = msg -> new Error("0901", "Unable to upload the collection file. Error is: [" + msg + "]", "Unable to upload the collection file. Error is: [" + msg + "]");

    // ADMIN UI
    public static final Function<String, Error> UI_UNAUTHORIZED = msg -> new Error("9001", "You are not authorized to see this page", msg);
    public static final Function<String, Error> NOTIFICATION_NOT_SENT = msg -> new Error("9002", "Notification was not sent. Error is: " + msg, "Notification was not sent. Error is: " + msg);

}
