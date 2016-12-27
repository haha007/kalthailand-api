package th.co.krungthaiaxa.api.elife.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author khoi.tran on 12/7/16.
 */
public class JdbcUtil {
    /**
     * @param clazz
     * @param <T>
     * @return
     * @deprecated use {@link JdbcHelper#getRowMapper(Class)}
     */
    @Deprecated
    public static <T> RowMapper<T> constructRowMapper(Class<T> clazz) {
        return new RowMapper<T>() {
            @Override
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                T instance = BeanUtils.instantiate(clazz);
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    copyFieldValueFromResultSet(rs, instance, field);
                }
                return instance;
            }
        };
    }

    private static <T> void copyFieldValueFromResultSet(ResultSet resultSet, T target, Field field) throws SQLException {
        Object value;
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        if (fieldType.isAssignableFrom(String.class)) {
            value = resultSet.getString(fieldName);
        } else if (fieldType.isAssignableFrom(Boolean.class)) {
            value = resultSet.getBoolean(fieldName);
        } else if (fieldType.isAssignableFrom(Double.class)) {
            value = resultSet.getDouble(fieldName);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            value = resultSet.getInt(fieldName);
        } else if (fieldType.isAssignableFrom(Float.class)) {
            value = resultSet.getFloat(fieldName);
        } else if (fieldType.isAssignableFrom(Long.class)) {
            value = resultSet.getLong(fieldName);
        } else if (fieldType.isAssignableFrom(Date.class)) {
            value = resultSet.getDate(fieldName);
        } else if (fieldType.isAssignableFrom(BigDecimal.class)) {
            value = resultSet.getBigDecimal(fieldName);
        } else if (fieldType.isAssignableFrom(Number.class)) {
            value = resultSet.getDouble(fieldName);
        } else if (fieldType.isAssignableFrom(Time.class)) {
            value = resultSet.getTime(fieldName);
        } else if (fieldType.isAssignableFrom(Timestamp.class)) {
            value = resultSet.getTimestamp(fieldName);
        } else if (fieldType.isAssignableFrom(Instant.class)) {
            Timestamp timestamp = resultSet.getTimestamp(fieldName);
            value = timestamp.toInstant();
        } else if (fieldType.isAssignableFrom(LocalDateTime.class)) {
            Timestamp timestamp = resultSet.getTimestamp(fieldName);
            value = timestamp.toLocalDateTime();
        } else {
            value = resultSet.getObject(fieldName);
        }
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            String msg = String.format("Cannot set value %s to field %s of target %s", value, fieldName, target);
            throw new UnexpectedException(msg, e);
        }
    }
}
