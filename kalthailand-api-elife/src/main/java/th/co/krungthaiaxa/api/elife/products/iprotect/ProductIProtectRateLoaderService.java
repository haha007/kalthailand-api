package th.co.krungthaiaxa.api.elife.products.iprotect;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductIProtectRateLoaderService {
    private static final String FILE_PATH_PREDEFINED_RATE = "/products/iprotect/iProtect.xlsx";
    /**
     * There's no setter for this property because its data is loaded from excel files automatically.
     */
    private final Map<ProductIProtectPackage, List<IprotectPredefinedRate>> predefinedIProtectRates = new HashMap<>();

    @PostConstruct
    public void init() {
        initRateByPackage(ProductIProtectPackage.IPROTECT10);
    }

    public List<IprotectPredefinedRate> getPredefinedIProtectRates(ProductIProtectPackage iprotectPackage) {
        return predefinedIProtectRates.get(iprotectPackage);
    }

    public Optional<IprotectPredefinedRate> getPredefinedIProtectRates(ProductIProtectPackage iprotectPackage, int age) {
        List<IprotectPredefinedRate> IProtectPredefinedRates = getPredefinedIProtectRates(iprotectPackage);
        return IProtectPredefinedRates.stream().filter(predefinedRate -> predefinedRate.getAge() == age).findAny();
    }

    private void initRateByPackage(ProductIProtectPackage iProtectPackage) {
        predefinedIProtectRates.put(iProtectPackage, loadPredefinedRates(iProtectPackage));
    }

    private List<IprotectPredefinedRate> loadPredefinedRates(ProductIProtectPackage iprotectPackage) {
        List<IprotectPredefinedRate> result = new ArrayList<>();
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

            IprotectPredefinedRate IProtectPredefinedRate = new IprotectPredefinedRate(iprotectPackage, age, maleRate, femaleRate);
            result.add(IProtectPredefinedRate);
            irow++;
        }

        return Collections.unmodifiableList(result);
    }

    private String loadSheetNameForRate(ProductIProtectPackage iProtectPackage) {
        return iProtectPackage.name().toLowerCase() + "_rate";
    }

}
