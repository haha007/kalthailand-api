package th.co.krungthaiaxa.elife.api.service;


import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;
import th.co.krungthaiaxa.elife.api.data.CollectionFileLine;
import th.co.krungthaiaxa.elife.api.exception.PolicyValidationException;
import th.co.krungthaiaxa.elife.api.exception.QuoteCalculationException;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.repository.CollectionFileRepository;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;

import javax.inject.Inject;

import static java.time.LocalDate.now;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.INCOMPLETE;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode.EVERY_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RLSServiceTest {
    @Inject
    private PaymentRepository paymentRepository;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private RLSService rlsService;
    @Inject
    private CollectionFileRepository collectionFileRepository;
    @Inject
    private MongoTemplate mongoTemplate;

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(CollectionFile.class);
    }

    @Test
    public void should_not_save_collection_file_when_there_is_an_error() throws Exception {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(collectionFileRepository.findAll()).hasSize(0);
    }

    @Test
    public void should_throw_exception_when_file_not_valid_excel_file() throws Exception {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_not_found() throws Exception {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/something.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_extra_column() throws Exception {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_extraColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_sheet() throws Exception {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_missingSheet.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_column() throws Exception {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_missingColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_wrong_column_name() throws Exception {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_wrongColumnName.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_saving_same_file_twice() throws Exception {
        CollectionFile collectionFile = rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        collectionFileRepository.save(collectionFile);
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_save_empty_collection() throws Exception {
        CollectionFile collectionFile = rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_empty.xls"));
        assertThat(collectionFile.getLines()).hasSize(0);
    }

    @Test
    public void should_save_collection_file_with_lines() throws Exception {
        CollectionFile collectionFile = rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        assertThat(collectionFile.getLines()).hasSize(50);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_does_not_exist() throws Exception {
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber("123");
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_is_not_monthly() throws Exception {
        Policy policy = getPolicy(EVERY_YEAR);
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount("1234567890");
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_payment_has_already_been_done() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setStatus(INCOMPLETE));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount("1234567890");
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_payment_due_date_is_older_than_28_days() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setDueDate(now().minusDays(30)));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount("1234567890");
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_find_a_payment_for_the_policy() throws Exception {
        Policy policy = getPolicy(EVERY_MONTH);
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount("1234567890");
        rlsService.addPaymentId(collectionFileLine);
        assertThat(collectionFileLine.getPaymentId()).isNotNull();
    }

    private Policy getPolicy(PeriodicityCode periodicityCode) throws QuoteCalculationException, PolicyValidationException {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation());
        quote(quote, periodicityCode, insured(35), beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }


}
