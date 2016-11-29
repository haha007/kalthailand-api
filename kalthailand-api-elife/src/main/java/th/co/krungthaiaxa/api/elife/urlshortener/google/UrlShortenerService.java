package th.co.krungthaiaxa.api.elife.urlshortener.google;

import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.urlshortener.Urlshortener;
import com.google.api.services.urlshortener.model.Url;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.model.cache.PermanentMemoryCache;
import th.co.krungthaiaxa.api.common.utils.ProfileHelper;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author khoi.tran on 11/29/16.
 *         Introduction: https://developers.google.com/api-client-library/java/apis/urlshortener/v1
 *         Sample Java code: https://github.com/google/google-api-java-client-samples/blob/master/urlshortener-robots-appengine-sample/
 *         Explain: https://developers.google.com/url-shortener/v1/getting_started
 */
@Service
public class UrlShortenerService {
    public static final Logger LOGGER = LoggerFactory.getLogger(UrlShortenerService.class);
    @Autowired
    private ProfileHelper profileHelper;
    public String googleAppName;

    @Value("${google.api.key.default}")
    private String googleKeyApi;

    private PermanentMemoryCache<String, String> shortenUrlCache = new PermanentMemoryCache<>();

    @PostConstruct
    public void init() {
        googleAppName = "axa-kalthailand-api_" + profileHelper.getFirstUsingProfile();
    }

    private Urlshortener getGoogleUrlShortenerService() {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleClientRequestInitializer keyInitializer = new CommonGoogleClientRequestInitializer(googleKeyApi);
        Urlshortener service = new Urlshortener.Builder(httpTransport, jsonFactory, null)
                .setGoogleClientRequestInitializer(keyInitializer)
                .setApplicationName(googleAppName)
                .build();
        return service;
    }

    public String getShortUrl(String originalUrl) {
        Urlshortener shortener = getGoogleUrlShortenerService();
        Url toInsert = new Url().setLongUrl(originalUrl);
        try {
            Url shortUrl = shortener.url().insert(toInsert).execute();
            String shortUrlString = shortUrl.getId();
            return shortUrlString;
        } catch (IOException e) {
            //We don't really need to create a specific error code for this kind of error because it doesn't need to return to client.
            throw new UnexpectedException("Google API error: " + e.getMessage(), e);
        }
    }

    /**
     * This method will never throw Exception, if there's something wrong, it will return original Url.
     *
     * @param originalUrl
     * @return
     */
    public String getShortUrlIfPossible(String originalUrl) {
        String shortUrl;
        if (StringUtils.isBlank(originalUrl)) {
            shortUrl = originalUrl;
        } else {
            try {
                shortUrl = shortenUrlCache.get(originalUrl);
                if (shortUrl == null) {
                    shortUrl = getShortUrl(originalUrl);
                }
                LOGGER.trace("Shorten Url: long: '{}', shortUrl: '{}'", originalUrl, shortUrl);
            } catch (Exception ex) {
                shortUrl = originalUrl;
                LOGGER.error("Cannot shorten Url '" + originalUrl + "': " + ex.getMessage(), ex);
            }
        }
        shortenUrlCache.put(originalUrl, shortUrl);
        return shortUrl;
    }
}
