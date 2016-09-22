package th.co.krungthaiaxa.api.elife.utils;

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
}
