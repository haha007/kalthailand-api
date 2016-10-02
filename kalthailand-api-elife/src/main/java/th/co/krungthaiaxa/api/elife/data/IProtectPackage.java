package th.co.krungthaiaxa.api.elife.data;

/**
 * In reality, when an agency sell products to customer, they have 3 packages:
 * - iProtect5 (customer must pay in 5 years)
 * - iProtect10 (pay in 10 years)
 * - iProtect85 (pay until 85 years old).
 * The predefined rate for those products will be different (see more in iProtect.xlsx, sheet 'iprotect10_rate')
 * <p>
 * <p>
 * However, on online, we only support iProtect10, and it's called iProtectS.
 */
public enum IProtectPackage {
    IPROTECT10;
}
