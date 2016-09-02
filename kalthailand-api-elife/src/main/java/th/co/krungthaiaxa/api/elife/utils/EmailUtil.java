package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author khoi.tran on 8/31/16.
 */
public class EmailUtil {
    public static List<Pair<byte[], String>> getDefaultImagePairs() {
        Map<String, String> imagesMap = new HashMap<>();
        imagesMap.put("<imageElife>", "/images/email/logo.png");
        imagesMap.put("<imgF>", "/images/email/facebook-logo.png");
        imagesMap.put("<imgT>", "/images/email/twitter-logo.png");
        imagesMap.put("<imgY>", "/images/email/youtube-logo.png");
        return th.co.krungthaiaxa.api.common.utils.EmailUtil.createBase64ImagePairs(imagesMap);
    }
}
