package th.co.krungthaiaxa.api.elife.service;

import static org.springframework.util.Assert.notNull;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.springframework.data.domain.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.springframework.data.domain.PageRequest;
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.repository.PolicyNumberRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyQuotaRepository;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;



@Service
public class PolicyQuotaService {
	
	private final static Logger logger = LoggerFactory.getLogger(PolicyQuotaService.class);
	private final PolicyQuotaRepository policyQuotaRepository;
	private final PolicyNumberRepository policyNumberRepository;
	private final SimpMessagingTemplate template;
	private final int POLICY_QUOTA_ROW_ID = 1;
	private Integer numberOfLinesAdded = 0;
    private Integer numberOfDuplicateLines = 0;
    private Integer numberOfEmptyLines = 0;
    private Integer numberOfLines = 0;
    private final static String SEP = ";COLUMN_SEPARATOR;";
    private static final String EXPECTED_HEADERS = "[\"policyId\"]";
    private final static String FIRST_LINE = "policyId";
	
	@Inject
	public PolicyQuotaService(PolicyQuotaRepository policyQuotaRepository, PolicyNumberRepository policyNumberRepository, SimpMessagingTemplate template){
		this.policyQuotaRepository = policyQuotaRepository;
		this.policyNumberRepository = policyNumberRepository;
		this.template = template;
	}
	
	public PolicyQuota getPolicyQuota(String rowId){
		logger.info(String.format("On %1$s .....", "getPolicyQuota"));
		PolicyQuota policyQuota = policyQuotaRepository.findByRowId((StringUtil.isBlank(rowId)?POLICY_QUOTA_ROW_ID:Integer.parseInt(rowId,10)));
		return policyQuota;
	}
	
	public void updatePolicyQuota(PolicyQuota updatePolicyQuota, String rowId){
		logger.info(String.format("On %1$s .....", "updatePolicyQuota"));
		PolicyQuota policyQuota = policyQuotaRepository.findByRowId(Integer.parseInt(rowId,10));
		policyQuotaRepository.deleteAll();
		policyQuota.setPercent(updatePolicyQuota.getPercent());
		policyQuota.setEmailList(updatePolicyQuota.getEmailList());
		policyQuotaRepository.save(policyQuota);
	}
	
	public UploadProgress readPolicyNumberExcelFile(InputStream inputStream) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        // Reading file has to be using SAX instead of regular XSSFWorkbook since they can be quite big
		
        notNull(inputStream, "The excel file is not available");
        OPCPackage pkg = OPCPackage.open(inputStream);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        InputStream sheet2 = r.getSheet("rId1");
        InputSource sheetSource = new InputSource(sheet2);

        numberOfLinesAdded = 0;
        numberOfDuplicateLines = 0;
        numberOfEmptyLines = 0;
        numberOfLines = 0;

        

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(sheetSource, new SheetHandler(sst));
        sheet2.close();
        logger.info("Policy number list file processed.");

        return new UploadProgress(numberOfLinesAdded, numberOfDuplicateLines, numberOfEmptyLines, numberOfLines);
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
            savePolicyNumber();
            template.convertAndSend("/topic/policy-quota/upload/progress/result", new String(JsonUtil.getJson(new UploadProgress(numberOfLinesAdded, numberOfDuplicateLines, numberOfEmptyLines, numberOfLines))));
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
			if (!dimension.startsWith("A1")) {
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
				   savePolicyNumber();
			   }
			   currentLineContent = "";
			   currentLineNumber = rowNumber;
			   if (numberOfLinesAdded % 1000 == 0) {
			       template.convertAndSend("/topic/blacklist/upload/progress/result", new String(JsonUtil.getJson(new UploadProgress(numberOfLinesAdded, numberOfDuplicateLines, numberOfEmptyLines, numberOfLines))));
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
        
        private void savePolicyNumber() {
            if (StringUtils.isNotEmpty(currentLineContent)) {
                String[] currentRow = currentLineContent.split(SEP);
                String newPolicyId = currentRow[0];
                PolicyNumber existsPolicyId =  policyNumberRepository.findByPolicyId(newPolicyId);
                if (existsPolicyId == null) {
                	PolicyNumber insertPolicyNumber = new PolicyNumber();
                	insertPolicyNumber.setPolicyId(newPolicyId);
                    policyNumberRepository.save(insertPolicyNumber);
                    numberOfLinesAdded++;
                } else {
                    numberOfDuplicateLines++;
                }
            } else {
                numberOfEmptyLines++;
            }
        }
    }
	
	public Page<PolicyNumber> findAll(Integer pageNumber, Integer pageSize) {
        return policyNumberRepository.findByPolicyNull(new PageRequest(pageNumber, pageSize, Sort.Direction.ASC, "policyId"));
    }

}
