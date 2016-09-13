package th.co.krungthaiaxa.api.elife.products;

import th.co.krungthaiaxa.api.elife.model.CommonData;

public enum ProductType {
    PRODUCT_10_EC("10EC", "10EC", "Product 10 EC"),
    PRODUCT_IBEGIN("iBegin", "iBegin", "Product iBegin"),
    PRODUCT_IFINE("iFine", "iFine", "Product iFine"),
    PRODUCT_IGEN("iGen", "iGen", "iGen"),
    PRODUCT_IPROTECT("iProtect", "iProtect S", "Product iProtect");
    /**
     * This is the logic name, never change its content!
     * TODO rename the fieldName to 'logicName', don't make it confused with default 'name' property of enum!
     */
    private String logicName;
    private String displayName;

    /**
     * @deprecated I just keep it to make compatible with old code & old data. Don't use it in future!!!
     */
    @Deprecated
    private String logicDisplayName;

    ProductType(String logicName, String displayName, String logicDisplayName) {
        this.logicName = logicName;
        this.displayName = displayName;
        this.logicDisplayName = logicDisplayName;
    }

    public String getLogicName() {
        return logicName;
    }

    /**
     * TODO please consider getting from message bundle file.
     *
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * I know the code is bad but the old code put this into {@link CommonData#productName}, and then that product name was put into quote.coverage as a logic name, so cannot change it.
     * This is related to coverage, so never change this name!!!
     *
     * @return
     * @deprecated I just keep it to make compatible with old code & old data. Don't use it in future!!!
     * For new product, please set {@link #logicDisplayName} is equals to {@link #logicName}
     */
    @Deprecated
    public String getLogicDisplayName() {
        return logicDisplayName;
    }
}
