package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.service.CollectionFileProcessingService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static th.co.krungthaiaxa.api.common.utils.JsonUtil.getJson;

@RestController
@Api(value = "RLS")
public class CollectionFileResource {
    private final static Logger logger = LoggerFactory.getLogger(CollectionFileResource.class);

    private final CollectionFileProcessingService collectionFileProcessingService;

    @Inject
    public CollectionFileResource(CollectionFileProcessingService collectionFileProcessingService) {
        this.collectionFileProcessingService = collectionFileProcessingService;
    }

    @ApiOperation(value = "Upload Collection files", notes = "Uploads a collection file and check for content validity", response = String.class)
    @RequestMapping(value = "/RLS/collectionFile", produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public ResponseEntity<byte[]> uploadCollectionFile(@RequestParam("file") MultipartFile file) {
        try {
            collectionFileProcessingService.importCollectionFile(file.getInputStream());
        } catch (IOException | IllegalArgumentException e) {
            return new ResponseEntity<>(getJson(ErrorCode.INVALID_COLLECTION_FILE.apply(e.getMessage())), NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(getJson(""), CREATED);
    }

    @ApiOperation(value = "Get Collection files", notes = "Get a list of all collection files", response = CollectionFile.class, responseContainer = "List")
    @RequestMapping(value = "/RLS/collectionFile", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getCollectionFiles() {
        logger.info("Getting all collection File");
        return new ResponseEntity<>(getJson(collectionFileProcessingService.getCollectionFiles()), OK);
    }

    @ApiOperation(value = "Get Deduction file", notes = "Get a Deduction file")
    @RequestMapping(value = "/RLS/deduction/download/{collectionFileId}", produces = APPLICATION_JSON_VALUE, method = GET)
    public void getDeductionFile(@PathVariable String collectionFileId, HttpServletResponse response) {
        logger.info("Downloading deduction File");
        CollectionFile collectionFile = collectionFileProcessingService.findOne(collectionFileId);
        if (collectionFile == null) {
            return;
        }
        byte[] excelFileContent = collectionFileProcessingService.createDeductionExcelFile(collectionFile.getDeductionFile());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(excelFileContent.length);

        String fileName = "deductionFile_" + ofPattern("yyyyMMdd_HHmmss").format(now()) + ".xlsx";
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

    @ApiOperation(value = "Get Deduction file", notes = "Get a Deduction file")
    @RequestMapping(value = "/RLS/collectionFile/process", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<CollectionFile> processCollectionFile() {
        return collectionFileProcessingService.processLatestCollectionFiles();
    }
}