package th.co.krungthaiaxa.api.elife.test.service;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.common.exeption.BadArgumentException;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.data.CollectionFile;
import th.co.krungthaiaxa.api.elife.data.CollectionFileLine;
import th.co.krungthaiaxa.api.elife.data.DeductionFile;
import th.co.krungthaiaxa.api.elife.data.DeductionFileLine;
import th.co.krungthaiaxa.api.elife.exception.PolicyNotFoundException;
import th.co.krungthaiaxa.api.elife.factory.CollectionFileFactory;
import th.co.krungthaiaxa.api.elife.factory.LineServiceMockFactory;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.QuoteFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.line.LinePayService;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.model.enums.PaymentStatus;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.repository.CollectionFileRepository;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;
import th.co.krungthaiaxa.api.elife.service.CollectionFileImportingService;
import th.co.krungthaiaxa.api.elife.service.CollectionFileProcessingService;
import th.co.krungthaiaxa.api.elife.service.PolicyService;
import th.co.krungthaiaxa.api.elife.service.QuoteService;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_MONTH;
import static th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode.EVERY_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CollectionFileProcessingServiceTest extends ELifeTest {
    private static final double DEFAULT_PAYMENT_AMOUNT = 20000;
    @Inject
    private CollectionFileFactory collectionFileFactory;

    @Inject
    private PaymentRepository paymentRepository;
    @Inject
    private PolicyRepository policyRepository;
    @Inject
    private PolicyService policyService;
    @Inject
    private QuoteService quoteService;
    @Inject
    private CollectionFileProcessingService collectionFileProcessingService;
    @Inject
    private CollectionFileImportingService collectionFileImportingService;
    @Inject
    private CollectionFileRepository collectionFileRepository;
    @Inject
    private LinePayService linePayService;
    @Inject
    private MongoTemplate mongoTemplate;
    @Inject
    private PolicyFactory policyFactory;
    @Inject
    private QuoteFactory quoteFactory;

    private List<CollectionFile> testingCollectionFiles;

    @Before
    public void setup() throws IOException {
        linePayService = LineServiceMockFactory.initServiceDefault();
        collectionFileProcessingService.setLinePayService(linePayService);
        testingCollectionFiles = new ArrayList<>();
    }

    @After
    public void tearDown() {
        collectionFileRepository.delete(testingCollectionFiles);
    }

    @Test
    public void should_not_save_collection_file_when_there_is_an_error() {
        long before = collectionFileRepository.count();
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(IOUtil.loadInputStreamFromClassPath("/graph.jpg"))))
                .isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(collectionFileRepository.count()).isEqualTo(before);
    }

    @Test
    public void should_throw_exception_when_file_not_valid_excel_file() {
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(IOUtil.loadInputStreamFromClassPath("/graph.jpg"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_not_found() {
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_extra_column() {
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(IOUtil.loadInputStreamFromClassPath("/collectionFile_extraColumn.xls"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_sheet() {
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(IOUtil.loadInputStreamFromClassPath("/collectionFile_missingSheet.xls"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_missing_column() {
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(IOUtil.loadInputStreamFromClassPath("/collectionFile_missingColumn.xls"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_file_with_wrong_column_name() {
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(IOUtil.loadInputStreamFromClassPath("/collectionFile_wrongColumnName.xls"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_saving_same_file_twice() {
        Policy policy01 = createValidatedIGenPolicyWithDefaultPayment(PeriodicityCode.EVERY_MONTH);
        Policy policy02 = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIProtectDefaultWithMonthlyPayment());
        InputStream collectionFile01 = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy01, policy02);
        testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(collectionFile01));

        //Collection02 is the same with collection01
        InputStream collectionFile02 = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy01, policy02);

        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(collectionFile02))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_throw_exception_when_duplicate_policies() {
        Policy policy01 = createValidatedIGenPolicyWithDefaultPayment(PeriodicityCode.EVERY_MONTH);
        Policy policy02 = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIProtectDefaultWithMonthlyPayment());

        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy01, policy02, policy01);
        CollectionFile collectionFile = collectionFileImportingService.importCollectionFile(inputStream);
        testingCollectionFiles.add(collectionFile);
        Assert.assertEquals(3, collectionFile.getLines().size());

        List<CollectionFile> collectionFiles = collectionFileProcessingService.processLatestCollectionFiles();
        CollectionFile collectionFileResult = collectionFiles.get(0);
        Assert.assertEquals(3, collectionFileResult.getLines().size());

        String firstPaymentIdInCollectionFile = collectionFileResult.getLines().get(0).getPaymentId();
        String thirdPaymentIdInCollectionFile = collectionFileResult.getLines().get(2).getPaymentId();
        assertCollectionFileAndDeductionFileIsEquals(collectionFileResult);

        Assert.assertEquals(3, collectionFileResult.getLines().size());
        Assert.assertNotEquals(firstPaymentIdInCollectionFile, thirdPaymentIdInCollectionFile);

//        assertThatThrownBy(() -> collectionFileImportingService.importCollectionFile(inputStream)).isInstanceOf(BadArgumentException.class);
    }

    private void assertDeductionStatus(CollectionFile collectionFile, String... statuses) {
        int i = 0;
        for (DeductionFileLine deductionFileLine : collectionFile.getDeductionFile().getLines()) {
            Assert.assertEquals(statuses[i], deductionFileLine.getRejectionCode());
            i++;
        }

    }

    private void assertCollectionFileAndDeductionFileIsEquals(CollectionFile collectionFile) {
        int line = 0;
        for (CollectionFileLine collectionFileLine : collectionFile.getLines()) {
            DeductionFileLine deductionFileLine = collectionFile.getDeductionFile().getLines().get(line);
            Assert.assertEquals(collectionFileLine.getPaymentId(), deductionFileLine.getPaymentId());
            assertThat(deductionFileLine.getAmount()).isEqualTo(collectionFileLine.getPremiumAmount());
            assertThat(deductionFileLine.getBankCode()).isEqualTo(collectionFileLine.getBankCode());
            assertThat(deductionFileLine.getPaymentMode()).isEqualTo(collectionFileLine.getPaymentMode());
            assertThat(deductionFileLine.getPolicyNumber()).isEqualTo(collectionFileLine.getPolicyNumber());
            assertThat(deductionFileLine.getProcessDate()).isEqualToIgnoringMinutes(LocalDateTime.now());

            line++;
        }
    }

    @Test
    public void should_save_empty_collection() {
        CollectionFile collectionFile = collectionFileImportingService.importCollectionFile(IOUtil.loadInputStreamFromClassPath("/collectionFile_empty.xls"));
        testingCollectionFiles.add(collectionFile);
        assertThat(collectionFile.getLines()).hasSize(0);
    }

    @Test
    public void should_save_collection_file_with_lines_hash_code_and_dates() {
        LocalDateTime now = now();
        CollectionFile collectionFile = collectionFileImportingService.importCollectionFile(createDefaultCollectionFile());
        testingCollectionFiles.add(collectionFile);
        assertThat(collectionFile.getLines()).hasSize(2);
        assertThat(collectionFile.getFileHashCode()).isNotNull();
        assertThat(collectionFile.getReceivedDate()).isAfter(now);
        assertThat(collectionFile.getJobStartedDate()).isNull();
        assertThat(collectionFile.getJobEndedDate()).isNull();
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_does_not_exist() {
        InputStream collectionFile = CollectionFileFactory.constructCollectionExcelFileWithDefaultPayment("123", "456");
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(collectionFile)))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    public void should_not_find_a_payment_for_the_policy_when_policy_is_not_monthly() {
        Policy policy = createValidatedIGenPolicyWithDefaultPayment(EVERY_YEAR);
        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
        assertThatThrownBy(() -> testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(inputStream)))
                .isInstanceOf(BadArgumentException.class);
    }

    //TODO miss testing logic
//    @Test
//    public void should_add_a_payment_with_null_registration_key() {
//        Policy policy = createValidatedIGenPolicyWithDefaultPayment(EVERY_MONTH);
//        policy.getPayments().stream().forEach(payment -> payment.setRegistrationKey(null));
//        policy.getPayments().stream().forEach(payment -> payment.setStatus(PaymentStatus.INCOMPLETE));
//        paymentRepository.save(policy.getPayments());
//
//        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
//        collectionFileImportingService.importCollectionFile(inputStream);
//        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
//
//        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
//        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
//        assertThat(newPayment.isPresent()).isTrue();
//        assertThat(newPayment.get().getRegistrationKey()).isNull();
//    }

    @Test
    public void should_add_a_payment_with_none_null_registration_key() {
        Policy policy = createValidatedIGenPolicyWithDefaultPayment(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setStatus(PaymentStatus.INCOMPLETE));
        paymentRepository.save(policy.getPayments());

        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
        testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(inputStream));
        collectionFileProcessingService.processLatestCollectionFiles();
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(newPayment.isPresent()).isTrue();
//        assertThat(newPayment.get().getRegistrationKey()).isEqualTo("something");
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_has_already_been_done() {
        Policy policy = createValidatedIGenPolicyWithDefaultPayment(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setStatus(PaymentStatus.INCOMPLETE));
        paymentRepository.save(policy.getPayments());

        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
        testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(inputStream));
        collectionFileProcessingService.processLatestCollectionFiles();
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(newPayment.isPresent()).isTrue();
        Assertions.assertThat(newPayment.get().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(newPayment.get().getAmount().getValue()).isEqualTo(DEFAULT_PAYMENT_AMOUNT);
    }

    @Test
    public void should_add_a_payment_for_the_policy_when_payment_due_date_is_older_than_28_days() {
        Policy policy = createValidatedIGenPolicyWithDefaultPayment(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setDueDate(LocalDateTime.now().minusDays(30)));
        paymentRepository.save(policy.getPayments());

        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
        testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(inputStream));
        collectionFileProcessingService.processLatestCollectionFiles();
        Policy updatedPolicy = policyRepository.findByPolicyId(policy.getPolicyId());
        Optional<Payment> newPayment = updatedPolicy.getPayments().stream().filter(payment -> !policy.getPayments().contains(payment)).findFirst();
        assertThat(updatedPolicy.getPayments()).hasSize(policy.getPayments().size() + 1);
        assertThat(newPayment.isPresent()).isTrue();
        Assertions.assertThat(newPayment.get().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(newPayment.get().getAmount().getValue()).isEqualTo(DEFAULT_PAYMENT_AMOUNT);
    }

//    @Test
//    public void should_find_a_payment_for_the_policy() {
//        Policy policy = createValidatedIGenPolicyWithDefaultPayment(EVERY_MONTH);
//
//        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
//        CollectionFile collectionFile = collectionFileImportingService.importCollectionFile(inputStream);
//
//        assertThat(collectionFile.getLines().get(0).getPaymentId()).isNotNull();
//    }

    @Test
    public void should_create_a_deduction_file_line_with_error_when_no_registration_key() throws IOException {
        when(linePayService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));

        Policy policy = createValidatedIGenPolicyWithDefaultPayment(EVERY_MONTH);
        policy.getPayments().stream().forEach(payment -> payment.setRegistrationKey(null));
        paymentRepository.save(policy.getPayments());

        InputStream inputStream = CollectionFileFactory.constructCollectionExcelFileByPolicy(policy);
        CollectionFile collectionFile = collectionFileImportingService.importCollectionFile(inputStream);
        testingCollectionFiles.add(collectionFile);
        List<CollectionFile> collectionFilesResult = collectionFileProcessingService.processLatestCollectionFiles();
        CollectionFile collectionFileResult = collectionFilesResult.get(0);
        CollectionFileLine firstCollectionFileLine = collectionFileResult.getLines().get(0);

        DeductionFile deductionFile = collectionFileResult.getDeductionFile();
        DeductionFileLine firstDeductionFileLine = deductionFile.getLines().get(0);

        assertThat(deductionFile.getLines()).hasSize(1);
        assertThat(firstDeductionFileLine.getAmount()).isEqualTo(firstCollectionFileLine.getPremiumAmount());
        assertThat(firstDeductionFileLine.getBankCode()).isEqualTo(firstCollectionFileLine.getBankCode());
        assertThat(firstDeductionFileLine.getPaymentMode()).isEqualTo(firstCollectionFileLine.getPaymentMode());
        assertThat(firstDeductionFileLine.getPolicyNumber()).isEqualTo(policy.getPolicyId());
        assertThat(firstDeductionFileLine.getProcessDate()).isEqualToIgnoringMinutes(LocalDateTime.now());
        assertThat(firstDeductionFileLine.getRejectionCode()).isEqualTo(LinePayService.RESPONSE_CODE_ERROR_INTERNAL_LINEPAY);

        Payment payment = paymentRepository.findOne(collectionFileResult.getLines().get(0).getPaymentId());
        Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.INCOMPLETE);
//        assertThat(payment.getEffectiveDate()).isNull();
        assertThat(payment.getPaymentInformations()).hasSize(1);
        assertThat(payment.getPaymentInformations().get(0).getAmount().getValue()).isEqualTo(firstDeductionFileLine.getAmount());
        assertThat(payment.getPaymentInformations().get(0).getAmount().getCurrencyCode()).isEqualTo("THB");
        Assertions.assertThat(payment.getPaymentInformations().get(0).getChannel()).isEqualTo(ChannelType.LINE);
        assertThat(payment.getPaymentInformations().get(0).getCreditCardName()).isNull();
        assertThat(payment.getPaymentInformations().get(0).getDate()).isEqualTo(LocalDate.now());
        assertThat(payment.getPaymentInformations().get(0).getMethod()).isNull();
        assertThat(payment.getPaymentInformations().get(0).getRejectionErrorCode()).isEqualTo(LinePayService.RESPONSE_CODE_ERROR_INTERNAL_LINEPAY);
        Assert.assertNotNull(payment.getPaymentInformations().get(0).getRejectionErrorMessage());
    }

    /*
        @Test
        public void should_create_a_deduction_file_line_with_success() throws IOException {
            when(linePayService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));

            Policy policy = getValidatedPolicy(EVERY_MONTH);
            CollectionFileLine collectionFileLine = collectionFileLine(policy, 100.0);
            rlsService.addPaymentId(collectionFileLine);
            DeductionFile deductionFile = new DeductionFile();
            rlsService.processCollectionFileLine(deductionFile, collectionFileLine);

            assertThat(deductionFile.getLines()).hasSize(1);
            assertThat(deductionFile.getLines().get(0).getAmount()).isEqualTo(100.0);
            assertThat(deductionFile.getLines().get(0).getBankCode()).isEqualTo("myBankCode");
            assertThat(deductionFile.getLines().get(0).getPaymentMode()).isEqualTo("M");
            assertThat(deductionFile.getLines().get(0).getPolicyNumber()).isEqualTo(policy.getPolicyId());
            assertThat(deductionFile.getLines().get(0).getProcessDate()).isEqualToIgnoringMinutes(LocalDateTime.now());
            assertThat(deductionFile.getLines().get(0).getRejectionCode()).isEqualTo("0000");

            Payment payment = paymentRepository.findOne(collectionFileLine.getPaymentId());
            Assertions.assertThat(payment.getStatus()).isEqualTo(PaymentStatus.INCOMPLETE);
            assertThat(payment.getEffectiveDate()).isNull();
            assertThat(payment.getPaymentInformations()).hasSize(1);
            assertThat(payment.getPaymentInformations().get(0).getAmount().getValue()).isEqualTo(100.0);
            assertThat(payment.getPaymentInformations().get(0).getAmount().getCurrencyCode()).isEqualTo("THB");
            Assertions.assertThat(payment.getPaymentInformations().get(0).getChannel()).isEqualTo(ChannelType.LINE);
            assertThat(payment.getPaymentInformations().get(0).getCreditCardName()).isEqualTo("myCreditCardName");
            assertThat(payment.getPaymentInformations().get(0).getDate()).isEqualTo(LocalDate.now());
            assertThat(payment.getPaymentInformations().get(0).getMethod()).isEqualTo("myMethod");
            assertThat(payment.getPaymentInformations().get(0).getRejectionErrorCode()).isEqualTo("0000");
            assertThat(payment.getPaymentInformations().get(0).getRejectionErrorMessage()).isEqualTo("success");
        }
    */
    /*
    @Test
    public void should_mark_collection_file_as_processed() throws IOException {
        when(linePayService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));
        Policy policy = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = getValidatedCollectionFile(
                collectionFileLine(policy, 100.0),
                collectionFileLine(policy, 150.0),
                collectionFileLine(policy, 200.0)
        );
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        assertThat(updatedCollectionFile.getJobStartedDate()).isAfter(updatedCollectionFile.getReceivedDate());
        assertThat(updatedCollectionFile.getJobEndedDate()).isAfter(updatedCollectionFile.getJobStartedDate());
    }
*/
    /*
    @Test
    public void should_process_collection_file() throws IOException {
        when(linePayService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));

        Policy policy1 = getValidatedPolicy(EVERY_MONTH);
        Policy policy2 = getValidatedPolicy(EVERY_MONTH);

        CollectionFile collectionFile = getValidatedCollectionFile(
                collectionFileLine(policy1, 100.0),
                collectionFileLine(policy2, 150.0),
                collectionFileLine(policy2, 200.0)
        );
        rlsService.processLatestCollectionFile();

        CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
        List<DeductionFileLine> deductionFileLines = updatedCollectionFile.getDeductionFile().getLines();
        assertThat(deductionFileLines).extracting("policyNumber").containsExactly(policy1.getPolicyId(), policy2.getPolicyId(), policy2.getPolicyId());
        assertThat(deductionFileLines).extracting("bankCode").containsExactly("myBankCode", "myBankCode", "myBankCode");
        assertThat(deductionFileLines).extracting("paymentMode").containsExactly("M", "M", "M");
        assertThat(deductionFileLines).extracting("amount").containsExactly(100.0, 150.0, 200.0);
        assertThat(deductionFileLines).extracting("processDate").doesNotContainNull();
        assertThat(deductionFileLines).extracting("rejectionCode").containsExactly("0000", "0000", "0000");
    }
*/

    @Test
    public void run_cron_job() {
        Policy policy01 = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment(), TestUtil.TESTING_EMAIL);
        Policy policy02 = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIProtectDefaultWithMonthlyPayment(), TestUtil.TESTING_EMAIL);
        Policy policy03 = policyFactory.createPolicyWithValidatedStatus(ProductQuotationFactory.constructIProtectDefaultWithMonthlyPayment(), TestUtil.TESTING_EMAIL_FAIL_COLLECTION_PAYMENT);

        InputStream inputStream = collectionFileFactory.constructCollectionExcelFileWithDefaultPayment(policy01.getPolicyId(), policy02.getPolicyId(), policy03.getPolicyId());

        testingCollectionFiles.add(collectionFileImportingService.importCollectionFile(inputStream));
        List<CollectionFile> collectionFiles = collectionFileProcessingService.processLatestCollectionFiles();
        CollectionFile collectionFile = collectionFiles.get(0);
        assertCollectionFileAndDeductionFileIsEquals(collectionFile);
        assertDeductionStatus(collectionFile, LinePayService.RESPONSE_CODE_SUCCESS, LinePayService.RESPONSE_CODE_SUCCESS, LinePayService.RESPONSE_CODE_ERROR_MOCK_LINE_FAIL);
    }

    /*
        @Test
        public void should_create_deduction_file_with_proper_header() throws IOException, InvalidFormatException {
            when(linePayService.capturePayment(anyString(), anyDouble(), anyString())).thenReturn(TestUtil.linePayResponse("0000", "success"));
            Policy policy = getValidatedPolicy(EVERY_MONTH);

            CollectionFile collectionFile = getValidatedCollectionFile(
                    collectionFileLine(policy, 100.0)
            );
            rlsService.processLatestCollectionFile();

            CollectionFile updatedCollectionFile = collectionFileRepository.findOne(collectionFile.getId());
            byte[] excelFileContent = rlsService.createDeductionExcelFile(updatedCollectionFile.getDeductionFile());

            assertThat(excelFileContent).isNotNull();
            Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(excelFileContent));
            assertThat(wb.getSheet("LFPATPTDR6")).isNotNull();
            assertThat(wb.getSheet("LFPATPTDR6").getLastRowNum()).isEqualTo(1);
            assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(0).getStringCellValue()).isEqualTo("M93RPNO6");
            assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(1).getStringCellValue()).isEqualTo("M93RBKCD6");
            assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(2).getStringCellValue()).isEqualTo("M93RPMOD6");
            assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(3).getStringCellValue()).isEqualTo("M93RPRM6");
            assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(4).getStringCellValue()).isEqualTo("M93RDOC6");
            assertThat(wb.getSheet("LFPATPTDR6").getRow(0).getCell(5).getStringCellValue()).isEqualTo("M93RJCD6");
        }
        */
    private InputStream createDefaultCollectionFile() {
        ProductQuotation productQuotation01 = ProductQuotationFactory.constructIGenDefaultWithMonthlyPayment();
        Policy policy01 = policyFactory.createPolicyWithValidatedStatus(productQuotation01, TestUtil.TESTING_EMAIL);

        ProductQuotation productQuotation02 = ProductQuotationFactory.constructIProtectDefaultWithMonthlyPayment();
        Policy policy02 = policyFactory.createPolicyWithValidatedStatus(productQuotation02, TestUtil.TESTING_EMAIL);
        return CollectionFileFactory.constructCollectionExcelFileByPolicy(policy01, policy02);
    }

    private Policy createValidatedIGenPolicyWithDefaultPayment(PeriodicityCode periodicityCode) {
        ProductQuotation productQuotation = ProductQuotationFactory.constructIGen(30, periodicityCode, DEFAULT_PAYMENT_AMOUNT * periodicityCode.getNbOfMonths(), false, 35, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
        return policyFactory.createPolicyWithValidatedStatus(productQuotation);
    }

}
