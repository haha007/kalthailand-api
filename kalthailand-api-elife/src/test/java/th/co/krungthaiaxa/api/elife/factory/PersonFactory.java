package th.co.krungthaiaxa.api.elife.factory;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.model.Person;
import th.co.krungthaiaxa.api.elife.model.Quote;

import java.time.LocalDate;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class PersonFactory {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    public static void setValuesToFirstInsuredPerson(Quote quote, String name, String email) {
        setDefaultValueToPerson(quote.getInsureds().get(0).getPerson(), name, email);
    }

    public static void setValuesToFirstInsuredPerson(Quote quote, int age, String name, String email) {
        setDefaultValueToPerson(quote.getInsureds().get(0).getPerson(), age, name, email);
    }

    public static void setDefaultValueToPerson(Person person, String name, String email) {
        person.setGivenName(name);
        person.setEmail(email);
    }

    public static void setDefaultValueToPerson(Person person, int age, String name, String email) {
        LocalDate now = LocalDate.now();
        LocalDate birthDate = now.minusYears(age);
        person.setBirthDate(birthDate);
        person.setGivenName(name);
        person.setEmail(email);
    }
}
