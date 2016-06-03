package th.co.krungthaiaxa.api.blacklist.service;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import th.co.krungthaiaxa.api.blacklist.data.BlackListed;
import th.co.krungthaiaxa.api.blacklist.exception.ElifeException;
import th.co.krungthaiaxa.api.blacklist.repository.BlackListedRepository;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

import static org.springframework.util.Assert.notNull;
import static th.co.krungthaiaxa.api.blacklist.utils.JsonUtil.getJson;

@Service
public class BlackListedService {
    private final static Logger logger = LoggerFactory.getLogger(BlackListedService.class);
    private final static String SEP = ";COLUMN_SEPARATOR;";
    private final static String FIRST_LINE = "Name" + SEP + "Idno" + SEP + "Desc" + SEP + "Type" + SEP + "Asof" + SEP + "Report_Date" + SEP + "Address";
    private static final String EXPECTED_HEADERS = "[\"Name\", \"Idno\", \"Desc\", \"Type\", \"Asof\", \"Report_Date\", \"Address\"]";

    private final BlackListedRepository blackListedRepository;
    private final SimpMessagingTemplate template;

    private Integer numberOfLinesAdded = 0;
    private Integer numberOfDuplicateLines = 0;
    private Integer numberOfEmptyLines = 0;
    private Integer numberOfLines = 0;

    @Inject
    public BlackListedService(BlackListedRepository blackListedRepository, SimpMessagingTemplate template) {
        this.blackListedRepository = blackListedRepository;
        this.template = template;
    }

    public void checkThaiIdFormat(String thaiId) throws ElifeException {
        if (StringUtils.isBlank(thaiId)) {
            throw new ElifeException("Thai ID cannot be null.");
        }
        if (!StringUtils.isNumeric(thaiId)) {
            throw new ElifeException("Thai ID [" + thaiId + "] is not numeric");
        }
    }

    public void checkThaiIdLength(String thaiId) throws ElifeException {
        if (thaiId.length() != 13) {
            throw new ElifeException("Thai ID [" + thaiId + "] is not in the right format");
        }
    }

    public boolean isBlackListed(String thaiId) throws ElifeException {
        Boolean result = blackListedRepository.findByIdNumber(thaiId) != null;
        logger.info("User is blacklisted " + result);
        return result;
    }

    public Page<BlackListed> findAll(Integer pageNumber, Integer pageSize, String searchContent) {
        return blackListedRepository.findByIdNumberContaining(searchContent, new PageRequest(pageNumber, pageSize, Sort.Direction.ASC, "idNumber"));
    }

    public UploadProgress readBlackListedExcelFile(InputStream inputStream) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        // Reading file has to be using SAX instead of regular XSSFWorkbook since they can be quite big
        notNull(inputStream, "The excel file is not available");
        OPCPackage pkg = OPCPackage.open(inputStream);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        InputStream sheet2 = r.getSheet("rId2");
        InputSource sheetSource = new InputSource(sheet2);

        blackListedRepository.deleteAll();
        numberOfLinesAdded = 0;
        numberOfDuplicateLines = 0;
        numberOfEmptyLines = 0;
        numberOfLines = 0;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(sheetSource, new SheetHandler(sst));
        sheet2.close();
        logger.info("Black list file processed.");

        return new UploadProgress(numberOfLinesAdded, numberOfDuplicateLines, numberOfEmptyLines, numberOfLines);
    }

    private class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;
        private String currentLineNumber = "1";
        private String currentLineContent = "";

        private SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            saveBlackListed();
            template.convertAndSend("/topic/blacklist/upload/progress/result", new String(getJson(new UploadProgress(numberOfLinesAdded, numberOfDuplicateLines, numberOfEmptyLines, numberOfLines))));
            if (numberOfLinesAdded == 0) {
                throw new ElifeException("No line has been found to add in the black list. Make sure the Excel file contains 2 sheets and that second sheet has (exact) headers " + EXPECTED_HEADERS + ".");
            } else {
                logger.info("A total number of [" + numberOfLinesAdded + "] lines have been added");
            }
        }

        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {
            // dimension => the dimension of the sheet
            if (name.equals("dimension")) {
                String dimension = attributes.getValue("ref");
                if (!dimension.startsWith("A1:G")) {
                    throw new ElifeException("The second sheet doesn't a valid range starting with 'A1:G'");
                }
                numberOfLines = Integer.valueOf(dimension.substring("A1:G".length()));
            }

            // row => row
            if (name.equals("row")) {
                String rowNumber = attributes.getValue("r");
                if (!rowNumber.equalsIgnoreCase(currentLineNumber)) {
                    // we changed line. Whatever was in previous line should be saved
                    if ("1".equalsIgnoreCase(currentLineNumber)) {
                        if (!currentLineContent.equalsIgnoreCase(FIRST_LINE)) {
                            throw new ElifeException("The first line of second sheet must contain following headers: " + EXPECTED_HEADERS + ".");
                        } else {
                            logger.info("First line containing [" + currentLineContent + "] is ignored.");
                            numberOfDuplicateLines++;
                        }
                    } else {
                        saveBlackListed();
                    }
                    currentLineContent = "";
                    currentLineNumber = rowNumber;
                    if (numberOfLinesAdded % 1000 == 0) {
                        template.convertAndSend("/topic/blacklist/upload/progress/result", new String(getJson(new UploadProgress(numberOfLinesAdded, numberOfDuplicateLines, numberOfEmptyLines, numberOfLines))));
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
                String name = currentRow[0];
                String idNumber = currentRow[1];
                BlackListed existingBlackListed = blackListedRepository.findByIdNumberAndName(idNumber, name);
                if (existingBlackListed == null) {
                    BlackListed blackListed = new BlackListed();
                    blackListed.setName(currentRow.length >= 1 ? currentRow[0] : null);
                    blackListed.setIdNumber(currentRow.length >= 2 ? currentRow[1] : null);
                    blackListed.setDescription(currentRow.length >= 3 ? currentRow[2] : null);
                    blackListed.setType(currentRow.length >= 4 ? currentRow[3] : null);
                    blackListed.setAsOf(currentRow.length >= 5 ? currentRow[4] : null);
                    blackListed.setReportDate(currentRow.length >= 6 ? currentRow[5] : null);
                    blackListed.setAddress(currentRow.length >= 7 ? currentRow[6] : null);
                    blackListedRepository.save(blackListed);
                    numberOfLinesAdded++;
                } else {
                    numberOfDuplicateLines++;
                }
            } else {
                numberOfEmptyLines++;
            }
        }
    }

    private class UploadProgress {
        private Integer numberOfLinesAdded;
        private Integer numberOfDuplicateLines;
        private Integer numberOfEmptyLines;
        private Integer numberOfLines;

        public UploadProgress(Integer numberOfLinesAdded, Integer numberOfDuplicateLines, Integer numberOfEmptyLines, Integer numberOfLines) {
            this.numberOfLinesAdded = numberOfLinesAdded;
            this.numberOfDuplicateLines = numberOfDuplicateLines;
            this.numberOfEmptyLines = numberOfEmptyLines;
            this.numberOfLines = numberOfLines;
        }

        public Integer getNumberOfDuplicateLines() {
            return numberOfDuplicateLines;
        }

        public Integer getNumberOfEmptyLines() {
            return numberOfEmptyLines;
        }

        public Integer getNumberOfLinesAdded() {
            return numberOfLinesAdded;
        }

        public Integer getNumberOfLines() {
            return numberOfLines;
        }
    }
}
