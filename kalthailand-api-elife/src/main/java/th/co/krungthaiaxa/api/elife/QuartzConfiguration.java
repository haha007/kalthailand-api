package th.co.krungthaiaxa.api.elife;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import th.co.krungthaiaxa.api.elife.policyNumbersQuota.cron.PolicyNumbersQuotaCheckerJob;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.model.enums.DurationUnit;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

@Configuration
public class QuartzConfiguration {
    public static final Logger LOGGER = LoggerFactory.getLogger(QuartzConfiguration.class);

    @Value("${policynumbersquota.cron.time.unit}")
    private DurationUnit policyNumbersQuotaTimeUnit;
    @Value("${policynumbersquota.cron.time.value}")
    private Integer policyNumbersQuotaTimeValue;

    @Inject
    private PolicyNumbersQuotaCheckerJob checkEnoughPolicyNumbers;

    @Bean
    public SimpleConfigTriggerFactoryBean simpleConfigTriggerFactoryBean() {
        SimpleConfigTriggerFactoryBean simpleConfigTriggerFactoryBean = new SimpleConfigTriggerFactoryBean();
        simpleConfigTriggerFactoryBean.setIntervalUnit(policyNumbersQuotaTimeUnit);
        simpleConfigTriggerFactoryBean.setIntervalValue(policyNumbersQuotaTimeValue);
        simpleConfigTriggerFactoryBean.setTargetObject(checkEnoughPolicyNumbers);
        return simpleConfigTriggerFactoryBean;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        try {
            scheduler.setWaitForJobsToCompleteOnShutdown(true);
            scheduler.setTriggers(
                    simpleConfigTriggerFactoryBean().getObject()
            );
            scheduler.setAutoStartup(true);
            return scheduler;
        } catch (Exception ex) {
            closeSchedulerIfExist(scheduler);
            throw new UnexpectedException("Cannot initiate SchedulerFactoryBean: " + ex.getMessage(), ex);
        }
    }

    @PreDestroy
    public void close() {
        closeSchedulerIfExist(schedulerFactoryBean());
    }

    private void closeSchedulerIfExist(SchedulerFactoryBean schedulerFactoryBean) {
        try {
            if (schedulerFactoryBean != null) {
                schedulerFactoryBean.stop();
                if (schedulerFactoryBean.getObject() != null) {
                    schedulerFactoryBean.destroy();
                }
                LOGGER.info("Shutdown Quartz scheduler.");
            } else {
                LOGGER.warn("Quartz scheduler is null, cannot close it.");
            }
        } catch (SchedulerException e) {
            LOGGER.error("Cannot shutdown Quartz scheduler {}", e.getMessage(), e);
        }
    }

}