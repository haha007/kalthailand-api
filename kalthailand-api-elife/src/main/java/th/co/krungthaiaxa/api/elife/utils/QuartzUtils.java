package th.co.krungthaiaxa.api.elife.utils;

import org.quartz.JobDetail;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author khoi.tran on 7/26/16.
 */
public class QuartzUtils {
    public static MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean(String beanName, String methodName) {
        MethodInvokingJobDetailFactoryBean obj = new MethodInvokingJobDetailFactoryBean();
        obj.setTargetBeanName(beanName);
        obj.setTargetMethod(methodName);
        return obj;
    }

    public static CronTriggerFactoryBean initCronTriggerFactoryBean(JobDetail jobDetail, String cronExpression) {
        CronTriggerFactoryBean stFactory = new CronTriggerFactoryBean();
        stFactory.setJobDetail(jobDetail);
        stFactory.setCronExpression(cronExpression);
        return stFactory;
    }

    public static SimpleTriggerFactoryBean initSimpleTriggerFactoryBean(JobDetail jobDetail, Long startDelay, Long repeatInterval, Integer repeatCount) {
        SimpleTriggerFactoryBean stFactory = new SimpleTriggerFactoryBean();
        stFactory.setJobDetail(jobDetail);
        if (startDelay != null) {
            stFactory.setStartDelay(startDelay);
        }
        if (repeatInterval != null) {
            stFactory.setRepeatInterval(repeatInterval);
        }
        if (repeatCount != null) {
            stFactory.setRepeatCount(repeatCount);
        }
        return stFactory;
    }
}
