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
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.elife.api.repository.CollectionFileRepository;
import th.co.krungthaiaxa.elife.api.repository.PaymentRepository;
import th.co.krungthaiaxa.elife.api.repository.PolicyRepository;

import javax.inject.Inject;

import static java.time.LocalDate.now;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static th.co.krungthaiaxa.elife.api.TestUtil.*;
import static th.co.krungthaiaxa.elife.api.model.enums.ChannelType.LINE;
import static th.co.krungthaiaxa.elife.api.model.enums.PaymentStatus.NOT_PROCESSED;
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
    private PolicyRepository policyRepository;
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
    public void should_not_save_collection_file_when_there_is_an_error() {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(collectionFileRepository.findAll()).hasSize(0);
    }

    @Test
    public void should_throw_exception_when_file_not_valid_excel_file() {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_not_found() {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/something.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_extra_column() {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_extraColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_sheet() {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_missingSheet.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_column() {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_missingColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_wrong_column_name() {
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_wrongColumnName.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_saving_same_file_twice() {
        CollectionFile collectionFile = rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        collectionFileRepository.save(collectionFile);
        assertThatThrownBy(() -> rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_save_empty_collection() {
        CollectionFile collectionFile = rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_empty.xls"));
        assertThat(collectionFile.getLines()).hasSize(0);
    }

    @Test
    public void should_save_collection_file_with_lines() {
        CollectionFile collectionFile = rlsService.readExcelFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        assertThat(collectionFile.getLines()).hasSize(50);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_does_not_exist() {
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber("123");
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_is_not_monthly() {
        Policy policy = getPolicy(EVERY_YEAR);
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount(1234.56);
        assertThatThrownBy(() -> rlsService.addPaymentId(collectionFileLine))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_has_already_been_done() {
        Policy policy = getPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setStatus(INCOMPLETE));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount(1234.56);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(updatedPolicy.getPayments()).extracting("status").contains(NOT_PROCESSED);
        assertThat(updatedPolicy.getPayments()).extracting("amount").extracting("value").contains(1234.56);
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_due_date_is_older_than_28_days() {
        Policy policy = getPolicy(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setDueDate(now().minusDays(30)));
        paymentRepository.save(policy.getPayments());
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount(1234.56);
        rlsService.addPaymentId(collectionFileLine);
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(updatedPolicy.getPayments()).extracting("status").contains(NOT_PROCESSED);
        assertThat(updatedPolicy.getPayments()).extracting("amount").extracting("value").contains(1234.56);
    }

    @Test
    public void should_find_a_payment_for_the_policy() {
        Policy policy = getPolicy(EVERY_MONTH);
        CollectionFileLine collectionFileLine = new CollectionFileLine();
        collectionFileLine.setPolicyNumber(policy.getPolicyId());
        collectionFileLine.setPremiumAmount(1234.56);
        rlsService.addPaymentId(collectionFileLine);
        assertThat(collectionFileLine.getPaymentId()).isNotNull();
    }

    private Policy getPolicy(PeriodicityCode periodicityCode) {
        Quote quote = quoteService.createQuote(randomNumeric(20), LINE, productQuotation(periodicityCode, 1000000.0, 5));
        quote(quote, beneficiary(100.0));
        quote = quoteService.updateQuote(quote);

        return policyService.createPolicy(quote);
    }
}
