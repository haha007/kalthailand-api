package th.co.krungthaiaxa.ebiz.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PremiumsData {
    private FinancialScheduler financialScheduler;
    private Amount lifeInsuranceSumInsured;
    private List<DatedAmount> lifeInsuranceMinimumYearlyReturns = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceAverageYearlyReturns = new ArrayList<>();
    private List<DatedAmount> lifeInsuranceMaximumYearlyReturns = new ArrayList<>();

    public FinancialScheduler getFinancialScheduler() {
        return financialScheduler;
    }

    public void setFinancialScheduler(FinancialScheduler financialScheduler) {
        this.financialScheduler = financialScheduler;
    }

    public Amount getLifeInsuranceSumInsured() {
        return lifeInsuranceSumInsured;
    }

    public void setLifeInsuranceSumInsured(Amount lifeInsuranceSumInsured) {
        this.lifeInsuranceSumInsured = lifeInsuranceSumInsured;
    }

    public List<DatedAmount> getLifeInsuranceMinimumYearlyReturns() {
        return Collections.unmodifiableList(lifeInsuranceMinimumYearlyReturns);
    }

    public void setLifeInsuranceMinimumYearlyReturns(List<DatedAmount> lifeInsuranceMinimumYearlyReturns) {
        this.lifeInsuranceMinimumYearlyReturns = lifeInsuranceMinimumYearlyReturns;
    }

    public List<DatedAmount> getLifeInsuranceAverageYearlyReturns() {
        return Collections.unmodifiableList(lifeInsuranceAverageYearlyReturns);
    }

    public void setLifeInsuranceAverageYearlyReturns(List<DatedAmount> lifeInsuranceAverageYearlyReturns) {
        this.lifeInsuranceAverageYearlyReturns = lifeInsuranceAverageYearlyReturns;
    }

    public List<DatedAmount> getLifeInsuranceMaximumYearlyReturns() {
        return Collections.unmodifiableList(lifeInsuranceMaximumYearlyReturns);
    }

    public void setLifeInsuranceMaximumYearlyReturns(List<DatedAmount> lifeInsuranceMaximumYearlyReturns) {
        this.lifeInsuranceMaximumYearlyReturns = lifeInsuranceMaximumYearlyReturns;
    }
}
