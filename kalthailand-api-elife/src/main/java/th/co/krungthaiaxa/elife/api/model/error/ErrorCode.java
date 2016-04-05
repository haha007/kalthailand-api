package th.co.krungthaiaxa.elife.api.model.error;

import java.util.function.Function;

public class ErrorCode {

    // Watermarking
    public static final Error WATERMARK_IMAGE_INPUT_NOT_READABLE = new Error("0101", "Unable to upload your picture", "The image cannot be read as stream");
    public static final Error INVALID_LINE_ID = new Error("0102", "Unable to get your user ID", "Unable to get mid out of decrypted value");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE = new Error("0103", "Unable to upload your picture", "The image cannot be read as an image");
    public static final Error WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN = new Error("0104", "Unable to save your picture", "The output image cannot be saved");
    public static final Error WATERMARK_IMAGE_INPUT_TOO_SMALL = new Error("0105", "Unable to save your picture, it is too small", "The input image is too small to apply the watermark");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_SUPPORTED = new Error("0106", "Unable to save your picture, it is not supported", "The input image is not supported by Java SDK with ImageIO.read");
    public static final Error WATERMARK_IMAGE_MIME_TYPE_UNKNOWN = new Error("0107", "Unable to get the type of image", "The input image has a mime type unknown from its Input Stream");
    // Quote
    public static final Error INVALID_QUOTE_PROVIDED = new Error("0301", "Unable to update your quote", "The quote provided as JSon is not a valid quote, probably an incompatibility between mobile and server");
    public static final Error QUOTE_NOT_CREATED = new Error("0302", "The quote has not been updated", "An error occured while trying to create the quote");
    public static final Error QUOTE_NOT_UPDATED = new Error("0303", "The quote has not been updated", "An error occured while trying to update the quote");
    public static final Error QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED = new Error("0304", "Unable to access given quote", "The quote could not be found or given mid does not have access to the quote");
    // Policy
    public static final Error POLICY_DOES_NOT_EXIST = new Error("0401", "The policy does not exist", "The policy does not exist");
    public static final Error POLICY_DOES_NOT_CONTAIN_PAYMENT = new Error("0402", "The policy does not contain the given payment", "The given payment id could not be found in the list of payments within the policy");
    public static final Function<String, Error> POLICY_CANNOT_BE_CREATED = msg -> new Error("0403", "Unable to create your policy. Error is: " + msg, "The policy could not be created out of the quote for validation reasons");
    public static final Error PAYMENT_NOT_UPDATED_ERROR_DETAILS_NEEDED = new Error("0404", "The payment has not been updated, error detail is needed", "Either error code and/or error message are empty and are needed in case the payment was not successful");
    public static final Error PAYMENT_NOT_UPDATED_REG_KEY_NEEDED = new Error("0405", "The payment has not been updated, registration key is needed", "When payment booking from LINE Pay is successful, the registration key must be sent");
    public static final Error PAYMENT_NOT_UPDATED_TRANSACTION_NEEDED = new Error("0406", "The payment has not been updated, transaction id is needed", "When payment booking from LINE Pay is successful, the transaction id must be sent");
    public static final Function<String, Error> POLICY_IS_CANCELED = msg -> new Error("0407", "The policy [" + msg + "] is canceled", "The policy [" + msg + "] is canceled");
    public static final Function<String, Error> POLICY_IS_PENDING_PAYMENT = msg -> new Error("0408", "The policy [" + msg + "] is waiting for payment registration", "The policy [" + msg + "] is waiting for payment registration");
    public static final Function<String, Error> POLICY_IS_VALIDATED = msg -> new Error("0409", "The policy [" + msg + "] is already validated", "The policy [" + msg + "] is already validated");
    public static final Function<String, Error> POLICY_IS_NOT_PENDING_FOR_PAYMENT = msg -> new Error("0410", "The policy [" + msg + "] is not pending for payment and cannot be updated to pending for validation", "The policy [" + msg + "] is not pending for payment and cannot be updated to pending for validation");
    public static final Function<String, Error> ORDER_ID_NOT_PROVIDED = msg -> new Error("0411", "The order id was not provided", "The order id was not provided");
    // Line
    public static final Error UNABLE_TO_DECRYPT = new Error("0501", "Unable to get your user ID", "The provided text could not be decrypted");
    public static final Error UNABLE_TO_GET_LINE_BC = new Error("0502", "Unable to get line bc", "The provided mid is not valid or have no line bc data along with input mid");
    public static final Function<String, Error> UNABLE_TO_CONFIRM_AYMENT = msg -> new Error("0503", "Unable to confirm the payment", "Unable to confirm the payment. Error is [" + msg + "]");
    // Document
    public static final Error POLICY_DOES_NOT_CONTAIN_DOCUMENT = new Error("0601", "Unable to locate the document", "The given document id does not exist for the given policy");
    public static final Error UNABLE_TO_CREATE_ERECEIPT = new Error("0602", "Unable to create e-receipt", "Processing for create e-receipt fail");
    public static final Error UNABLE_TO_DOWNLOAD_DOCUMENT = new Error("0603", "Unable to download the document", "Processing for create e-receipt fail");
    // Email
    public static final Function<String, Error> UNABLE_TO_SEND_EMAIL = msg -> new Error("0701", "Unable to send email", "Error message is:" + msg);
    // Product
    public static final Function<String, Error> INVALID_PRODUCT_QUOTATION_PROVIDED = msg -> new Error("0801", "Unable to get the product details", "The given product cannot be transformed. Error message is:" + msg);
    // Watermarking
    public static final Function<String, Error> INVALID_COLLECTION_FILE = msg -> new Error("0901", "Unable to upload the collection file. Error is: [" + msg + "]", "Unable to upload the collection file. Error is: [" + msg + "]");
    // SMS
    public static final Function<String, Error> UNABLE_TO_SEND_SMS = msg -> new Error("1001", "Unable to send SMS", "Error message is:" + msg);
    public static final Function<String, Error> SMS_IS_UNAVAILABLE = msg -> new Error("1002", "SMS is unavailable", "Error message is:" + msg);


    // ADMIN UI
    public static final Error UI_UNAUTHORIZED = new Error("9001", "You are not authorized to see this page", "User's credentials do not allow the user to see this page");
}
