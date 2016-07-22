package th.co.krungthaiaxa.api.elife.service;

import static org.springframework.util.Assert.notNull;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
	
	@Inject
	public PolicyQuotaService(PolicyQuotaRepository policyQuotaRepository, PolicyNumberRepository policyNumberRepository, SimpMessagingTemplate template){
		this.policyQuotaRepository = policyQuotaRepository;
		this.policyNumberRepository = policyNumberRepository;
		this.template = template;
	}
	
	public PolicyQuota getPolicyQuota(){
		logger.info(String.format("On %1$s .....", "getPolicyQuota"));
		PolicyQuota policyQuota = policyQuotaRepository.findByRowId(POLICY_QUOTA_ROW_ID);
		return policyQuota;
	}
	
	public void updatePolicyQuota(PolicyQuota updatePolicyQuota){
		logger.info(String.format("On %1$s .....", "updatePolicyQuota"));
		policyQuotaRepository.deleteAll();
		updatePolicyQuota.setRowId(POLICY_QUOTA_ROW_ID);
		policyQuotaRepository.save(updatePolicyQuota);
	}
	
	public UploadProgress readPolicyNumberExcelFile(InputStream inputStream) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        // Reading file has to be using SAX instead of regular XSSFWorkbook since they can be quite big
        notNull(inputStream, "The excel file is not available");
        OPCPackage pkg = OPCPackage.open(inputStream);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        InputStream sheet2 = r.getSheet("Sheet1");
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
            saveBlackListed();
            template.convertAndSend("/topic/policyquota/upload/progress/result", new String(JsonUtil.getJson(new UploadProgress(numberOfLinesAdded, numberOfDuplicateLines, numberOfEmptyLines, numberOfLines))));
            if (numberOfLinesAdded == 0) {
                throw new ElifeException("No line has been found to add in the black list. Make sure the Excel file contains 2 sheets and that second sheet has (exact) headers " + EXPECTED_HEADERS + ".");
            } else {
                logger.info("A total number of [" + numberOfLinesAdded + "] lines have been added");
            }
        }
        
        private void saveBlackListed() {
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

}
