package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.io.IOUtils.toByteArray;

/**
 * @author khoi.tran on 7/28/16.
 */
public class EmailUtil {
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
