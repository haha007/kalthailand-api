package th.co.krungthaiaxa.api.elife.urlshortener.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.urlshortener.Urlshortener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author khoi.tran on 11/29/16.
 */
@Service
public class UrlShortenerService {
    @Value("${google.api.key.default}")
    private String googleKeyApi;

    public static void main(String[] a) {
        try {
            UrlShortenerService urlShortenerService = new UrlShortenerService();
            urlShortenerService.getShortUrl("http://203.154.153.117:8080/admin-elife/admin#/policy-detail?policyID=502-4878405");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getShortUrl(String originalUrl) throws IOException {
//        UrlShortenerRequest urlShortenerRequest = new UrlShortenerRequest();
//        urlShortenerRequest.setLongUrl(originalUrl);
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyDB9_kBpncpF5xCI2188N_UGfgWCfVk1bY", urlShortenerRequest, String.class);
//        String responseContent = responseEntity.getBody();
//        System.out.print(responseContent);

        // setup up the HTTP transport
//
//        HttpTransport transport = new ApacheHttpTransport();
//        // add default headers
//        GoogleHeaders defaultHeaders = new GoogleHeaders();
//        transport.defaultHeaders = defaultHeaders;
//        transport.defaultHeaders.put("Content-Type", "application/json");
//        transport.addParser(new JsonHttpParser());
//
//        // build the HTTP GET request and URL
//        HttpRequest request = transport.buildPostRequest();
//        request.setUrl(GOOGL_URL);
//        GenericData data = new GenericData();
//        data.put("longUrl", "http://www.google.com/");
//        JsonHttpContent content = new JsonHttpContent();
//        content.data = data;
//        request.content = content;
//
//        HttpResponse response = request.execute();
//        String result = response.parseAs(String.class);
//
//        System.out.println(result);
//
//        UrlshortenerRequestInitializer urlshortenerRequestInitializer = new UrlshortenerRequestInitializer("AIzaSyDB9_kBpncpF5xCI2188N_UGfgWCfVk1bY");
////        Urlshortener urlshortener =  new Urlshortener(urlshortenerRequestInitializer);
////
////        new Urlshortener.Builder().setUrlshortenerRequestInitializer(urlshortenerRequestInitializer);
////        GoogleAccountCredential.
//        AppIdentityCredential credential =
//                new AppIdentityCredential(Arrays.asList(UrlshortenerScopes.URLSHORTENER));
        Map<String, String> key = readKey();
        String clientId = key.get("client_id");
        String clientSecret = key.get("");
        Urlshortener shortener = new Urlshortener.Builder(new ApacheHttpTransport(), new JacksonFactory(), urlshortenerRequestInitializer)
                .build();
//        UrlHistory history = shortener.URL().list().execute();
        return null;
    }

    public static Map<String, String> readKey() {
        String json = IOUtil.loadTextFileInClassPath("/google-api/AXA-kalthailand-api-8f4bf9245bd6.json");
        Map map = ObjectMapperUtil.toObject(new ObjectMapper(), json, Hashtable.class);
        return map;
    }

    public static class UrlShortenerRequest {
        private String longUrl;

        public String getLongUrl() {
            return longUrl;
        }

        public void setLongUrl(String longUrl) {
            this.longUrl = longUrl;
        }
    }
}
