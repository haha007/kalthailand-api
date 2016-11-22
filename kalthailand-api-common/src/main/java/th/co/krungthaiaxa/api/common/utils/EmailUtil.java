package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 7/28/16.
 */
public class EmailUtil {
    //    https://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression//**/
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-\\+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
//    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9+_.-]+$");

    public static boolean isValidEmailAddress(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    /**
     * @param imageMap key: imagePlaceHolderName, value: image relative path
     * @return
     */
    public static List<Pair<byte[], String>> createBase64ImagePairs(Map<String, String> imageMap) {
        Set<Map.Entry<String, String>> entrySet = imageMap.entrySet();
        List<Pair<byte[], String>> base64ImgFileNames = entrySet.stream()
                .map(e -> createBase64ImagePair(e))
                .collect(Collectors.toList());
        return base64ImgFileNames;
    }

    private static Pair<byte[], String> createBase64ImagePair(Map.Entry<String, String> mapEntry) {
        return Pair.of(IOUtil.loadBinaryFileInClassPath(mapEntry.getValue()), mapEntry.getKey());
    }
}
