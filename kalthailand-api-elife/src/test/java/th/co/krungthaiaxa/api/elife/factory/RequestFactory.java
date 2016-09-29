package th.co.krungthaiaxa.api.elife.factory;

/**
 * @author khoi.tran on 9/29/16.
 */
public class RequestFactory {
    public static String generateAccessToken() {
        return "MOCKACCESSTOKEN_" + System.currentTimeMillis();
    }

    public static String generateSession() {
        return "MOCKSESSION_" + System.currentTimeMillis();
    }
}
