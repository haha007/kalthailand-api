package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author khoi.tran on 9/30/16.
 */
public class ObjectUtil {
    /**
     * @param a
     * @param b
     * @return
     * @deprecated You may want to use {@link EqualsBuilder#reflectionEquals(Object, Object, String...)} instead.
     */
    @Deprecated
    public static boolean equalsAllFields(Object a, Object b) {
        if (a == b) return true;
        if (a == null && b == null) return true;
        if (a == null || a.getClass() != b.getClass()) return false;
        Class<? extends Object> clazz = a.getClass();
        EqualsBuilder equalsBuilder = new EqualsBuilder().appendSuper(a.equals(b));
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method getter = propertyDescriptor.getReadMethod();
                Object fieldValueOfObjectA = getter.invoke(a);
                Object fieldValueOfObjectB = getter.invoke(b);
                equalsBuilder.append(fieldValueOfObjectA, fieldValueOfObjectB);
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            throw new UnexpectedException("Cannot compare two object: objA: " + a + ", objB: " + b, e);
        }
        return equalsBuilder.isEquals();
    }
}
