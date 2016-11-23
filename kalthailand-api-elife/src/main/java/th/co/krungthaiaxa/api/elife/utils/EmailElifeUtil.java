package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * @author khoi.tran on 8/31/16.
 */
public class EmailElifeUtil {
    public static final String EMAIL_IMAGE_FOLDER = "/images/email/";

//    public static List<Pair<byte[], String>> getDefaultImagePairs() {
//        Map<String, String> imagesMap = new HashMap<>();
//        imagesMap.put("<imageElife>", EMAIL_IMAGE_FOLDER + "logo.png");
//        imagesMap.put("<imgF>", EMAIL_IMAGE_FOLDER + "facebook-logo.png");
//        imagesMap.put("<imgT>", EMAIL_IMAGE_FOLDER + "twitter-logo.png");
//        imagesMap.put("<imgY>", EMAIL_IMAGE_FOLDER + "youtube-logo.png");
//        return th.co.krungthaiaxa.api.common.utils.EmailUtil.createBase64ImagePairs(imagesMap);
//    }
//
//    public static List<Pair<byte[], String>> initImagePairs(String... imageNames) {
//        Map<String, String> imagesMap = new HashMap<>();
//        for (String imageName : imageNames) {
//            imagesMap.put("<image" + imageName + ">", EMAIL_IMAGE_FOLDER + imageName + ".png");
//        }
//        return th.co.krungthaiaxa.api.common.utils.EmailUtil.createBase64ImagePairs(imagesMap);
//    }

    public static List<Pair<byte[], String>> initAttachment(String attachmentName, byte[] attachmentData) {
        Pair pair = Pair.of(attachmentData, attachmentName);
        return Arrays.asList(pair);
    }
}
