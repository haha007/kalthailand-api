package th.co.krungthaiaxa.api.elife.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.generalsetting.GeneralSetting;
import th.co.krungthaiaxa.api.elife.generalsetting.GeneralSettingService;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.urlshortener.google.UrlShortenerService;

/**
 * @author khoi.tran on 11/22/16.
 */
@Service
public class PaymentRetryLinkService {
    private final GeneralSettingService generalSettingService;
    private final UrlShortenerService urlShortenerService;

    @Autowired
    public PaymentRetryLinkService(GeneralSettingService generalSettingService, UrlShortenerService urlShortenerService) {
        this.generalSettingService = generalSettingService;
        this.urlShortenerService = urlShortenerService;
    }

    public String createPaymentLink(String policyNumber, Payment payment) {
        GeneralSetting generalSetting = generalSettingService.loadGeneralSetting();
        String retryLink = generalSetting.getRetryPaymentSetting().getRetryLink();
        if (payment != null) {
            String originalLink = String.format("%s?policyNumber=%s&paymentId=%s", retryLink, policyNumber, payment.getPaymentId());
            String shortenedUrl = urlShortenerService.getShortUrlIfPossible(originalLink);
            return shortenedUrl;
        } else {
            //We terrible sorry but cannot help user to payment again by himself: cannot generate payment link without payment and policyNumber. We need to contact and handle this case manually.
            return "";
        }
    }
}
