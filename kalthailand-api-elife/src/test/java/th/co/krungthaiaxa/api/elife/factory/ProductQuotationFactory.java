package th.co.krungthaiaxa.api.elife.factory;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.model.Amount;
import th.co.krungthaiaxa.api.elife.model.ProductDividendOption;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import static java.time.LocalDate.now;
import static th.co.krungthaiaxa.api.elife.products.ProductUtils.amountTHB;

/**
 * @author khoi.tran on 9/19/16.
 */
@Component
public class ProductQuotationFactory {
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

    public static ProductQuotation constructIProtect(Integer age, PeriodicityCode periodicityCode, Double amountValue, Boolean isSumInsured, Integer taxRate, GenderCode genderCode) {
        return constructQuotation(ProductType.PRODUCT_IGEN, null, age, periodicityCode, amountValue, isSumInsured, taxRate, genderCode, 1, null);
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
        return constructQuotation(ProductType.PRODUCT_IGEN, null, age, periodicityCode, amountValue, isSumInsured, taxRate, null, null, productDividendOption);
    }
}
