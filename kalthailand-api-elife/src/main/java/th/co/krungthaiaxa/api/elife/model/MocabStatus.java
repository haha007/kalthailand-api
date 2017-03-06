package th.co.krungthaiaxa.api.elife.model;

/**
 * Created by tuong.le on 3/3/17.
 */
public enum MocabStatus {
    PENDING, // no action for this status
    SUCCESS, // return success is true
    DUPLICATED, // retry but duplicate from mocab the same success
    LOSS_CONNECTION, // http exception, should be retry 
    ERROR2_MOCAB1, // for handle mocab exception1
    ERROR2_MOCAB2  // for handle mocab exception1
}
