package th.co.krungthaiaxa.api.elife.test.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import th.co.krungthaiaxa.api.elife.utils.Decrypt;

public class DecryptTest {
    private String secretkey = "c30b5f5f92d5b66d39a5c9b4787c87bf";

    @Test
    public void should_decrypt_successfully() throws Exception {
        String result = Decrypt.decrypt("CmknoozyMpJSi3pzrA6bbpVa8zrx7uBe28zP/lNi//CxTXNlxIPat1xUmBRWTRH3hZT9wkXpV8PJmEP4Gc11v8IseAY88sDud1H4sX63cUku/jPGx9erFweiBSw8l4yEyUAZ7YS+/q6TX43GkWOEXM9JCu9qPCcNm/RB6k0WE+YW6CpFpnW82D8X8aEU3Jz0Q9OLiIrrBVrTQSBzxz+RrC14r18lNDjFsi5Lv5ZefmLjGxgHogU1pCvDwsookbUR1zxNcYmspuQ+5XQcGpLOEbTQbbFXJaT2nAYZKXDAv46z9lZDr1YPRzNQObA9PS3N7nPT7fjg5TxHyL2pYQVkDg==", secretkey);
        Assertions.assertThat(result).isNotNull();
    }

    @Test
    public void should_decrypt_successfully_URL_encoded_string() throws Exception {
        String result = Decrypt.decrypt("CmknoozyMpJSi3pzrA6bbpVa8zrx7uBe28zP/lNi//CxTXNlxIPat1xUmBRWTRH3hZT9wkXpV8PJmEP4Gc11v8IseAY88sDud1H4sX63cUku/jPGx9erFweiBSw8l4yEyUAZ7YS%2B/q6TX43GkWOEXM9JCu9qPCcNm/RB6k0WE%2BYW6CpFpnW82D8X8aEU3Jz0Q9OLiIrrBVrTQSBzxz%2BRrC14r18lNDjFsi5Lv5ZefmLjGxgHogU1pCvDwsookbUR1zxNcYmspuQ%2B5XQcGpLOEbTQbbFXJaT2nAYZKXDAv46z9lZDr1YPRzNQObA9PS3N7nPT7fjg5TxHyL2pYQVkDg%3D%3D", secretkey);
        Assertions.assertThat(result).isNotNull();
    }
}
