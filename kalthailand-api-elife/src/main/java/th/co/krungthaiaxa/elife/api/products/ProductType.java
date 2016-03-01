package th.co.krungthaiaxa.elife.api.products;

public enum ProductType {
    PRODUCT_10_EC("10EC"), PRODUCT_IFINE("iFine");
    private String name;

    ProductType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
