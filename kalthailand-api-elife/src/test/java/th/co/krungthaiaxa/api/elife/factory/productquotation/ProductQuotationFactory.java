package th.co.krungthaiaxa.api.elife.factory.productquotation;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.enums.AtpMode;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.products.ProductIFinePackage;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import static java.time.LocalDate.now;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.amountTHB;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class ProductQuotationFactory {

    public static ProductQuotation constructQuotation(
            ProductType productType, String packageName, Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode, Integer occupationTypeId,
            ProductDividendOption productDividendOption, AtpMode atpMode) {
        Amount amount = amountTHB(amountValue);

        ProductQuotation productQuotation = new ProductQuotation();
        productQuotation.setProductType(productType);
        if (age != null) {
            productQuotation.setDateOfBirth(now().minusYears(age));
        }
        productQuotation.setDeclaredTaxPercentAtSubscription(taxRate);
        productQuotation.setGenderCode(genderCode);
        productQuotation.setPeriodicityCode(periodicityCode);
        productQuotation.setOccupationId(occupationTypeId);
        productQuotation.setPackageName(packageName);
        if (productDividendOption != null) {
            productQuotation.setDividendOptionId(productDividendOption.getId());
        }
        if (isSumInsured != null && isSumInsured) {
            productQuotation.setSumInsuredAmount(amount);
        } else {
            productQuotation.setPremiumAmount(amount);
        }
        if (atpMode != null) {
            productQuotation.setAtpMode(atpMode.getNumValue());
        } else {
            productQuotation.setAtpMode(null);
        }
        return productQuotation;
    }

    public static ProductQuotation constructQuotation(
            ProductType productType, String packageName, Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured,
            Integer taxRate, GenderCode genderCode, Integer occupationTypeId, ProductDividendOption productDividendOption) {
        return constructQuotation(productType, packageName, age, periodicityCode, amountValue, isSumInsured, taxRate, genderCode, occupationTypeId, productDividendOption, getDefaultAtpModeByPeriodicity(periodicityCode));
    }

    public static ProductQuotation constructIProtectDefault() {
        return constructIProtect(33, PeriodicityCode.EVERY_MONTH, 300000.0, true, 35, GenderCode.MALE);
    }

    public static ProductQuotation constructIProtectDefaultWithMonthlyPayment() {
        return constructIProtect(33, PeriodicityCode.EVERY_MONTH, 300000.0, true, 35, GenderCode.MALE);
    }

    public static ProductQuotation constructIProtect(Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode) {
        return constructIProtect(age, periodicityCode, amountValue, isSumInsured, taxRate, genderCode, getDefaultAtpModeByPeriodicity(periodicityCode));
    }

    public static ProductQuotation constructIProtect(Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode, AtpMode atpMode) {
        return constructQuotation(ProductType.PRODUCT_IPROTECT, IProtectPackage.IPROTECT10.name(), age, periodicityCode, amountValue, isSumInsured, taxRate, genderCode, 1, null, atpMode);
    }

    /**
     * Don't need package name or genderCode or occupation because they don't affect the calculation result.
     *
     * @param age
     * @param periodicityCode
     * @param amountValue
     * @param isSumInsured
     * @param taxRate
     * @return
     */
    public static ProductQuotation constructIGen(Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, ProductDividendOption productDividendOption) {
        //This product always required occupation to stored in DB, but it doesn't impact the calculation.
        return constructQuotation(ProductType.PRODUCT_IGEN, null, age, periodicityCode, amountValue, isSumInsured, taxRate, GenderCode.MALE, 1, productDividendOption);
    }

    public static ProductQuotation constructIGen(Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, ProductDividendOption productDividendOption, AtpMode atpMode) {
        return constructQuotation(ProductType.PRODUCT_IGEN, null, age, periodicityCode, amountValue, isSumInsured, taxRate, GenderCode.MALE, 1, productDividendOption, atpMode);
    }

    /**
     * You should use this for only integration test, we don't actually care about the calculation result of this input.
     *
     * @return
     * @Note: Never change values in this method. If you want to use another value, please create the new method.
     */
    public static ProductQuotation constructIGenDefault() {
        return ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_YEAR, 1000000.0, true, 35, ProductDividendOption.END_OF_CONTRACT_PAY_BACK);
    }

    public static ProductQuotation constructIGenDefaultWithMonthlyPayment() {
        return ProductQuotationFactory.constructIGen(33, PeriodicityCode.EVERY_MONTH, 1000000.0, true, 35, ProductDividendOption.END_OF_CONTRACT_PAY_BACK);
    }

    public static ProductQuotation constructIProtectDefault(PeriodicityCode periodicityCode) {
        return ProductQuotationFactory.constructIGen(33, periodicityCode, 100000.0, true, 35, ProductDividendOption.END_OF_CONTRACT_PAY_BACK);
    }

    public static ProductQuotation construct10ECDefault() {

        return constructQuotation(ProductType.PRODUCT_10_EC, null, 32, PeriodicityCode.EVERY_MONTH, 10000.0, false, 35, GenderCode.MALE, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
    }

    public static ProductQuotation construct10ECDefault(int age, PeriodicityCode periodicityCode) {
        return constructQuotation(ProductType.PRODUCT_10_EC, null, age, periodicityCode, 350000.0, true, 23, GenderCode.MALE, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
    }

    public static ProductQuotation construct10ECDefault(int age, PeriodicityCode periodicityCode, double amount, boolean isSumInsured, int tax) {
        return constructQuotation(ProductType.PRODUCT_10_EC, null, age, periodicityCode, amount, isSumInsured, tax, GenderCode.MALE, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
    }

    public static ProductQuotation construct10ECDefault(int age, PeriodicityCode periodicityCode, double amount, boolean isSumInsured, int tax, AtpMode atpMode) {
        return constructQuotation(ProductType.PRODUCT_10_EC, null, age, periodicityCode, amount, isSumInsured, tax, GenderCode.MALE, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH, atpMode);
    }

    public static ProductQuotation constructIFineDefault() {
        return constructQuotation(ProductType.PRODUCT_IFINE, ProductIFinePackage.IFINE1.name(), 32, PeriodicityCode.EVERY_MONTH, ProductIFinePackage.IFINE1.getSumInsured(), true, 35, GenderCode.MALE, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
    }

    public static ProductQuotation constructDefault(ProductType productType) {
        return ProductFactoryForProductQuotation.getFactory(productType).constructDefault();
    }

    public static ProductQuotation constructDefault(ProductType productType, PeriodicityCode periodicityCode, AtpMode atpMode) {
        return ProductFactoryForProductQuotation.getFactory(productType).constructDefault(periodicityCode, atpMode);
    }

    public static ProductQuotation constructIBeginDefault() {
        return constructQuotation(ProductType.PRODUCT_IBEGIN, null, 32, PeriodicityCode.EVERY_MONTH, 10000.0, false, 35, GenderCode.MALE, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
    }

    public static ProductQuotation constructIBegin(int age, PeriodicityCode periodicityCode, double amount, boolean isSumInsurd, GenderCode genderCode) {
        return constructQuotation(ProductType.PRODUCT_IBEGIN, null, age, periodicityCode, amount, isSumInsurd, 23, genderCode, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH);
    }

    public static ProductQuotation constructIBegin(int age, PeriodicityCode periodicityCode, double amount, boolean isSumInsurd, GenderCode genderCode, AtpMode atpMode) {
        return constructQuotation(ProductType.PRODUCT_IBEGIN, null, age, periodicityCode, amount, isSumInsurd, 23, genderCode, 1, ProductDividendOption.ANNUAL_PAY_BACK_CASH, atpMode);
    }

    public static ProductQuotation constructIFine(ProductIFinePackage productIFinePackage, int age, PeriodicityCode periodicityCode, GenderCode genderCode, boolean riskOccupation, AtpMode atpMode) {
        int occupationId = 1;
        if (riskOccupation) {
            occupationId = 21;
        }
        ProductQuotation productQuotation = constructQuotation(ProductType.PRODUCT_IFINE, productIFinePackage.name(), age, periodicityCode, productIFinePackage.getSumInsured(), true, 23, genderCode, occupationId, null, atpMode);
        return productQuotation;
    }

    public static ProductQuotation constructIFine(ProductIFinePackage productIFinePackage, int age, PeriodicityCode periodicityCode, GenderCode genderCode, boolean riskOccupation) {
        AtpMode atpMode = getDefaultAtpModeByPeriodicity(periodicityCode);
        return constructIFine(productIFinePackage, age, periodicityCode, genderCode, riskOccupation, atpMode);
    }

    private static AtpMode getDefaultAtpModeByPeriodicity(PeriodicityCode periodicityCode) {
        AtpMode atpMode = null;
        if (PeriodicityCode.EVERY_MONTH.equals(periodicityCode)) {
            atpMode = AtpMode.AUTOPAY;
        }
        return atpMode;
    }
}
