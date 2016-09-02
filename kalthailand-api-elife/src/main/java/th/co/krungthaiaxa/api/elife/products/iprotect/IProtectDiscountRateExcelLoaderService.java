package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.elife.products.iprotect.data.IProtectDiscountRate;
import th.co.krungthaiaxa.api.elife.products.iprotect.data.IProtectPackage;
import th.co.krungthaiaxa.api.elife.repository.ProductIProtectDiscountRateRepository;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class IProtectDiscountRateExcelLoaderService {
    private static final String FILE_PATH_PREDEFINED_RATE = "/products/iprotect/iProtect.xlsx";

    private final ProductIProtectDiscountRateRepository productIProtectDiscountRateRepository;

    @Inject
    public IProtectDiscountRateExcelLoaderService(ProductIProtectDiscountRateRepository productIProtectDiscountRateRepository) {this.productIProtectDiscountRateRepository = productIProtectDiscountRateRepository;}

    public List<IProtectDiscountRate> saveIProtectDiscountRatesFromExcelToDB() {
        List<IProtectDiscountRate> existingRates = productIProtectDiscountRateRepository.findAll();
        if (!existingRates.isEmpty()) {
            return existingRates;
        }
        List<IProtectDiscountRate> iProtectDiscountRates = loadPredefinedRatesFromExcel(IProtectPackage.IPROTECT10);
        productIProtectDiscountRateRepository.save(iProtectDiscountRates);
        return iProtectDiscountRates;
    }

    private List<IProtectDiscountRate> loadPredefinedRatesFromExcel(IProtectPackage iprotectPackage) {
        List<IProtectDiscountRate> result = new ArrayList<>();
        String sheetName = loadSheetNameForDiscountRate(iprotectPackage);
        Workbook workbook = ExcelIOUtils.loadFileFromClassPath(FILE_PATH_PREDEFINED_RATE);
        Sheet sheet = workbook.getSheet(sheetName);
        int irow = 0;
        for (Row row : sheet) {
            if (irow == 0) {
                irow++;
                continue;//Ignore the header row
            }
            double sumInsured = ExcelUtils.getInteger(row, 0);
            int columnForIProtectPackage = getColumnIndex(iprotectPackage);
            double discountRate = ExcelUtils.getDouble(row, columnForIProtectPackage);

            IProtectDiscountRate iProtectDiscountRate = new IProtectDiscountRate();
            iProtectDiscountRate.setPackageName(iprotectPackage);
            iProtectDiscountRate.setSumInsured(sumInsured);
            iProtectDiscountRate.setDiscountRate(discountRate);

            result.add(iProtectDiscountRate);
            irow++;
        }

        return Collections.unmodifiableList(result);
    }

    private int getColumnIndex(IProtectPackage iprotectPackage) {
        switch (iprotectPackage) {
        case IPROTECT10: {
            return 2;
        }
        default: {
            throw new UnexpectedException("Don't support " + iprotectPackage);
        }
        }
    }

    private String loadSheetNameForDiscountRate(IProtectPackage iProtectPackage) {
        return "discount_rate";
    }

}
