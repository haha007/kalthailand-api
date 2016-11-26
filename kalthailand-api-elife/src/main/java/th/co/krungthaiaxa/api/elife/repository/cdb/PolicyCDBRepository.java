package th.co.krungthaiaxa.api.elife.repository.cdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.ListUtil;
import th.co.krungthaiaxa.api.elife.generalsetting.GeneralSetting;
import th.co.krungthaiaxa.api.elife.model.PolicyCDB;
import th.co.krungthaiaxa.api.elife.generalsetting.GeneralSettingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 10/18/16.
 */
@Repository
public class PolicyCDBRepository {
    public static final String CDB_DATE_PATTERN = "yyyyMMdd";
    public static final boolean MOCK_DATA = true;

    @Autowired
    private GeneralSettingService generalSettingService;

    @Autowired
    @Qualifier("policyPremiumCdbTemplate")
    private JdbcTemplate jdbcTemplate;

    public PolicyCDB findOneByPolicyNumberAndDOB(String policyNumber, LocalDate insuredDob) {
        GeneralSetting generalSetting = generalSettingService.loadGeneralSetting();
        if (generalSetting.isPolicyPremiumMockData()) {
            return mockData(policyNumber, insuredDob);
        }

        String query = " SELECT PNO, PDOB,PNAMF, PNAME, PPTD,PMPREM, "
                + " PLMBNO, PIEMAL, PSTU FROM LFKLUDTA_LFPPML "
                + " WHERE PNO LIKE ? AND PDOB = ? "
                + " AND PSTU IN ('1','2','5','6','B','F') ";
        String dobString = DateTimeUtil.formatLocalDate(insuredDob, CDB_DATE_PATTERN);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query, policyNumber, dobString);
        List<PolicyCDB> policyCDBs = toPolicies(results);
        return ListUtil.hasOneElement(policyCDBs);
    }

    //TODO Just temporary
    private PolicyCDB mockData(String policyNumber, LocalDate insuredDob) {
        if (policyNumber.startsWith("4")) {
            return null;
        }
        PolicyCDB policyCDB = new PolicyCDB();
        PolicyCDB.InsuredCDB mainInsuredCDB = new PolicyCDB.InsuredCDB();
        mainInsuredCDB.setMobilePhone("");
        mainInsuredCDB.setEmail("chairat.poo@krungthai-axa.co.th");
        mainInsuredCDB.setFullName("Chairat Poo");
        mainInsuredCDB.setFirstName("Jo");
        mainInsuredCDB.setDob(insuredDob);

        policyCDB.setMainInsured(mainInsuredCDB);
        policyCDB.setStatus("1");
        policyCDB.setPolicyNumber(policyNumber);
        policyCDB.setPremiumValue(1000.0);
        policyCDB.setDueDate(LocalDate.now());
        return policyCDB;
    }

    private List<PolicyCDB> toPolicies(List<Map<String, Object>> queryRows) {
        return queryRows.stream().map(row -> toPolicy(row)).collect(Collectors.toList());
    }

    private PolicyCDB toPolicy(Map<String, Object> queryRow) {
        if (queryRow == null) {
            return null;
        }
        String policyNumber = (String) queryRow.get("PNO");
        LocalDate insuredDob = getLocalDateString(queryRow, "PDOB");
        String insuredFirstName = (String) queryRow.get("PNAMF");
        String insuredFullName = (String) queryRow.get("PNAM");
        LocalDate dueDate = getLocalDateString(queryRow, "PPTD");
        Double premiumValue = getDouble(queryRow, "PMPREM");
        String insuredMobileNumber = (String) queryRow.get("PLMBNO");
        String insuredEmail = (String) queryRow.get("PIEMAL");
        String status = (String) queryRow.get("PSTU");

        PolicyCDB policy = new PolicyCDB();
        PolicyCDB.InsuredCDB mainInsuredCDB = new PolicyCDB.InsuredCDB();
        policy.setMainInsured(mainInsuredCDB);

        policy.setPolicyNumber(policyNumber);
        mainInsuredCDB.setDob(insuredDob);
        mainInsuredCDB.setEmail(insuredEmail);
        mainInsuredCDB.setFirstName(insuredFirstName);
        mainInsuredCDB.setFullName(insuredFullName);
        mainInsuredCDB.setMobilePhone(insuredMobileNumber);

        policy.setDueDate(dueDate);
        policy.setPremiumValue(premiumValue);
        policy.setStatus(status);
        return policy;
    }

    private String getBigDecimalString(Map<String, Object> queryRow, String propertyName) {
        Object propertyValue = queryRow.get(propertyName);
        return propertyValue == null ? null : String.valueOf(propertyValue);
    }

    private Double getDouble(Map<String, Object> queryRow, String propertyName) {
        Object propertyValue = queryRow.get(propertyName);
        if (propertyValue == null) {
            return null;
        } else if (propertyValue instanceof Number) {
            return ((Number) propertyValue).doubleValue();
        } else {
            String numString = String.valueOf(propertyValue);
            return Double.valueOf(numString);
        }
    }

    private LocalDate getLocalDateString(Map<String, Object> queryRow, String propertyName) {
        String propertyDateAsString = getBigDecimalString(queryRow, propertyName);
        return propertyDateAsString != null ? DateTimeUtil.toLocalDate(propertyDateAsString, CDB_DATE_PATTERN) : null;
    }
}
