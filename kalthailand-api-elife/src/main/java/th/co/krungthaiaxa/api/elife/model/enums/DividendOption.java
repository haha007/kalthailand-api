package th.co.krungthaiaxa.api.elife.model.enums;

/**
 * @deprecated this value is used only for iFine because of the enum value (IN_FINE).
 * It's not general enough for other product. And DB use enum value to stored in DB.
 * So in future product, please use {@link ProductDividendOption}.
 */
@Deprecated
public enum DividendOption {
    YEARLY_CASH, YEARLY_FOR_NEXT_PREMIUM, IN_FINE
}
