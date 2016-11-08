package th.co.krungthaiaxa.api.elife.factory;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import static java.time.LocalDate.now;
import static th.co.krungthaiaxa.api.elife.products.utils.ProductUtils.amountTHB;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class ProductQuotationFactory {
    public static final String DUMMY_EMAIL = "dummy@krungthai-axa.co.th";

    public static ProductQuotation constructQuotation(ProductType productType, String packageName, Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode, Integer occupationTypeId,
            ProductDividendOption productDividendOption) {
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
        return productQuotation;
    }

    public static ProductQuotation constructIProtectDefault() {
        return constructIProtect(33, PeriodicityCode.EVERY_MONTH, 2000.0, false, 35, GenderCode.MALE);
    }

    public static ProductQuotation constructIProtectDefaultWithMonthlyPayment() {
        return constructIProtect(33, PeriodicityCode.EVERY_MONTH, 2000.0, false, 35, GenderCode.MALE);
    }

    public static ProductQuotation constructIProtect(Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode) {
        return constructQuotation(ProductType.PRODUCT_IPROTECT, IProtectPackage.IPROTECT10.name(), age, periodicityCode, amountValue, isSumInsured, taxRate, genderCode, 1, null);
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
        //This product always required occupation to stored in DB, but don't need it for calculation.
        return constructQuotation(ProductType.PRODUCT_IGEN, null, age, periodicityCode, amountValue, isSumInsured, taxRate, GenderCode.MALE, 1, productDividendOption);
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
}
