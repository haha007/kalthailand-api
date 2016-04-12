package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;
import th.co.krungthaiaxa.elife.api.model.error.ErrorCode;
import th.co.krungthaiaxa.elife.api.service.RLSService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.elife.api.utils.JsonUtil.getJson;

@RestController
@Api(value = "RLS")
public class RLSResource {
    private final static Logger logger = LoggerFactory.getLogger(RLSResource.class);

    private final RLSService rlsService;

    @Inject
    public RLSResource(RLSService rlsService) {
        this.rlsService = rlsService;
    }

    @ApiOperation(value = "Upload Collection files", notes = "Uploads a collection file and check for content validity", response = String.class)
    @RequestMapping(value = "/RLS/collectionFile", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> uploadCollectionFile(@RequestParam("file") MultipartFile file) {
        try {
            rlsService.importCollectionFile(file.getInputStream());
        } catch (IOException | IllegalArgumentException e) {
            return new ResponseEntity<>(getJson(ErrorCode.INVALID_COLLECTION_FILE.apply(e.getMessage())), NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(getJson(""), CREATED);
    }

    @ApiOperation(value = "Get Collection files", notes = "Get a list of all collection files", response = CollectionFile.class, responseContainer = "List")
    @RequestMapping(value = "/RLS/collectionFile", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getCollectionFiles() {
        logger.info("Getting all collection File");
        return new ResponseEntity<>(getJson(rlsService.getCollectionFiles()), OK);
    }

    @ApiOperation(value = "Get Deduction file", notes = "Get a Deduction file")
    @RequestMapping(value = "/RLS/deduction/download/{collectionFileId}", produces = APPLICATION_JSON_VALUE, method = GET)
    public void getDeductionFile(@PathVariable String collectionFileId, HttpServletResponse response) {
        logger.info("Downloading deduction File");
        CollectionFile collectionFile = rlsService.findOne(collectionFileId);
        byte[] excelFileContent = rlsService.createDeductionExcelFile(collectionFile.getDeductionFile());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(excelFileContent.length);

        String fileName = "deductionFile_" + ofPattern("yyyyMMdd_hhmmss").format(now()) + ".xlsx";
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(excelFileContent, outStream);
        } catch (IOException e) {
            logger.error("Unable to download the deduction file", e);
        }
    }
}
