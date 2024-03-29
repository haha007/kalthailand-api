package th.co.krungthaiaxa.api.elife.generalsetting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.StringUtil;

import java.util.List;

/**
 * @author khoi.tran on 8/29/16.
 */
@Service
public class GeneralSettingService {
    private final GeneralSettingRepository generalSettingRepository;
    //    @Value("${line.app.id}")
//    private String lineAppId;
//
//    @Value("${payment.retry.link.contextpath}")
//    private String defaultRetryPaymentContextPath;
//
    @Value("line://ch/${line.app.id}/${payment.retry.link.contextpath}")
    private String defaultRetryPaymentLink;

    @Value("${payment.retry.tosuccessemails}")
    private String defaultRetryPaymentSuccessToEmailsString;

    @Autowired
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

    public void saveSetting(GeneralSetting generalSetting) {
        generalSettingRepository.save(generalSetting);
    }

    private GeneralSetting addDefaultSetting() {
        GeneralSetting generalSetting = new GeneralSetting();
        GeneralSetting.RetryPaymentSetting retryPaymentSetting = new GeneralSetting.RetryPaymentSetting();
        retryPaymentSetting.setRetryLink(defaultRetryPaymentLink);
//        retryPaymentSetting.setRetryLink(initDefaultRetryPaymentLink());
        List<String> paymentSuccessToEmails = StringUtil.splitToNotNullStrings(defaultRetryPaymentSuccessToEmailsString);
        retryPaymentSetting.setToSuccessEmails(paymentSuccessToEmails);
        generalSetting.setRetryPaymentSetting(retryPaymentSetting);
        generalSetting = generalSettingRepository.save(generalSetting);
        return generalSetting;
    }

//    private String initDefaultRetryPaymentLink() {
//        return String.format("line://ch/%s/%s", lineAppId, defaultRetryPaymentContextPath);
//    }

}
