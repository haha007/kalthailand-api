package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.enums.GenderCode;
import th.co.krungthaiaxa.api.elife.repository.ProductIProtectRateRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class IProtectRateExcelLoaderService {
    private static final String FILE_PATH_PREDEFINED_RATE = "/products/iprotect/iProtect.xlsx";

    private final ProductIProtectRateRepository productIProtectRateRepository;

    @Inject
    public IProtectRateExcelLoaderService(ProductIProtectRateRepository productIProtectRateRepository) {this.productIProtectRateRepository = productIProtectRateRepository;}

    public List<IProtectRate> saveIProtectRatesFromExcelToDB() {
        List<IProtectRate> iprotectRates = loadPredefinedRatesFromExcel(IProtectPackage.IPROTECT10);
        productIProtectRateRepository.save(iprotectRates);
        return iprotectRates;
    }

    private List<IProtectRate> loadPredefinedRatesFromExcel(IProtectPackage iprotectPackage) {
        List<IProtectRate> result = new ArrayList<>();
        String sheetName = loadSheetNameForRate(iprotectPackage);
        Workbook workbook = ExcelIOUtils.loadFileFromClassPath(FILE_PATH_PREDEFINED_RATE);
        Sheet sheet = workbook.getSheet(sheetName);
        int irow = 0;
        for (Row row : sheet) {
            if (irow == 0) {
                irow++;
                continue;//Ignore the header row
            }
            int age = ExcelUtils.getInteger(row, 0);
            double maleRate = ExcelUtils.getDouble(row, 1);
            double femaleRate = ExcelUtils.getDouble(row, 2);

            IProtectRate iprotectRateForMale = new IProtectRate();
            iprotectRateForMale.setPackageName(iprotectPackage);
            iprotectRateForMale.setAge(age);
            iprotectRateForMale.setGender(GenderCode.MALE);
            iprotectRateForMale.setPremiumRate(maleRate);

            IProtectRate iprotectRateForFemale = new IProtectRate();
            iprotectRateForFemale.setPackageName(iprotectPackage);
            iprotectRateForFemale.setAge(age);
            iprotectRateForFemale.setGender(GenderCode.FEMALE);
            iprotectRateForFemale.setPremiumRate(femaleRate);

            result.add(iprotectRateForMale);
            result.add(iprotectRateForFemale);
            irow++;
        }

        return Collections.unmodifiableList(result);
    }

    private String loadSheetNameForRate(IProtectPackage iProtectPackage) {
        return iProtectPackage.name().toLowerCase() + "_rate";
    }

}
