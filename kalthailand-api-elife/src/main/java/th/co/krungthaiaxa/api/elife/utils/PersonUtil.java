package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.StringUtils;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.StringUtil;
import th.co.krungthaiaxa.api.elife.model.Person;

/**
 * @author khoi.tran on 9/22/16.
 */
public class PersonUtil {
    public static String getFullName(Person person) {
        if (person == null) {
            return null;
        }
        return StringUtil.joinNotBlankStrings(" ", person.getGivenName(), person.getMiddleName(), person.getSurName());
    }

    public static String getFirstNameAndLastName(Person person) {
        if (person == null) {
            return null;
        }
        return StringUtil.joinNotBlankStrings(" ", person.getGivenName(), person.getSurName());
    }

    public static String validateExistMobilePhoneNumber(Person person) {
        String mobilePhoneNumber = person.getMobilePhoneNumber().getNumber();
        if (StringUtils.isBlank(mobilePhoneNumber)) {
            throw new UnexpectedException("Not exist mobile phone number of person: \n " + ObjectMapperUtil.toStringMultiLine(person));
        } else {
            return mobilePhoneNumber;
        }
    }
}
