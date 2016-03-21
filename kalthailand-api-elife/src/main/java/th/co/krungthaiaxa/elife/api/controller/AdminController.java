package th.co.krungthaiaxa.elife.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.service.PolicyService;

import javax.inject.Inject;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.POLICY_DOES_NOT_EXIST;
import static th.co.krungthaiaxa.elife.api.utils.JsonUtil.getJson;

@ApiIgnore
@Controller
public class AdminController {
    private final PolicyService policyService;

    @Inject
    public AdminController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @RequestMapping(value = "/admin", method = GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/admin/collectionFile", method = GET)
    public String collectionFile() {
        return "collectionFile";
    }

    @RequestMapping(value = "/admin/policyValidation", method = GET)
    public String validatePolicy() {
        return "policyValidation";
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies/{policyId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getPolicy(@PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (policy.isPresent()) {
            return new ResponseEntity<>(getJson(policy.get()), OK);
        }
        else {
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/all", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity getAllPolicies(@RequestParam Integer startIndex, @RequestParam Integer nbOfRecords) {
        return new ResponseEntity<>(getJson(policyService.findAll(startIndex, nbOfRecords)), OK);
    }
}
