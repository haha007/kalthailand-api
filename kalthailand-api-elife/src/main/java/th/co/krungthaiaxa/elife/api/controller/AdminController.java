package th.co.krungthaiaxa.elife.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus.*;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.elife.api.utils.JsonUtil.getJson;

@ApiIgnore
@Controller
public class AdminController {
    private final PolicyService policyService;

    @Inject
    public AdminController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies/validate/{policyId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPolicy(@PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        } else if (policy.get().getStatus().equals(CANCELED)) {
            return new ResponseEntity<>(getJson(POLICY_IS_CANCELED.apply(policyId)), NOT_ACCEPTABLE);
        } else if (policy.get().getStatus().equals(PENDING_PAYMENT)) {
            return new ResponseEntity<>(getJson(POLICY_IS_PENDING_PAYMENT.apply(policyId)), NOT_ACCEPTABLE);
        } else if (policy.get().getStatus().equals(VALIDATED)) {
            return new ResponseEntity<>(getJson(POLICY_IS_VALIDATED.apply(policyId)), NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity<>(getJson(policy.get()), OK);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/all", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getAllPolicies(@RequestParam Integer startIndex, @RequestParam Integer nbOfRecords) {
        return new ResponseEntity<>(getJson(policyService.findAll(startIndex, nbOfRecords)), OK);
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/check/access/autopay", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> checkAccessRightsAutopay() {
        Optional<? extends GrantedAuthority> role = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .filter(tmp -> tmp.getAuthority().contains("ADMIN") || tmp.getAuthority().contains("AUTOPAY"))
                .findAny();
        if (role.isPresent()) {
            return new ResponseEntity<>(getJson(""), ACCEPTED);
        }
        else {
            return new ResponseEntity<>(getJson(UI_UNAUTHORIZED), UNAUTHORIZED);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/check/access/validation", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> checkAccessRightsValidation() {
        Optional<? extends GrantedAuthority> role =  SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .filter(tmp -> tmp.getAuthority().contains("ADMIN") || tmp.getAuthority().contains("VALIDATION"))
                .findAny();
        if (role.isPresent()) {
            return new ResponseEntity<>(getJson(""), ACCEPTED);
        }
        else {
            return new ResponseEntity<>(getJson(UI_UNAUTHORIZED), UNAUTHORIZED);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/check/access/dashboard", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> checkAccessRightsDashboard() {
        Optional<? extends GrantedAuthority> role =  SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .filter(tmp -> tmp.getAuthority().contains("ADMIN"))
                .findAny();
        if (role.isPresent()) {
            return new ResponseEntity<>(getJson(""), ACCEPTED);
        }
        else {
            return new ResponseEntity<>(getJson(UI_UNAUTHORIZED), UNAUTHORIZED);
        }
    }

}
