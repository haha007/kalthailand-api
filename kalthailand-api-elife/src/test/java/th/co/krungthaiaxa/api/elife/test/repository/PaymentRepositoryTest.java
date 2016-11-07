package th.co.krungthaiaxa.api.elife.test.repository;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.utils.EncryptUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PaymentRepositoryTest {
    Logger logger = LoggerFactory.getLogger(PaymentRepositoryTest.class);

    @Inject
    private PaymentRepository paymentRepository;

    @Test
    public void can_query_by_policyId_and_status_and_dueDate() {
        String policyId = "507-4876896";
        PaymentStatus paymentStatus = PaymentStatus.NOT_PROCESSED;
        LocalDateTime searchToDueDate = LocalDateTime.now();
        LocalDateTime searchFromDueDate = searchToDueDate.minusMonths(3);
        Optional<Payment> paymentOptional = paymentRepository.findOneByPolicyIdAndDueDateRangeAndInStatus(policyId, searchFromDueDate, searchToDueDate, paymentStatus);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            logger.debug(ObjectMapperUtil.toStringMultiLine(payment));
            Assert.assertEquals(policyId, payment.getPolicyId());
            Assert.assertTrue(paymentStatus.equals(payment.getStatus()));
            String dueDateRangeErrorMessage = String.format("DueDate must be from %s to %s: %s", searchFromDueDate, searchToDueDate, ObjectMapperUtil.toString(payment));
            Assert.assertTrue(dueDateRangeErrorMessage, !payment.getDueDate().isBefore(searchFromDueDate));
            Assert.assertTrue(dueDateRangeErrorMessage, !payment.getDueDate().isAfter(searchToDueDate));
        }
    }

    //    @Test
    public void testDecryptAllRegistrationKeysInPayments() throws NoSuchFieldException {
        Field registrationKey = Payment.class.getDeclaredField("registrationKey");
        registrationKey.setAccessible(true);
        StringBuilder finalResult = new StringBuilder();
        Sort sort = new Sort(Sort.Direction.ASC, "policyId");
        List<Payment> payments = paymentRepository.findByRegKeyNotEmpty(sort);
        for (Payment payment : payments) {
            String csvRow = reportCsvRow(registrationKey, payment);
            finalResult.append(csvRow);
        }
        File file = new File("regKey" + System.currentTimeMillis() + ".csv");
        try {
            FileUtils.writeStringToFile(file, finalResult.toString());
            logger.debug("Export csv file: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        registrationKey.setAccessible(false);
    }

    private String reportCsvRow(Field field, Payment payment) {
        String result;
        String regKeyEncryptedWithBase64 = "";
        byte[] regKeyEncryptedNoBase64Bytes = new byte[0];
        String regKeyEncryptedNoBase64 = "";
        String plainText = "";
        String regKeyNoBase64Length = "";
        try {
            regKeyEncryptedWithBase64 = (String) field.get(payment);
            regKeyEncryptedNoBase64Bytes = Base64.decodeBase64(regKeyEncryptedWithBase64);
            regKeyEncryptedNoBase64 = new String(regKeyEncryptedNoBase64Bytes);
            regKeyNoBase64Length = "" + regKeyEncryptedNoBase64Bytes.length;
            plainText = EncryptUtil.decrypt(regKeyEncryptedWithBase64);
            result = "SUCCESS";
        } catch (Exception ex) {
            result = escape(ex.getMessage());
        }

        String rowOriginalData = String.format("%s,%s,%s,%s", result, payment.getPolicyId(), payment.getPaymentId(), regKeyNoBase64Length);
        if (regKeyEncryptedNoBase64Bytes.length == 0) {
            rowOriginalData += "," + escape(regKeyEncryptedNoBase64);
        } else {
            rowOriginalData += "," + escape(plainText);
        }
//        String row = String.format(",%s,%s,%s,%s%n", escape(regKeyEncryptedWithBase64), "" + regKeyEncryptedNoBase64Bytes.length, escape(regKeyEncryptedNoBase64), escape(plainText));
//        return rowOriginalData + row;
        return rowOriginalData + "%n";
    }

    private String escape(String input) {
        return "\"" + input + "\"";
    }
}
