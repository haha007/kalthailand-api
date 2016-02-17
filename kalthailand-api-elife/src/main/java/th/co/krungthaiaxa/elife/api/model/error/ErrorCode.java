package th.co.krungthaiaxa.elife.api.model.error;

import java.util.function.Function;

public class ErrorCode {

    // Watermarking
    public static final Error WATERMARK_IMAGE_INPUT_NOT_READABLE = new Error("10001", "Unable to upload your picture", "The image cannot be read as stream");
    public static final Error INVALID_LINE_ID = new Error("10002", "Unable to get your user ID", "Unable to get mid out of decrypted value");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE = new Error("10003", "Unable to upload your picture", "The image cannot be read as an image");
    public static final Error WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN = new Error("10004", "Unable to save your picture", "The output image cannot be saved");
    public static final Error WATERMARK_IMAGE_INPUT_TOO_SMALL = new Error("10005", "Unable to save your picture, it is too small", "The input image is too small to apply the watermark");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_SUPPORTED = new Error("10006", "Unable to save your picture, it is not supported", "The input image is not supported by Java SDK with ImageIO.read ");
    // Quote
    public static final Error INVALID_QUOTE_PROVIDED = new Error("30001", "Unable to update your quote", "The quote provided as JSon is not a valid quote, probably an incompatibility between mobile and server");
    public static final Error INVALID_PRODUCT_ID_PROVIDED = new Error("30002", "The selected product doesn't exist", "The product ID sent does not match known product");
    public static final Error QUOTE_NOT_UPDATED = new Error("30003", "The quote has not been updated", "An error occured while trying to update the quote");
    public static final Error QUOTE_DOES_NOT_EXIST = new Error("30004", "Quote dose not exists", "There is no quote with the given quote Id");
    public static final Error QUOTE_DOES_NOT_EXIST_OR_ACCESS_DENIED = new Error("30005", "Unable to access given quotes", "The quote could not be found or given mid does not have access to the quote");
    // Policy
    public static final Error POLICY_DOES_NOT_EXIST = new Error("40001", "The policy does not exist", "The policy does not exist");
    public static final Error POLICY_DOES_NOT_CONTAIN_PAYMENT = new Error("40002", "The policy does not contain the given payment", "The given payment id could not be found in the list of payments within the policy");
    public static final Function<String, Error> POLICY_CANNOT_BE_CREATED = msg -> new Error("40003", "Unable to create your policy. Error is: " + msg, "The policy could not be created out of the quote for validation reasons");
    // Line token decryption
    public static final Error UNABLE_TO_DECRYPT = new Error("50001", "Unable to get your user ID", "The provided text could not be decrypted");
    // E-Receipt
    public static final Error UNABLE_TO_CREATE_ERECEIPT = new Error("60001", "Unable to create e-receipt", "Processing for create e-receipt fail");
    // Email
    public static final Error UNABLE_TO_SEND_EMAIL = new Error("70001", "Unable to send email", "The Provided email is not valid");

}
