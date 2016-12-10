package th.co.krungthaiaxa.api.elife.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA;

public class ExcelUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);
    public static final String CELL_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static Double getNumber(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell != null) {
            return cell.getNumericCellValue();
        } else {
            return null;
        }
    }

    public static Integer getInteger(Row row, int cellIndex) {
        Double number = getNumber(row, cellIndex);
        return number == null ? null : (int) (double) number;
    }

    /**
     * If cell is empty, null pointer exception will be thrown.
     *
     * @param row
     * @param cellIndex
     * @return
     */
    public static int getInt(Row row, int cellIndex) {
        return (int) (double) getNumber(row, cellIndex);
    }

    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
        case Cell.CELL_TYPE_NUMERIC:
            return String.valueOf(cell.getNumericCellValue());
        default:
            return null;
        }
    }

    public static Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_NUMERIC:
            return cell.getNumericCellValue();
        default:
            return null;
        }
    }

    public static void autoWidthAllColumns(Workbook workbook) {
        autoWidthAllColumns(workbook, false);
    }

    public static void autoWidthAllColumns(Workbook workbook, boolean useMergedCells) {
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            Sheet sheet = workbook.getSheetAt(sheetNum);
            autoWidthAllColumns(sheet, useMergedCells);
        }
    }

    public static void autoWidthAllColumns(Sheet sheet, boolean useMergedCells) {
        short biggestCellNumberInSheet = newArrayList(sheet.rowIterator()) //
                .stream() //
                .map(Row::getLastCellNum).reduce((short) 0, (a, b) -> (short) Math.max(a, b));
        for (int columnIndex = 0; columnIndex < biggestCellNumberInSheet; columnIndex++) {
            sheet.autoSizeColumn(columnIndex, useMergedCells);
        }
    }

    public static int createCells(Row row, CellContent... content) {
        return createCells(row, 0, content);
    }

    public static Row appendRow(Sheet sheet, CellContent... content) {
        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
        createCells(row, 0, content);
        return row;
    }

    public static int createCells(Row row, int cellNumber, CellContent... content) {
        return createCellsFrom(row, cellNumber, content);
    }

    public static void mergeCells(Sheet sheet, int rowIndex, int columnIndex, int rowIncrement, int columnIncrement) {
        if (rowIncrement < 0) {
            throw new IllegalArgumentException("rowIncrement must be positive");
        }
        if (columnIncrement < 0) {
            throw new IllegalArgumentException("columnIncrement must be positive");
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, // first row (0-based)
                rowIndex + rowIncrement, // last row (0-based)
                columnIndex, // first column (0-based)
                columnIndex + columnIncrement // last column (0-based)
        ));
    }

    public static CellContent date(LocalDate time) {
        return new DateCellContent(time);
    }

    public static CellContent percentFormula(String str) {
        return new PercentFormula(str);
    }

    public static CellContent integerFormula(String str) {
        return new IntegerFormula(str);
    }

    public static CellContent integer(Number value) {
        return new IntegerCell(value);
    }

    public static CellContent text(BigInteger value) {
        return new TextCellContent(value.toString());
    }

    public static CellContent text(LocalDate localDate) {
        return new DateCellContent(localDate);
    }

    public static CellContent text(LocalDateTime localDateTime) {
        return new DateTimeCellContent(localDateTime);
    }

    public static CellContent text(Instant instant) {
        return new InstantCellContent(instant);
    }

    public static CellContent text(String value) {
        return new TextCellContent(value);
    }

    public static CellContent text(Double value) {
        return new DoubleCellContent(value);
    }

    public static CellContent text(Boolean value) {
        return value == null ? new TextCellContent(Boolean.FALSE.toString()) : new TextCellContent(value.toString());
    }

    public static CellContent empty() {
        return new Empty();
    }

    public static int createCellsFrom(Row row, int cellNumber, CellContent... content) {
        for (CellContent value : content) {
            Cell cell = row.createCell(cellNumber++);
            value.populateCell(cell);
        }

        return cellNumber;
    }

    public static Cell appendCell(Row row, CellContent cellContent) {
        int lastCellNum = row.getLastCellNum();
        Cell cell = row.createCell(lastCellNum);
        cellContent.populateCell(cell);
        return cell;
    }

    public static void setStyleToCell(Cell cell, String styleProperty, Object styleValue) {
        Map<String, Object> styleProperties = new HashMap<>();
        styleProperties.put(styleProperty, styleValue);
        CellUtil.setCellStyleProperties(cell, styleProperties);
    }

    public static void styleAlignCenter(Cell cell) {
        setStyleToCell(cell, "alignment", CellStyle.ALIGN_CENTER);
    }

    public static Cell appendMergedCell(Row row, CellContent cellContent, int colSpans) {
        Cell nextCell = appendCell(row, cellContent);
        styleAlignCenter(nextCell);
//        CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
//        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//        nextCell.setCellStyle(cellStyle);
        for (int i = 0; i < colSpans - 1; i++) {
            appendCell(row, new Empty());
        }
        CellRangeAddress region = new CellRangeAddress(row.getRowNum(), row.getRowNum(), nextCell.getColumnIndex(), nextCell.getColumnIndex() + colSpans - 1);
        row.getSheet().addMergedRegion(region);
        return nextCell;
    }

    public interface CellContent {
        void populateCell(Cell cell);
    }

    private static class TextCellContent implements CellContent {
        private final String string;

        public TextCellContent(String string) {
            this.string = string;
        }

        @Override
        public void populateCell(Cell cell) {
            cell.setCellValue(string);
        }
    }

    private static class PercentFormula implements CellContent {
        private final String formula;

        public PercentFormula(String formula) {
            this.formula = formula;
        }

        @Override
        public void populateCell(Cell cell) {
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat("0.00%"));
            cell.setCellType(CELL_TYPE_FORMULA);
            cell.setCellFormula(formula);
            cell.setCellStyle(style);
        }
    }

    private static class DateCellContent implements CellContent {
        private final LocalDate localDate;

        public DateCellContent(LocalDate localDate) {
            this.localDate = localDate;
        }

        @Override
        public void populateCell(Cell cell) {
            cell.setCellValue(DateTimeUtil.formatLocalDate(localDate, CELL_DATE_TIME_FORMAT));
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(
                    cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat(CELL_DATE_TIME_FORMAT));
            cell.setCellStyle(style);
        }
    }

    private static class DateTimeCellContent implements CellContent {
        private final LocalDateTime localDateTime;

        public DateTimeCellContent(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }

        @Override
        public void populateCell(Cell cell) {
            cell.setCellValue(DateTimeUtil.formatLocalDateTime(localDateTime, CELL_DATE_TIME_FORMAT));
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(
                    cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat(CELL_DATE_TIME_FORMAT));
            cell.setCellStyle(style);
        }
    }

    private static class InstantCellContent implements CellContent {
        private final Instant instant;

        public InstantCellContent(Instant instant) {
            this.instant = instant;
        }

        @Override
        public void populateCell(Cell cell) {
            cell.setCellValue(DateTimeUtil.format(instant, CELL_DATE_TIME_FORMAT));
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(
                    cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat(CELL_DATE_TIME_FORMAT));
            cell.setCellStyle(style);
        }
    }

    private static class Empty implements CellContent {
        @Override
        public void populateCell(Cell cell) {
        }
    }

    private static class DoubleCellContent implements CellContent {
        private final double value;

        public DoubleCellContent(Number value) {
            this.value = (value == null ? 0L : value).doubleValue();
        }

        @Override
        public void populateCell(Cell cell) {
            cell.setCellValue(value);
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat("#0.0000"));
            cell.setCellStyle(style);
        }
    }

    private static class IntegerCell implements CellContent {
        private final long value;

        public IntegerCell(Number value) {
            this.value = (value == null ? 0L : value).longValue();
        }

        @Override
        public void populateCell(Cell cell) {
            cell.setCellValue(value);
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat("#,##0"));
            cell.setCellStyle(style);
        }
    }

    private static class IntegerFormula implements CellContent {
        private final String formula;

        public IntegerFormula(String formula) {
            this.formula = formula;
        }

        @Override
        public void populateCell(Cell cell) {
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat("#,##0"));
            cell.setCellType(CELL_TYPE_FORMULA);
            cell.setCellFormula(formula);
            cell.setCellStyle(style);
        }
    }

}
