package th.co.krungthaiaxa.api.common.model.authentication;

public class Token {
    private String token;

    public static Token of(String token) {
        Token result = new Token();
        result.setToken(token);
        return result;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
