package th.co.krungthaiaxa.api.elife.model.enums;

public enum PeriodicityCode {
    EVERY_MONTH(1, "Every Month"), EVERY_QUARTER(3, "Every Quarter"), EVERY_HALF_YEAR(6, "Every Half Year"), EVERY_YEAR(12, "Every Year");

    private final Integer nbOfMonths;

    private final String label;

    PeriodicityCode(Integer nbOfMonths, String label) {
        this.nbOfMonths = nbOfMonths;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Integer getNbOfMonths() {
        return nbOfMonths;
    }
}
