package th.co.krungthaiaxa.elife.api.exception;

public class ElifeException extends RuntimeException {
    public ElifeException() {
    }

    public ElifeException(String message) {
        super(message);
    }

    public ElifeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElifeException(Throwable cause) {
        super(cause);
    }

    public ElifeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
