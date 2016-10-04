package th.co.krungthaiaxa.api.elife.exception;

import th.co.krungthaiaxa.api.common.exeption.BaseException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 10/4/16.
 *         The error for processing policy validation (change status of policy to {@link th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus#VALIDATED}.
 */
public class PolicyValidatedProcessException extends BaseException {
    private static final String ERROR_CODE = ErrorCode.ERROR_CODE_POLICY_VALIDATION_PROCESS;

    public PolicyValidatedProcessException(String message) {
        super(ERROR_CODE, message);
    }
}
