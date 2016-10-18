package th.co.krungthaiaxa.api.elife.repository.cdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.elife.model.PolicyCDB;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author khoi.tran on 10/18/16.
 */
@Repository
public class PolicyCDBRepository {
    public static final String CDB_DATE_PATTERN = "yyyyMMdd";

    @Autowired
    @Qualifier("cdbTemplate")
    private JdbcTemplate jdbcTemplate;

    public PolicyCDB findOneByPolicyNumberAndDOB(String policyNumber, LocalDate dob) {
        String query = " SELECT PNO, PDOB,PNAMF, PNAME, PPTD,PMPREM, "
                + " PLMBNO, PIEMAL, PSTU FROM LFKLUDTA_LFPPML "
                + " WHERE PNO = ? AND PDOB = ? "
                + " AND PSTU IN ('1','2','5','6','B','F') ";
        String dobString = DateTimeUtil.formatLocalDate(dob, CDB_DATE_PATTERN);
        Map<String, Object> result = jdbcTemplate.queryForMap(query, policyNumber, dobString);
        return toPolicy(result);
    }

    private PolicyCDB toPolicy(Map<String, Object> queryRow) {
        if (queryRow == null) {
            return null;
        }
        String policyNumber = (String) queryRow.get("PNO");
        String insuredDobString = (String) queryRow.get("PDOB");
        LocalDate insuredDob = insuredDobString != null ? DateTimeUtil.toLocalDate(insuredDobString, CDB_DATE_PATTERN) : null;
        String insuredFirstName = (String) queryRow.get("PNAMF");
        String insuredFullName = (String) queryRow.get("PNAM");
        String dueDateString = (String) queryRow.get("PPTD");
        LocalDate dueDate = dueDateString != null ? DateTimeUtil.toLocalDate(dueDateString, CDB_DATE_PATTERN) : null;
        Double premiumValue = (Double) queryRow.get("PMPREM");
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

}
