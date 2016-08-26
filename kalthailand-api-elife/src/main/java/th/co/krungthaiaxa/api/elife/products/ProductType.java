package th.co.krungthaiaxa.api.elife.products;

public enum ProductType {
    PRODUCT_10_EC("10EC"), PRODUCT_IBEGIN("iBegin"), PRODUCT_IFINE("iFine"), PRODUCT_IGEN("iGen"), PRODUCT_IPROTECT("iProtect");
    /**
     * This is the logic name, never change its content!
     * TODO rename the fieldName to 'logicName', don't make it confused with default 'name' property of enum!
     */
    private String name;

    ProductType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return "Product " + name;
    }
}
