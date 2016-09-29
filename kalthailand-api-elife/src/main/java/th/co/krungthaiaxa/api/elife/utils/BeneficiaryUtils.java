package th.co.krungthaiaxa.api.elife.utils;

import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.Quote;

/**
 * @author khoi.tran on 9/29/16.
 */
public class BeneficiaryUtils {
    public static void addBeneficiariesToFirstCoverage(Quote quote, CoverageBeneficiary... beneficiaries) {
        Coverage firstCoverage = quote.getCoverages().get(0);
        for (CoverageBeneficiary beneficiary : beneficiaries) {
            firstCoverage.addBeneficiary(beneficiary);
        }
    }
}
