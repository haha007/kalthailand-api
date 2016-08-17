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
public class IProtectRateLoaderService {
    private static final String FILE_PATH_PREDEFINED_RATE = "/products/iprotect/iProtect.xlsx";
    /**
     * There's no setter for this property because its data is loaded from excel files automatically.
     */
    private final Map<IProtectPackage, List<IProtectPredefinedRate>> predefinedIProtectRates = new HashMap<>();

    @PostConstruct
    public void init() {
        initRateByPackage(IProtectPackage.IPROTECT10);
    }

    public List<IProtectPredefinedRate> getPredefinedIProtectRates(IProtectPackage iprotectPackage) {
        return predefinedIProtectRates.get(iprotectPackage);
    }

    public Optional<IProtectPredefinedRate> getPredefinedIProtectRates(IProtectPackage iprotectPackage, int age) {
        List<IProtectPredefinedRate> iprotectPredefinedRates = getPredefinedIProtectRates(iprotectPackage);
        return iprotectPredefinedRates.stream().filter(predefinedRate -> predefinedRate.getAge() == age).findAny();
    }

    private void initRateByPackage(IProtectPackage iProtectPackage) {
        predefinedIProtectRates.put(iProtectPackage, loadPredefinedRates(iProtectPackage));
    }

    private List<IProtectPredefinedRate> loadPredefinedRates(IProtectPackage iprotectPackage) {
        List<IProtectPredefinedRate> result = new ArrayList<>();
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

            IProtectPredefinedRate iprotectPredefinedRate = new IProtectPredefinedRate(iprotectPackage, age, maleRate, femaleRate);
            result.add(iprotectPredefinedRate);
            irow++;
        }

        return Collections.unmodifiableList(result);
    }

    private String loadSheetNameForRate(IProtectPackage iProtectPackage) {
        return iProtectPackage.name().toLowerCase() + "_rate";
    }

}
