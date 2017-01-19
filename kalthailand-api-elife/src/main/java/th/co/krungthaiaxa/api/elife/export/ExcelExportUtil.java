package th.co.krungthaiaxa.api.elife.export;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.log.LogUtil;
import th.co.krungthaiaxa.api.elife.utils.ExcelIOUtils;
import th.co.krungthaiaxa.api.elife.utils.ExcelUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * @author khoi.tran on 12/26/16.
 */
public class ExcelExportUtil {
    /**
     * @param object if object is null, return an empty excel file.
     * @return
     */
    public static byte[] exportObjectToRows(Object object) {
        Instant start = LogUtil.logStarting("[Excel][Export][start]");
        try (Workbook workbook = new XSSFWorkbook()) {
            exportObjectToRows(workbook, object);
            ExcelUtils.autoWidthAllColumns(workbook);
            byte[] bytes = ExcelIOUtils.writeToBytes(workbook);
            LogUtil.logFinishing(start, "[Excel][Export][finish]");
            return bytes;
        } catch (Exception ex) {
            String msg = String.format("[Excel][Export][error]: %s", ex.getMessage());
            throw new UnexpectedException(msg, ex);
        }
    }

    private static void exportObjectToRows(Workbook workbook, Object object) {
        Sheet sheet = workbook.createSheet();
        if (object == null) {
            return;
        }
        if (object instanceof Collection) {
            Collection<?> collection = (Collection) object;
            int i = 0;
            for (Object element : collection) {
                if (i == 0) {
                    List<ReportFieldDescription> reportFieldDescriptions = ReportFieldUtils.analyseReportFieldDescriptions(element.getClass());
                    addHeaderToNextRow(sheet, reportFieldDescriptions);
                }
                addObjectToNextRow(sheet, i, element);
                i++;
            }
        } else {
            List<ReportFieldDescription> reportFieldDescriptions = ReportFieldUtils.analyseReportFieldDescriptions(object.getClass());
            addHeaderToNextRow(sheet, reportFieldDescriptions);
            addObjectToNextRow(sheet, 0, object);
        }
    }

    private static void addHeaderToNextRow(Sheet sheet, List<ReportFieldDescription> reportFieldDescriptions) {
        ExcelUtils.CellContent[] cellContents = new ExcelUtils.CellContent[reportFieldDescriptions.size() + 1];
        int i = 0;
        cellContents[0] = ExcelUtils.text("#");//The order column.
        for (ReportFieldDescription reportFieldDescription : reportFieldDescriptions) {
            cellContents[i + 1] = ExcelUtils.text(reportFieldDescription.getFieldDisplayName());
            i++;
        }
        ExcelUtils.appendRow(sheet, cellContents);
    }

    /**
     * @param sheet
     * @param rowIndex start by 0, this is the row order of object.
     * @param object
     */
    private static void addObjectToNextRow(Sheet sheet, int rowIndex, Object object) {
        int col = 0;
        List<ReportFieldValue> reportFieldValues = ReportFieldUtils.analyseReportFieldValues(object);
        ExcelUtils.CellContent[] cellContents = new ExcelUtils.CellContent[reportFieldValues.size() + 1];
        cellContents[0] = ExcelUtils.text(rowIndex + 1);
        for (ReportFieldValue reportFieldValue : reportFieldValues) {
            cellContents[col + 1] = ExcelUtils.text(reportFieldValue.getValue());
            col++;
        }
        ExcelUtils.appendRow(sheet, cellContents);
    }

//
//    public static abstract class ExcelExportAction {
//        public abstract void execute(Workbook workbook, Object object);
//    }
}
