package th.co.krungthaiaxa.api.elife.products;

public enum ProductType {
    PRODUCT_10_EC("10EC"), PRODUCT_IBEGIN("iBegin"), PRODUCT_IFINE("iFine"), PRODUCT_IGEN("iGen");
    private String name;

    ProductType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
