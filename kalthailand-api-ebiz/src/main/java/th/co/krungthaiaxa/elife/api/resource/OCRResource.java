package th.co.krungthaiaxa.elife.api.resource;

import com.aspose.ocr.ImageStreamFormat;
import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;
import th.co.krungthaiaxa.elife.api.utils.OCRUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@Api(value = "OCR")
public class OCRResource {
    @RequestMapping(value = "/ocr/validate/id", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity validateId(@RequestParam String id, @RequestParam String type, HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            return new ResponseEntity<>(ErrorCode.OCR_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> images = multipartRequest.getFiles("image");
        if (images == null || images.size() == 0) {
            return new ResponseEntity<>(ErrorCode.OCR_IMAGE_INPUT_NOT_RECEIVED, HttpStatus.BAD_REQUEST);
        }

        InputStream in;
        try {
            in = images.get(0).getInputStream();
        } catch (IOException e) {
            return new ResponseEntity<>(ErrorCode.OCR_IMAGE_INPUT_NOT_READABLE, HttpStatus.BAD_REQUEST);
        }

        int imageType;
        switch (type.toLowerCase()) {
            case "tiff":
                imageType = ImageStreamFormat.Tiff;
                break;
            case "bmp":
                imageType = ImageStreamFormat.Bmp;
                break;
            case "png":
                imageType = ImageStreamFormat.Png;
                break;
            case "jpg":
                imageType = ImageStreamFormat.Jpg;
                break;
            default:
                imageType = ImageStreamFormat.Gif;
                break;
        }

        String result;
        try {
            result = OCRUtil.extractText(in, imageType);
        } catch (Exception e) {
            return new ResponseEntity<>(ErrorCode.OCR_IMPOSSIBLE, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result.contains(id), HttpStatus.OK);
    }

}
