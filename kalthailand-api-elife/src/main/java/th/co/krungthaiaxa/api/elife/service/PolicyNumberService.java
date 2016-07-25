package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.jsoup.helper.StringUtil;
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
import th.co.krungthaiaxa.api.elife.data.PolicyNumber;
import th.co.krungthaiaxa.api.elife.data.PolicyQuota;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.repository.PolicyNumberRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyQuotaRepository;
import th.co.krungthaiaxa.api.elife.utils.JsonUtil;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

import static org.springframework.util.Assert.notNull;

@Service
public class PolicyNumberService {

    private final static Logger logger = LoggerFactory.getLogger(PolicyNumberService.class);
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
    public PolicyNumberService(PolicyQuotaRepository policyQuotaRepository, PolicyNumberRepository policyNumberRepository, SimpMessagingTemplate template) {
        this.policyQuotaRepository = policyQuotaRepository;
        this.policyNumberRepository = policyNumberRepository;
        this.template = template;
    }

    public PolicyQuota getPolicyQuota(String rowId) {
        logger.info(String.format("On %1$s .....", "getPolicyQuota"));
        PolicyQuota policyQuota = policyQuotaRepository.findByRowId((StringUtil.isBlank(rowId) ? POLICY_QUOTA_ROW_ID : Integer.parseInt(rowId, 10)));
        return policyQuota;
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
