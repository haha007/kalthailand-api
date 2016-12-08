package th.co.krungthaiaxa.api.common.utils;

/**
 * @author khoi.tran on 12/8/16.
 */
public class ExceptionUtil {
    public static String getDataExceptionRoot(Exception exception) {
        return getErrorLine(exception, 0);
    }

    public static String getNullPointerExceptionRoot(Exception exception) {
        return getErrorLine(exception, 0);
    }

    public static String getErrorLine(Exception exception, int stackTraceIndex) {
        StackTraceElement first = exception.getStackTrace()[stackTraceIndex];
        String fileName = first.getFileName();
        String methodName = first.getMethodName();
        int lineNumber = first.getLineNumber();
        String errorRootCause = String.format("%s#%s():%s", fileName, methodName, lineNumber);
        return "Root: " + errorRootCause;
    }
}
