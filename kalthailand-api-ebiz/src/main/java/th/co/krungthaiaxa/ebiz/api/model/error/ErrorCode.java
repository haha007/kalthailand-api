package th.co.krungthaiaxa.ebiz.api.model.error;

import java.util.function.Function;

public class ErrorCode {

    // Watermarking
    public static final Error WATERMARK_IMAGE_INPUT_NOT_READABLE = new Error("10001", "Unable to upload your picture", "The image cannot be read as stream");
    public static final Error INVALID_LINE_ID = new Error("10002", "Unable to get your user ID", "Unable to get mid out of decrypted value");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE = new Error("10003", "Unable to upload your picture", "The image cannot be read as an image");
    public static final Error WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN = new Error("10004", "Unable to save your picture", "The output image cannot be saved");
    public static final Error WATERMARK_IMAGE_INPUT_TOO_SMALL = new Error("10005", "Unable to save your picture, it is too small", "The input image is too small to apply the watermark");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_SUPPORTED = new Error("10006", "Unable to save your picture, it is not supported", "The input image is not supported by Java SDK with ImageIO.read ");
    // OCR
    public static final Error OCR_INVALID_REQUEST = new Error("20001", "Unable to verify your ID", "The request doesn't contain any uploaded bytes");
    public static final Error OCR_IMAGE_INPUT_NOT_RECEIVED = new Error("20002", "Unable to verify your ID", "The image is empty");
    public static final Error OCR_IMAGE_INPUT_NOT_READABLE = new Error("20003", "Unable to verify your ID", "The image cannot be read as stream");
    public static final Error OCR_IMPOSSIBLE = new Error("20004", "Unable to verify your ID", "Extracting text on provided image was impossible");
    // Quote
    public static final Error INVALID_QUOTE_PROVIDED = new Error("30001", "Unable to update your quote", "The quote provided as JSon is not a valid quote, probably an incompatibility between mobile and server");
    public static final Error NO_QUOTE_IN_SESSION = new Error("30002", "You don't have a saved quote", "There is no quote with the given session id");
    // Policy
    public static final Error POLICY_DOES_NOT_EXIST = new Error("40001", "The policy does not exist", "The policy does not exist");
    public static final Error POLICY_DOES_NOT_CONTAIN_PAYMENT = new Error("40002", "The policy does not contain the given payment", "The given payment id could not be found in the list of payments within the policy");
    public static final Function<String, Error> POLICY_CANNOT_BE_CREATED = msg -> new Error("40003", "Unable to create your policy. Error is: " + msg, "The policy could not be created out of the quote for validation reasons");
    // Line token decryption
    public static final Error UNABLE_TO_DECRYPT = new Error("50001", "Unable to get your user ID", "The provided text could not be decrypted");
    // E-Receipt
    public static final Error UNABLE_TO_CREATE_ERECEIPT = new Error("60001", "Unable to create e-receipt", "Processing for create e-receipt fail");

}
