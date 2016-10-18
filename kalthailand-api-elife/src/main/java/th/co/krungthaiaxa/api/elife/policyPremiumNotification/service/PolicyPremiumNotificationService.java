package th.co.krungthaiaxa.api.elife.policyPremiumNotification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.exception.SMSException;
import th.co.krungthaiaxa.api.elife.model.PolicyCDB;
import th.co.krungthaiaxa.api.elife.model.sms.SMSResponse;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.model.PolicyPremiumNoticeRequest;
import th.co.krungthaiaxa.api.elife.policyPremiumNotification.model.PolicyPremiumNoticeSMSRequest;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailHelper;
import th.co.krungthaiaxa.api.elife.service.ElifeEmailService;
import th.co.krungthaiaxa.api.elife.service.SMSApiService;
import th.co.krungthaiaxa.api.elife.utils.EmailUtil;

import java.util.List;

/**
 * @author khoi.tran on 10/17/16.
 *         This service will notify client about his premium amount need to be paid in a policy.
 */
@Service
public class PolicyPremiumNotificationService {
    private final PolicyCDBService policyCDBService;
    private final SMSApiService smsApiService;
    private final ElifeEmailService elifeEmailService;
    private final ElifeEmailHelper elifeEmailHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    public PolicyPremiumNotificationService(PolicyCDBService policyCDBService, SMSApiService smsApiService, ElifeEmailService elifeEmailService, ElifeEmailHelper elifeEmailHelper, ObjectMapper objectMapper) {
        this.policyCDBService = policyCDBService;
        this.smsApiService = smsApiService;
        this.elifeEmailService = elifeEmailService;
        this.elifeEmailHelper = elifeEmailHelper;
        this.objectMapper = objectMapper;
    }

    public void sendSMS(PolicyPremiumNoticeSMSRequest policyPremiumNoticeSMSRequest) {
        PolicyCDB policy = policyCDBService.validateExistByPolicyNumberAndMainInsuredDOB(policyPremiumNoticeSMSRequest.getPolicyNumber(), policyPremiumNoticeSMSRequest.getInsuredDob());
        String phoneNumber = policy.getMainInsured().getMobilePhone();
        String messageTemplate = IOUtil.loadTextFileInClassPath("/policy-premium/premium-notice-sms.txt");
        String message = fillNoticeSMS(messageTemplate, policyPremiumNoticeSMSRequest, policy);
        SMSResponse smsResponse = smsApiService.sendMessage(phoneNumber, message);
        if (!smsResponse.getStatus().equals(SMSResponse.STATUS_SUCCESS)) {
            throw new SMSException("Sending SMS not success", smsResponse);
        }
    }

    private String fillNoticeSMS(String messageTemplate, PolicyPremiumNoticeSMSRequest policyPremiumNoticeSMSRequest, PolicyCDB policy) {
        return messageTemplate
                .replaceAll("%COMPANY_CODE%", policyPremiumNoticeSMSRequest.getCompanyCode())
                .replaceAll("%POLICY_NUMBER%", policy.getPolicyNumber())
                .replaceAll("%DUE_DATE%", elifeEmailHelper.toNormalDate(policy.getDueDate()))
                .replaceAll("%PREMIUM_AMOUNT%", elifeEmailHelper.toCurrencyValue(policy.getPremiumValue()))
                ;
    }

    public void sendEmail(PolicyPremiumNoticeRequest policyPremiumNoticeRequest) {
        PolicyCDB policy = policyCDBService.validateExistByPolicyNumberAndMainInsuredDOB(policyPremiumNoticeRequest.getPolicyNumber(), policyPremiumNoticeRequest.getInsuredDob());
        String toEmail = policy.getMainInsured().getEmail();

        //TODO need to be updated
        String emailSubject = "Policy Premium Notification";
        String emailTemplate = IOUtil.loadTextFileInClassPath("/policy-premium/premium-notice-email.html");
        String emailContent = emailTemplate;//fillNotificationEmail(emailTemplate, policy);

        byte[] premiumNoticePdfBytes = exportPdf(policyPremiumNoticeRequest, policy);
        List<Pair<byte[], String>> attachment = EmailUtil.initAttachment("premium-notice.pdf", premiumNoticePdfBytes);
        elifeEmailService.sendEmail(toEmail, emailSubject, emailContent, EmailUtil.initImagePairs("logo"), attachment);
    }

//    private String fillNotificationEmail(String emailTemplate, PolicyCDB policy) {
//
//        Insured mainInsured = ProductUtils.validateExistMainInsured(policy);
//        Amount premiumAmount = ProductUtils.getPremiumAmount(policy);
//        Periodicity periodicity = ProductUtils.getPremiumPeriodicity(policy);
//        String mainInsuredFullName = PersonUtil.getFullName(mainInsured.getPerson());
//        //TODO need to be updated (must be passed from client)
//        String companyCode = "000";
//
//        String emailContent = emailTemplate;
//        return emailContent.replace("%DUE_DATE%", elifeEmailHelper.toThaiYear(policy.getCreationDateTime()))
//                .replace("%PREMIUM_PERIODICITY%", elifeEmailHelper.toThaiPaymentMode(periodicity))
//                .replace("%PREMIUM_VALUE%", elifeEmailHelper.toCurrencyValue(premiumAmount.getValue()))
//                ;
//    }

    //TODO pdf will be generated by RLS Service, we don't need to generate it by ourselves.
    public byte[] exportPdf(PolicyPremiumNoticeRequest policyPremiumNoticeRequest) {
        PolicyCDB policy = policyCDBService.validateExistByPolicyNumberAndMainInsuredDOB(policyPremiumNoticeRequest.getPolicyNumber(), policyPremiumNoticeRequest.getInsuredDob());
        return exportPdf(policyPremiumNoticeRequest, policy);
    }

    //TODO this is just sample file, we will need detail implementation later.
    public byte[] exportPdf(PolicyPremiumNoticeRequest policyPremiumNoticeRequest, PolicyCDB policyCDB) {
        return IOUtil.loadBinaryFileInClassPath("/policy-premium/premium-notice-pdf-mock.pdf");
    }
//
//    private PdfDataSource toPdfDataSource(Policy policy) {
//        PdfDataSource dataSource = new PdfDataSource();
//        //TODO get data from policy and then input into dataSource.
//        //The field name of dataSource must match with the field name inside jasper template file.
//
//    }
//
//    public static class PdfDataSource {
//
//    }
}
