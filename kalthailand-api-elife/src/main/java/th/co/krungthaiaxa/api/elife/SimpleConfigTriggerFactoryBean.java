package th.co.krungthaiaxa.api.elife;

import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.model.enums.DurationUnit;

import javax.annotation.PostConstruct;
import java.text.ParseException;

public class SimpleConfigTriggerFactoryBean extends org.springframework.scheduling.quartz.CronTriggerFactoryBean {

    public static final Logger log = LoggerFactory.getLogger(SimpleConfigTriggerFactoryBean.class);

    private DurationUnit intervalUnit;
    private Integer intervalValue;

    private String cronExpression;

    public void setIntervalUnit(DurationUnit randomIntervalUnit) {
        this.intervalUnit = randomIntervalUnit;
    }

    public void setIntervalValue(Integer randomInterval) {
        this.intervalValue = randomInterval;
    }

    /**
     * Note:
     * InitSequenceBean: constructor
     * InitSequenceBean: postConstruct
     * InitSequenceBean: afterPropertiesSet
     * InitSequenceBean: init-method
     */
    @PostConstruct
    public void initialize() {
        if (intervalUnit != null && intervalValue != null) {
            this.cronExpression = setInterval(this.intervalUnit, this.intervalValue);
        }
    }

    @Override
    public void afterPropertiesSet() throws ParseException {
        super.afterPropertiesSet();
        log.info("Initiated CronTrigger '{}' with Cron Expression '{}'", super.getObject().getJobKey(), this.cronExpression);
    }

    public void setTargetObject(Object targetObject) {
        JobDetail jobDetail = initJobDetail(targetObject, "execute");
        super.setJobDetail(jobDetail);
    }

    private JobDetail initJobDetail(Object targetObject, String methodName) {
        MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactory.setName(targetObject.getClass().getSimpleName());
        jobDetailFactory.setTargetMethod(methodName);
        jobDetailFactory.setTargetObject(targetObject);
        try {
            jobDetailFactory.afterPropertiesSet();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            String msg = String.format("TargetObject %s doesn't has method %s", targetObject.getClass().getSimpleName(), methodName);
            throw new UnexpectedException(msg, e);
        }
        return jobDetailFactory.getObject();
    }

    private String setInterval(DurationUnit intervalUnit, int intervalValue) {
        switch (intervalUnit) {
        case SECOND:
            return setIntervalBySecond(intervalValue);
        case MINUTE:
            return setIntervalByMinute(intervalValue);
        case HOUR:
            return setIntervalByHour(intervalValue);
        case DAY:
            return setIntervalByDay(intervalValue);
        default:
            return null;
        }
    }

    /**
     * @param intervalSecond
     * @return cronExpression
     */
    private String setIntervalBySecond(int intervalSecond) {
        int initSecond = 0;//RandomUtil.randomInt(0, intervalSecond);
        int loop = intervalSecond;
        if (intervalSecond == 0) {
            loop = 1;
        }
        String cronExpression = String.format("%s/%s * * * * ?", initSecond, loop);
        super.setCronExpression(cronExpression);
        return cronExpression;
    }

    private String setIntervalByMinute(int intervalMinute) {
        int initSecond = 0;//RandomUtil.randomInt(0, 60);
        int initMinute = 0;//RandomUtil.randomInt(0, intervalMinute);
        int loop = intervalMinute;
        if (intervalMinute == 0) {
            loop = 1;
        }
        String cronExpression = String.format("%s %s/%s * * * ?", initSecond, initMinute, loop);
        super.setCronExpression(cronExpression);
        return cronExpression;
    }

    /**
     * Note: don't use initHour is 1, view more at this link
     * http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger (view the Note)
     *
     * @param intervalHour
     * @return
     */
    private String setIntervalByHour(int intervalHour) {
        int initSecond = 0;//RandomUtil.randomInt(0, 60);
        int initMinute = 0;//RandomUtil.randomInt(0, 60);
        int initHour = 1;//RandomUtil.randomInt(1, intervalHour);
        int loop = intervalHour;
        if (intervalHour == 0) {
            loop = 1;
        }
        String cronExpression = String.format("%s %s %s/%s * * ?", initSecond, initMinute, initHour, loop);
        super.setCronExpression(cronExpression);
        return cronExpression;
    }

    /**
     * Note: don't use initHour is 1, view more at this link
     * http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger (view the Note)
     *
     * @param intervalDay
     * @return
     */
    private String setIntervalByDay(int intervalDay) {
        int initSecond = 0;//RandomUtil.randomInt(0, 60);
        int initMinute = 0;//RandomUtil.randomInt(0, 60);
        int initHour = 1;//RandomUtil.randomInt(1, 24);
        int initDay = 1;//RandomUtil.randomInt(1, intervalDay + 1);
        String cronExpression = String.format("%s %s %s %s/%s * ?", initSecond, initMinute, initHour, initDay, intervalDay);
        super.setCronExpression(cronExpression);
        return cronExpression;
    }

}
