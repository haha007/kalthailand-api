package th.co.krungthaiaxa.ebiz.api.utils;

import com.aspose.ocr.ImageStreamFormat;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

public class OCRUtilTest {

    @Test
    public void should_get_text_from_image_that_is_just_a_number() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/justNumber.png"), ImageStreamFormat.Png);
        Assertions.assertThat(result).contains("1 2345 67890 12 3");
    }

    @Test
    public void should_get_text_from_image_that_is_thai_text_and_a_number() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/thaiTextWithNumber.png"), ImageStreamFormat.Png);
        Assertions.assertThat(result).contains("1 2345 67890 12 3");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_1() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/1 2345 67890 12 3.png"), ImageStreamFormat.Png);
        Assertions.assertThat(result).contains("1 2345 67890 12 3");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_2() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/0 0000 00000 00 0.jpg"), ImageStreamFormat.Jpg);
        Assertions.assertThat(result).contains("0 0000 00000 00 0");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_3() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/1 2345 67891 31 8.gif"), ImageStreamFormat.Gif);
        Assertions.assertThat(result).contains("1 2345 67891 31 8");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_4() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/1 4804 00019 16 1.jpg"), ImageStreamFormat.Jpg);
        Assertions.assertThat(result).contains("1 4804 00019 16 1");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_5() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/3 1024 00480 39 1.jpg"), ImageStreamFormat.Jpg);
        Assertions.assertThat(result).contains("3 1024 00480 39 1");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_6() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/3 2001 01216 66 4.jpg"), ImageStreamFormat.Jpg);
        Assertions.assertThat(result).contains("3 2001 01216 66 4");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_7() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/3 8204 00154 48 7.jpg"), ImageStreamFormat.Jpg);
        Assertions.assertThat(result).contains("3 8204 00154 48 7");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_8() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/33405 0147 60 0.jpg"), ImageStreamFormat.Jpg);
        Assertions.assertThat(result).contains("33405 0147 60 0");
    }

    @Test
    @Ignore
    public void should_get_text_from_full_image_9() throws Exception {
        String result = OCRUtil.extractText(this.getClass().getResourceAsStream("/images/IMG_2138.jpg"), ImageStreamFormat.Jpg);
        Assertions.assertThat(result).contains("3 1203 00153 83 3");
    }
}
