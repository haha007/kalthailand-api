package th.co.krungthaiaxa.api.elife.model;

/**
 * @author khoi.tran on 9/27/16.
 */
public enum ProductDividendOption {
    END_OF_CONTRACT_PAY_BACK("1"),
    ANNUAL_PAY_BACK_CASH("2"),
    ANNUAL_PAY_BACK_NEXT_PREMIUM("3");

    /**
     * This value will be stored in DB, so never change it.
     */
    private final String id;

    ProductDividendOption(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
