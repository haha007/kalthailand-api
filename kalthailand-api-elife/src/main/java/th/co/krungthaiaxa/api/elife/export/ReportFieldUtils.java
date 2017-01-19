package th.co.krungthaiaxa.api.elife.export;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author khoi.tran on 12/26/16.
 */
public class ReportFieldUtils {

    public static List<ReportFieldDescription> analyseReportFieldDescriptions(Class<?> objectClass) {
        List<ReportFieldDescription> reportFieldDescriptions = new ArrayList<>();
        Field[] fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            ReportField reportField = field.getAnnotation(ReportField.class);
            String reportFieldName;
            if (reportField != null && StringUtils.isNotBlank(reportField.value())) {
                reportFieldName = reportField.value();
            } else {
                reportFieldName = StringUtil.toCamelCaseWords(field.getName());
            }

            ReportFieldDescription reportFieldDescription = new ReportFieldDescription();
            reportFieldDescription.setField(field);
            reportFieldDescription.setFieldDisplayName(reportFieldName);
            reportFieldDescriptions.add(reportFieldDescription);
        }
        return reportFieldDescriptions;
    }

    public static List<ReportFieldValue> analyseReportFieldValues(Object object) {
        List<ReportFieldDescription> reportFieldDescriptions = analyseReportFieldDescriptions(object.getClass());
        List<ReportFieldValue> reportFieldValues = new ArrayList<>();
        try {
            for (ReportFieldDescription reportFieldDescription : reportFieldDescriptions) {
                Object value = PropertyUtils.getProperty(object, reportFieldDescription.getField().getName());
                ReportFieldValue reportFieldValue = new ReportFieldValue();
                reportFieldValue.setFieldDisplayName(reportFieldDescription.getFieldDisplayName());
                reportFieldValue.setValue(value);
                reportFieldValues.add(reportFieldValue);
            }
            return reportFieldValues;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            String msg = String.format("Cannot access field of object: %s", e.getMessage(), e);
            throw new UnexpectedException(msg);
        }
    }
}
