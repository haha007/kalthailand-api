package th.co.krungthaiaxa.elife.api.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;
import th.co.krungthaiaxa.elife.api.model.Document;
import th.co.krungthaiaxa.elife.api.model.DocumentDownload;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.service.DocumentService;
import th.co.krungthaiaxa.elife.api.service.PolicyService;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static th.co.krungthaiaxa.elife.api.model.enums.PolicyStatus.*;
import static th.co.krungthaiaxa.elife.api.model.error.ErrorCode.*;
import static th.co.krungthaiaxa.elife.api.utils.JsonUtil.getJson;

@ApiIgnore
@Controller
public class AdminController {
    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final PolicyService policyService;
    private final DocumentService documentService;

    @Inject
    public AdminController(PolicyService policyService, DocumentService documentService) {
        this.policyService = policyService;
        this.documentService = documentService;
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/policies/{policyId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPolicy(@PathVariable String policyId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        } else if (policy.get().getStatus().equals(CANCELED)) {
            return new ResponseEntity<>(getJson(POLICY_IS_CANCELED.apply(policyId)), NOT_ACCEPTABLE);
        } else if (policy.get().getStatus().equals(PENDING_PAYMENT)) {
            return new ResponseEntity<>(getJson(POLICY_IS_PENDING_PAYMENT.apply(policyId)), NOT_ACCEPTABLE);
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
    @RequestMapping(value = "admin/policies/{policyId}/reminder/{reminderId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> sendReminder(@PathVariable String policyId, @PathVariable Integer reminderId) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return new ResponseEntity<>(getJson(POLICY_DOES_NOT_EXIST), NOT_FOUND);
        }

        try {
            switch (reminderId) {
                case 1:
                    policyService.sendNotificationsWhenUserNotRespondingToCalls(policy.get());
                    break;
                case 2:
                    policyService.sendNotificationsWhenPhoneNumberIsWrong(policy.get());
                    break;
            }
        } catch (MessagingException | IOException e) {
            return new ResponseEntity<>(getJson(NOTIFICATION_NOT_SENT.apply(e.getMessage())), INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(getJson("Notifications have been sent"), OK);
    }

    @ApiIgnore
    @RequestMapping(value = "admin/policies/{policyId}/document/{documentType}/download", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public void downloadDocument(@PathVariable String policyId, @PathVariable String documentType, HttpServletResponse response) {
        Optional<Policy> policy = policyService.findPolicy(policyId);
        if (!policy.isPresent()) {
            return;
        }

        Optional<Document> document = policy.get().getDocuments().stream().filter(tmp -> tmp.getTypeName().name().equals(documentType)).findFirst();
        if (!document.isPresent()) {
            return;
        }

        DocumentDownload documentDownload = documentService.downloadDocument(document.get().getId());
        byte[] documentContent = Base64.getDecoder().decode(documentDownload.getContent());

        response.setContentType("application/pdf");
        response.setContentLength(documentContent.length);

        String fileName = policyId + "-" + documentType + "_" + ofPattern("yyyyMMdd_hhmmss").format(now()) + ".pdf";
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

        try (OutputStream outStream = response.getOutputStream()) {
            IOUtils.write(documentContent, outStream);
        } catch (IOException e) {
            logger.error("Unable to download the document", e);
        }
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
        } else {
            return new ResponseEntity<>(getJson(UI_UNAUTHORIZED), UNAUTHORIZED);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/check/access/validation", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> checkAccessRightsValidation() {
        Optional<? extends GrantedAuthority> role = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .filter(tmp -> tmp.getAuthority().contains("ADMIN") || tmp.getAuthority().contains("VALIDATION"))
                .findAny();
        if (role.isPresent()) {
            return new ResponseEntity<>(getJson(""), ACCEPTED);
        } else {
            return new ResponseEntity<>(getJson(UI_UNAUTHORIZED), UNAUTHORIZED);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/admin/check/access/dashboard", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public ResponseEntity<byte[]> checkAccessRightsDashboard() {
        Optional<? extends GrantedAuthority> role = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .filter(tmp -> tmp.getAuthority().contains("ADMIN"))
                .findAny();
        if (role.isPresent()) {
            return new ResponseEntity<>(getJson(""), ACCEPTED);
        } else {
            return new ResponseEntity<>(getJson(UI_UNAUTHORIZED), UNAUTHORIZED);
        }
    }

}
