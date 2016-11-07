package th.co.krungthaiaxa.api.elife.test;

import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.filter.ExceptionTranslator;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.FieldError;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.exception.PolicyValidationException;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class BeanValidationExceptionTranslatorTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BeanValidationExceptionTranslatorTest.class);

    @Inject
    BeanValidator beanValidator;

    @Inject
    ExceptionTranslator exceptionTranslator;

    @Test
    public void test_constructor_of_BeanValidationException_must_enough_arguments() {
        try {
            beanValidator.validate(new ValidationTarget(), PolicyValidationException.class);
            Assert.fail("Above method must throw Exception, it should not run here.");
        } catch (PolicyValidationException ex) {
            Assert.assertNotNull(ex.getErrorTarget());
            Assert.assertTrue(ex.getViolations().size() > 0);

            Error error = exceptionTranslator.processUnknownInternalException(ex);
            Assert.assertTrue(error.getFieldErrors().size() >= 0);
            for (FieldError fieldError : error.getFieldErrors()) {
                Assert.assertTrue(fieldError.getField().contains("notNullField"));
            }
        } catch (Exception ex) {
            Assert.fail("The exception must be catched before, it should not run here.");
        }
    }

    public static class ValidationTarget {
        @NotEmpty
        private String notNullField;

        public String getNotNullField() {
            return notNullField;
        }

        public void setNotNullField(String notNullField) {
            this.notNullField = notNullField;
        }
    }
}
