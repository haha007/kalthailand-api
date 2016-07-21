package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.client.CDBClient;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.data.Setting;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;
import th.co.krungthaiaxa.api.elife.model.*;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;
import th.co.krungthaiaxa.api.elife.model.enums.RegistrationTypeName;
import th.co.krungthaiaxa.api.elife.model.enums.SuccessErrorStatus;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.products.Product;
import th.co.krungthaiaxa.api.elife.products.ProductFactory;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.*;
import th.co.krungthaiaxa.api.elife.tmc.TMCClient;
import th.co.krungthaiaxa.api.elife.utils.RsaUtil;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.isTrue;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;
import static th.co.krungthaiaxa.api.elife.model.enums.DocumentType.*;
import static th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus.*;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus.*;
import static th.co.krungthaiaxa.api.elife.products.ProductUtils.amount;

@Service
public class SettingService {
    private final static Logger logger = LoggerFactory.getLogger(SettingService.class);
    private static final int POLICY_QUOTA = 1000;
    @Inject
    private SettingRepository settingRepository;

    /**
     * This method is not only find policy setting. If it cannot find any setting inside DB, then it will initiate one setting in DB.
     *
     * @return
     */
    public PolicySetting loadPolicySetting() {
        Setting setting;
        List<Setting> settings = settingRepository.findAll();
        if (settings.isEmpty()) {
            setting = initDefaultSetting();
            settingRepository.save(setting);
        } else {
            if (settings.size() > 1) {
                logger.warn("We actually use only the first one setting. Other settings will be ingnored.");
            }
            setting = settings.get(0);
        }
        PolicySetting policySetting = setting.getPolicySetting();
        return policySetting;
    }

    private Setting initDefaultSetting() {
        Setting setting = new Setting();
        PolicySetting policySetting = new PolicySetting();
        policySetting.setQuotaInMonth(POLICY_QUOTA);
        setting.setPolicySetting(policySetting);
        return setting;
    }
}
