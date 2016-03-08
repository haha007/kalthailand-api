package th.co.krungthaiaxa.elife.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;
import th.co.krungthaiaxa.elife.api.service.RLSService;

import javax.inject.Inject;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
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

    @ApiOperation(value = "Get Collection files", notes = "Get a list of all collection files", response = CollectionFile.class, responseContainer = "List")
    @RequestMapping(value = "/RLS/collectionFile", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getCollectionFiles() {
        logger.info("Getting all collection File");
//        List<CollectionFile> result = new ArrayList<>();
//
//        CollectionFileLine collectionFileLine = new CollectionFileLine();
//
//        DeductionFile deductionFile = new DeductionFile();
//        deductionFile.addLine(new DeductionFileLine());
//
//        CollectionFile collectionFile = new CollectionFile();
//        collectionFile.setReceivedDate(LocalDateTime.now());
//        collectionFile.setJobEndedDate(LocalDateTime.now());
//        collectionFile.setJobStartedDate(LocalDateTime.now());
//        collectionFile.setDeductionFile(deductionFile);
//        collectionFile.addLine(collectionFileLine);
//
//        result.add(collectionFile);
//        return new ResponseEntity<>(getJson(result), OK);
        return new ResponseEntity<>(getJson(rlsService.getCollectionFiles()), OK);
    }

}
