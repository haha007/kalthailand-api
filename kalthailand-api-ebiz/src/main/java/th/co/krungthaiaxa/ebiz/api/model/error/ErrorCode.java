package th.co.krungthaiaxa.ebiz.api.model.error;

public class ErrorCode {
    // Line token decryption
    public static final Error UNABLE_TO_DECRYPT = new Error("10001", "Unable to get your user ID", "The provided text could not be decrypted");
    public static final Error INAVLID_LINE_ID = new Error("10002", "Unable to get your user ID", "Unable to get mid out of decrypted value");

    // Watermarking
    public static final Error WATERMARK_IMAGE_INPUT_NOT_READABLE = new Error("10013", "Unable to upload your picture", "The image cannot be read as stream");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_BUFFERABLE = new Error("10014", "Unable to upload your picture", "The image cannot be read as an image");
    public static final Error WATERMARK_IMAGE_OUTPUT_NOT_WRITTEN = new Error("10015", "Unable to save your picture", "The output image cannot be saved");
    public static final Error WATERMARK_IMAGE_INPUT_TOO_SMALL = new Error("10016", "Unable to save your picture, it is too small", "The input image is too small to apply the watermark");
    public static final Error WATERMARK_IMAGE_INPUT_NOT_SUPPORTED = new Error("10017", "Unable to save your picture, it is not supported", "The input image is not supported by Java SDK with ImageIO.read ");

    // OCR
    public static final Error OCR_INVALID_REQUEST = new Error("10021", "Unable to verify your ID", "The request doesn't contain any uploaded bytes");
    public static final Error OCR_IMAGE_INPUT_NOT_RECEIVED = new Error("10022", "Unable to verify your ID", "The image is empty");
    public static final Error OCR_IMAGE_INPUT_NOT_READABLE = new Error("10023", "Unable to verify your ID", "The image cannot be read as stream");
    public static final Error OCR_IMPOSSIBLE = new Error("10024", "Unable to verify your ID", "Extracting text on provided image was impossible");

    // Quote
    public final static Error INVALID_QUOTE_PROVIDED = new Error("20001", "Unable to update your quote", "The quote provided as JSon is not a valid quote, probably an incompatibility between mobile and server");
    public final static Error NO_QUOTE_IN_SESSION = new Error("20002", "You don't have a saved quote", "There is no quote with the given session id");

    // Quote
    public final static Error INVALID_POLICY_PROVIDED = new Error("20001", "Unable to update your policy", "The policy provided as JSon is not a valid policy, probably an incompatibility between mobile and server");

}
