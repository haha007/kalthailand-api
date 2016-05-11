package th.co.krungthaiaxa.elife.api.products;

public enum ProductType {
    PRODUCT_10_EC("10EC"), PRODUCT_IBEGIN("iBegin"), PRODUCT_IFINE("iFine"), PRODUCT_ISAFE("iSafe");
    private String name;

    ProductType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
