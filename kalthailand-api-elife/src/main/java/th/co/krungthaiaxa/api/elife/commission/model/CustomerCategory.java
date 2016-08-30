package th.co.krungthaiaxa.api.elife.commission.model;

/**
 * @author khoi.tran on 8/30/16.
 */
public enum CustomerCategory {
    /**
     * The new customer, this is the first time he buy a policy from company.
     */
    NEW
    /**
     * This customer already bought a policy before. He's already existing in our system.
     */
    , EXISTING;
}
