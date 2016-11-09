package th.co.krungthaiaxa.api.elife.model.enums;

import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;

/**
 * @author khoi.tran on 11/7/16.
 */
public enum AtpMode {

    NO_AUTOPAY(0),
    /**
     * The policy will be auto paid by our company:
     * Base on the application form, the company will input into AS400 and then generated the CollectionFile.
     * Then our {@link th.co.krungthaiaxa.api.elife.service.CollectionFileProcessingService} will process only policies with atpMode is {@link #AUTOPAY}.
     */
    AUTOPAY(1);
    private final int numValue;

    AtpMode(int numValue) {this.numValue = numValue;}

    public int getNumValue() {
        return numValue;
    }

    public static AtpMode enumOfNumValue(Integer numValue) {
        if (numValue == null) {
            return null;
        }
        for (AtpMode atpMode : values()) {
            if (atpMode.getNumValue() == numValue) {
                return atpMode;
            }
        }
        throw new UnexpectedException(AtpMode.class.getSimpleName() + " doesn't have numValue '" + numValue + "'");
    }
}
