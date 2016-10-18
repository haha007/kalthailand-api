package th.co.krungthaiaxa.api.elife.policyPremiumNotification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.exception.PolicyNotFoundException;
import th.co.krungthaiaxa.api.elife.model.PolicyCDB;
import th.co.krungthaiaxa.api.elife.repository.cdb.PolicyCDBRepository;

import java.time.LocalDate;

/**
 * @author khoi.tran on 10/17/16.
 *         This service will get policy detail from CDB service.
 */
@Service
public class PolicyCDBService {
    private final PolicyCDBRepository policyCDBRepository;

    @Autowired
    public PolicyCDBService(PolicyCDBRepository policyCDBRepository) {this.policyCDBRepository = policyCDBRepository;}

    public PolicyCDB findOneByPolicyNumberAndMainInsuredDOB(String policyNumber, LocalDate insuredDob) {
        return policyCDBRepository.findOneByPolicyNumberAndDOB(policyNumber, insuredDob);
    }

    public PolicyCDB validateExistByPolicyNumberAndMainInsuredDOB(String policyNumber, LocalDate insuredDob) {
        PolicyCDB policyCDB = policyCDBRepository.findOneByPolicyNumberAndDOB(policyNumber, insuredDob);
        if (policyCDB == null) {
            String msg = String.format("Cannot found policy by policyId %s and insuredDob %s", policyNumber, insuredDob);
            throw new PolicyNotFoundException(msg);
        } else {
            return policyCDB;
        }
    }
}
