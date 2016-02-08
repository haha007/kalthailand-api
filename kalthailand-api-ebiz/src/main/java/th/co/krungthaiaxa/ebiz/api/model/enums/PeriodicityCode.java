package th.co.krungthaiaxa.ebiz.api.model.enums;

public enum PeriodicityCode {
    EVERY_MONTH(1), EVERY_QUARTER(3), EVERY_HALF_YEAR(6), EVERY_YEAR(12);

    private Integer nbOfMonths;

    PeriodicityCode(Integer nbOfMonths) {
        this.nbOfMonths = nbOfMonths;
    }

    public Integer getNbOfMonths() {
        return nbOfMonths;
    }
}
