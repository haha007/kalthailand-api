package th.co.krungthaiaxa.api.elife.products;

public enum ProductIFinePackage {
    IFINE1(100000.0, 500000.0, 500000.0, 1000.0, 500000.0),
    IFINE2(150000.0, 750000.0, 750000.0, 1250.0, 750000.0),
    IFINE3(200000.0, 1000000.0, 1000000.0, 1500.0, 1000000.0),
    IFINE4(250000.0, 2000000.0, 2000000.0, 2000.0, 2000000.0),
    IFINE5(300000.0, 3000000.0, 3000000.0, 2500.0, 3000000.0);

    private Double sumInsured;
    private Double accidentSumInsured;
    private Double healthSumInsured;
    private Double hospitalizationSumInsured;
    private Double deathByAccident;

    ProductIFinePackage(Double sumInsured, Double accidentSumInsured, Double healthSumInsured, Double hospitalizationSumInsured, Double deathByAccident) {
        this.sumInsured = sumInsured;
        this.accidentSumInsured = accidentSumInsured;
        this.healthSumInsured = healthSumInsured;
        this.hospitalizationSumInsured = hospitalizationSumInsured;
        this.deathByAccident = deathByAccident;
    }

    public Double getSumInsured() {
        return sumInsured;
    }

    public Double getAccidentSumInsured() {
        return accidentSumInsured;
    }

    public Double getHealthSumInsured() {
        return healthSumInsured;
    }

    public Double getHospitalizationSumInsured() {
        return hospitalizationSumInsured;
    }

    public Double getDeathByAccident() {
        return deathByAccident;
    }
}
