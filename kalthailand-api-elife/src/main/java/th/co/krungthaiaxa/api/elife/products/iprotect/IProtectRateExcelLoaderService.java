package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.data.ProductPremiumRate;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.ProductIProtectRateRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class IProtectRateExcelLoaderService {
    public static final Logger LOGGER = LoggerFactory.getLogger(IProtectRateExcelLoaderService.class);

    private static final String FILE_PATH_PREDEFINED_RATE = "/products/iprotect/iProtect.xlsx";

    private final ProductIProtectRateRepository productIProtectRateRepository;

    @Inject
    public IProtectRateExcelLoaderService(ProductIProtectRateRepository productIProtectRateRepository) {this.productIProtectRateRepository = productIProtectRateRepository;}

//    public List<IProtectRate> saveIProtectRatesFromExcelToDB() {
//        List<IProtectRate> existingRates = productIProtectRateRepository.findAll();
//        if (!existingRates.isEmpty()) {
//            return existingRates;
//        }
//        List<IProtectRate> iprotectRates = excelToProductPremiumRates();
//        productIProtectRateRepository.save(iprotectRates);
//        return iprotectRates;
//    }
//
//
    public List<ProductPremiumRate> excelToProductPremiumRates(IProtectPackage iProtectPackage) {
        return excelToProductPremiumRates(FILE_PATH_PREDEFINED_RATE, ProductType.PRODUCT_IPROTECT.getLogicName(), iProtectPackage.name());
    }

    public List<ProductPremiumRate> excelToProductPremiumRates(String excelFilePath, String productLogicName, String packageName) {
        List<ProductPremiumRate> result = new ArrayList<>();
        String sheetName = loadSheetNameForRate(productLogicName, packageName);
        Workbook workbook = ExcelIOUtils.loadFileFromClassPath(excelFilePath);
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            LOGGER.warn("Cannot find sheet " + sheetName + ", so we will get the first sheet");
            sheet = workbook.getSheetAt(0);
        }
        int irow = 0;
        for (Row row : sheet) {
            if (irow == 0) {
                irow++;
                continue;//Ignore the header row
            }
            Integer age = ExcelUtils.getInteger(row, 0);
            if (age == null) continue;

            Double maleRate = ExcelUtils.getNumber(row, 1);
            Double femaleRate = ExcelUtils.getNumber(row, 2);
            ProductPremiumRate productPremiumRateForMale = new ProductPremiumRate();
            if (maleRate != null) {
                productPremiumRateForMale.setProductId(productLogicName);
                productPremiumRateForMale.setPackageName(packageName);
                productPremiumRateForMale.setAge(age);
                productPremiumRateForMale.setGender(GenderCode.MALE);
                productPremiumRateForMale.setPremiumRate(maleRate);
                result.add(productPremiumRateForMale);
            }
            ProductPremiumRate productPremiumRateForFemale = new ProductPremiumRate();
            if (femaleRate != null) {
                productPremiumRateForFemale.setProductId(productLogicName);
                productPremiumRateForFemale.setPackageName(packageName);
                productPremiumRateForFemale.setAge(age);
                productPremiumRateForFemale.setGender(GenderCode.FEMALE);
                productPremiumRateForFemale.setPremiumRate(femaleRate);
                result.add(productPremiumRateForFemale);
            }
            irow++;
        }

        return Collections.unmodifiableList(result);
    }

    private String loadSheetNameForRate(String productLogicName, String packageName) {
        return productLogicName.toLowerCase() + "_" + packageName.toLowerCase() + "_premiumrate";
    }

}
