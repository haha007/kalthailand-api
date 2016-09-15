package th.co.krungthaiaxa.api.elife.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.StringUtil;
import th.co.krungthaiaxa.api.elife.data.GeneralSetting;
import th.co.krungthaiaxa.api.elife.repository.GeneralSettingRepository;

import javax.inject.Inject;
import java.util.List;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class GeneralSettingService {
    private final GeneralSettingRepository generalSettingRepository;
    @Value("line://ch/${line.app.id}/${payment.retry.link.contextPath}")
    private String defaultRetryPaymentLink;
    @Value("${payment.retry.tosuccessemails}")
    private String defaultRetryPaymentSuccessToEmailsString;

    @Inject
    public GeneralSettingService(GeneralSettingRepository generalSettingRepository) {this.generalSettingRepository = generalSettingRepository;}

    public GeneralSetting loadGeneralSetting() {
        GeneralSetting generalSetting;
        List<GeneralSetting> generalSettings = generalSettingRepository.findAll();
        if (generalSettings.isEmpty()) {
            generalSetting = addDefaultSetting();
        } else {
            generalSetting = generalSettings.get(0);
        }
        return generalSetting;
    }

    private GeneralSetting addDefaultSetting() {
        GeneralSetting generalSetting = new GeneralSetting();
        GeneralSetting.RetryPaymentSetting retryPaymentSetting = new GeneralSetting.RetryPaymentSetting();
        retryPaymentSetting.setRetryLink(defaultRetryPaymentLink);
        List<String> paymentSuccessToEmails = StringUtil.splitToNotNullStrings(defaultRetryPaymentSuccessToEmailsString);
        retryPaymentSetting.setToSuccessEmails(paymentSuccessToEmails);
        generalSetting.setRetryPaymentSetting(retryPaymentSetting);
        generalSetting = generalSettingRepository.save(generalSetting);
        return generalSetting;
    }

}
