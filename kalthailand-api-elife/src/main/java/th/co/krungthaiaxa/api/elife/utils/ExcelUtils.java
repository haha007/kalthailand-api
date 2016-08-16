package th.co.krungthaiaxa.api.elife.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigInteger;
import java.time.LocalDate;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA;

public class ExcelUtils {
    public static double getDouble(Row row, int cellIndex) {
        return row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getNumericCellValue();
    }

    public static int getInteger(Row row, int cellIndex) {
        return (int) getDouble(row, cellIndex);
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
            short biggestCellNumberInSheet = newArrayList(workbook.getSheetAt(sheetNum).rowIterator()) //
                    .stream() //
                    .map(Row::getLastCellNum).reduce((short) 0, (a, b) -> (short) Math.max(a, b));
            for (int columnIndex = 0; columnIndex < biggestCellNumberInSheet; columnIndex++) {
                workbook.getSheetAt(sheetNum).autoSizeColumn(columnIndex, useMergedCells);
            }
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

    public static CellContent time(LocalDate time) {
        return new Time(time);
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
        return new Text(value.toString());
    }

    public static CellContent text(String value) {
        return new Text(value);
    }

    public static CellContent text(Boolean value) {
        return value == null ? new Text(Boolean.FALSE.toString()) : new Text(value.toString());
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

    public interface CellContent {
        void populateCell(Cell cell);
    }

    private static class Text implements CellContent {
        private final String string;

        public Text(String string) {
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

    private static class Time implements CellContent {
        private final LocalDate time;

        public Time(LocalDate time) {
            this.time = time;
        }

        @Override
        public void populateCell(Cell cell) {
            cell.setCellValue(time.toString());
            CellStyle style = cell.getRow().getSheet().getWorkbook().createCellStyle();
            style.setDataFormat(
                    cell.getRow().getSheet().getWorkbook().createDataFormat().getFormat("YYYY/MM/DD HH:MM:SS"));
            cell.setCellStyle(style);
        }
    }

    private static class Empty implements CellContent {
        @Override
        public void populateCell(Cell cell) {
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
