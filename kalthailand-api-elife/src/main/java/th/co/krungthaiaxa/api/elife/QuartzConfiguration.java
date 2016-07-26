package th.co.krungthaiaxa.api.elife;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import th.co.krungthaiaxa.api.elife.cron.PolicyNumbersQuotaCheckerJob;
import th.co.krungthaiaxa.api.elife.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.elife.model.enums.DurationUnit;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

@Configuration
public class QuartzConfiguration {
    public static final Logger LOGGER = LoggerFactory.getLogger(QuartzConfiguration.class);

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

    @PreDestroy
    public void close() {
        closeSchedulerIfExist(schedulerFactoryBean());
    }
//
//    @Bean(name = "checkEnoughPolicyNumbersExecuteMethod")
//    public MethodInvokingJobDetailFactoryBean testAExecuteMethod() {
//        return QuartzUtils.methodInvokingJobDetailFactoryBean("checkEnoughPolicyNumbers", "execute");
//    }
//
//    @Bean(name = "checkEnoughPolicyNumbersTriggerFactory")
//    public CronTriggerFactoryBean testATriggerFactory() {
//        return QuartzUtils.initCronTriggerFactoryBean(testAExecuteMethod().getObject(), "0/5 * * * * ?");
//    }
//
//    @Bean(name = "testBExecuteMethod")
//    public MethodInvokingJobDetailFactoryBean testBExecuteMethod() {
//        return QuartzUtils.methodInvokingJobDetailFactoryBean("testB", "execute");
//    }
//
//    @Bean(name = "testBTriggerFactory")
//    public SimpleTriggerFactoryBean testBTriggerFactory() {
//        return QuartzUtils.initSimpleTriggerFactoryBean(testBExecuteMethod().getObject(), null, 3000L, null);
//    }

    @Inject
    private PolicyNumbersQuotaCheckerJob checkEnoughPolicyNumbers;

    @Bean
    public SimpleConfigTriggerFactoryBean simpleConfigTriggerFactoryBean() {
        SimpleConfigTriggerFactoryBean simpleConfigTriggerFactoryBean = new SimpleConfigTriggerFactoryBean();
        simpleConfigTriggerFactoryBean.setIntervalUnit(DurationUnit.SECOND);
        simpleConfigTriggerFactoryBean.setIntervalValue(1);
        simpleConfigTriggerFactoryBean.setTargetObject(checkEnoughPolicyNumbers);
        return simpleConfigTriggerFactoryBean;
    }
}