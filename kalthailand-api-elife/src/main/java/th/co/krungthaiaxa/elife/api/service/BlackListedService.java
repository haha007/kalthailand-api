package th.co.krungthaiaxa.elife.api.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import th.co.krungthaiaxa.elife.api.data.BlackListed;
import th.co.krungthaiaxa.elife.api.exception.ElifeException;
import th.co.krungthaiaxa.elife.api.repository.BlackListedRepository;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

import static org.springframework.util.Assert.notNull;

@Service
public class BlackListedService {
    private final static Logger logger = LoggerFactory.getLogger(BlackListedService.class);
    private final static String SEP = ";COLUMN_SEPARATOR;";
    private final static String FIRST_LINE = "Name" + SEP + "Idno" + SEP + "Desc" + SEP + "Type" + SEP + "Asof" + SEP + "Report_Date" + SEP + "Address";

    private final BlackListedRepository blackListedRepository;

    @Inject
    public BlackListedService(BlackListedRepository blackListedRepository) {
        this.blackListedRepository = blackListedRepository;
    }

    public Page<BlackListed> findAll(Integer pageNumber, Integer pageSize, String searchContent) {
        return blackListedRepository.findByIdNumberContaining(searchContent, new PageRequest(pageNumber, pageSize, Sort.Direction.ASC, "idNumber"));
    }

    public void readBlackListedExcelFile(InputStream inputStream) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        // Reading file has to be using SAX instead of regular XSSFWorkbook since they can be quite big
        notNull(inputStream, "The excel file is not available");
        OPCPackage pkg = OPCPackage.open(inputStream);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        InputStream sheet2 = r.getSheet("rId2");
        InputSource sheetSource = new InputSource(sheet2);

        blackListedRepository.deleteAll();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(sheetSource, new SheetHandler(sst));
        sheet2.close();
        logger.info("Black list file processed.");
    }

    private class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;
        private String currentLineNumber = "1";
        private String currentLineContent = "";
        private Integer numberOfBlacListedAdded = 0;

        private SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        @Override
        public void endDocument() throws SAXException {
            saveBlackListed();
            logger.info("A total number of [" + numberOfBlacListedAdded + "] lines have been added");
        }

        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {
            // row => row
            if (name.equals("row")) {
                String rowNumber = attributes.getValue("r");
                if (!rowNumber.equalsIgnoreCase(currentLineNumber)) {
                    // we changed line. Whatever was in previous line should be saved
                    if ("1".equalsIgnoreCase(currentLineNumber)) {
                        if (!currentLineContent.equalsIgnoreCase(FIRST_LINE)) {
                            throw new ElifeException("The first line of second sheet must contain following headers: [\"Name\", \"Idno\", \"Desc\", \"Type\", \"Asof\", \"Report_Date\", \"Address\"].");
                        }
                        else {
                            logger.info("First line containing [" + currentLineContent + "] is ignored.");
                        }
                    } else {
                        saveBlackListed();
                    }
                    currentLineContent = "";
                    currentLineNumber = rowNumber;
                    if (numberOfBlacListedAdded % 1000 == 0) {
                        logger.info("[" + numberOfBlacListedAdded + "] lines have been added");
                    }
                }
            }

            // c => cell
            if (name.equals("c")) {
                // Figure out if the value is an index in the SST
                String cellType = attributes.getValue("t");
                if (cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        public void endElement(String uri, String localName, String name)
                throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                nextIsString = false;
            }

            // v => contents of a cell
            // Output after we've seen the string contents
            if (name.equals("v")) {
                if (!StringUtils.isEmpty(currentLineContent)) {
                    currentLineContent += ";COLUMN_SEPARATOR;";
                }
                currentLineContent += lastContents;
            }
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {
            lastContents += new String(ch, start, length);
        }

        private void saveBlackListed() {
            if (StringUtils.isNotEmpty(currentLineContent)) {
                String[] currentRow = currentLineContent.split(SEP);
                BlackListed blackListed = new BlackListed();
                blackListed.setName(currentRow.length >= 1 ? currentRow[0] : null);
                blackListed.setIdNumber(currentRow.length >= 2 ? currentRow[1] : null);
                blackListed.setDescription(currentRow.length >= 3 ? currentRow[2] : null);
                blackListed.setType(currentRow.length >= 4 ? currentRow[3] : null);
                blackListed.setAsOf(currentRow.length >= 5 ? currentRow[4] : null);
                blackListed.setReportDate(currentRow.length >= 6 ? currentRow[5] : null);
                blackListed.setAddress(currentRow.length >= 7 ? currentRow[6] : null);
                blackListedRepository.save(blackListed);
                numberOfBlacListedAdded++;
            }
        }
    }
}
