package th.co.krungthaiaxa.api.common.utils.base36;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author khoi.tran on 10/28/16.
 *         This system uses value from 0 -> 9, then A, B, C,... Z
 *         10 decimal = A base36
 *         16 decimal = F base36
 *         35 decimal = Z base36
 *         <p>
 *         Note: There's no support for MongoDB convert or Json convert for this class yet. So please careful when using this class.
 */
public class Base36Number extends Number implements Comparable, Cloneable {
    public static final int RADIX = 36;
    public static final String MAX_BASE36_VALUE = "1y2p0ij32e8e7";
    public static final String MIN_BASE36_VALUE = "-1y2p0ij32e8e8";
    public static final Base36Number MAX_VALUE = new Base36Number(MAX_BASE36_VALUE);
    public static final Base36Number MIN_VALUE = new Base36Number(MIN_BASE36_VALUE);

    @NotNull
    private final String base36Value;
    private final long decimalValue;

    public Base36Number() {
        this.base36Value = "0";
        this.decimalValue = Base36Util.toDecimalLong(base36Value);
    }

    public Base36Number(@NotNull String base36Value) {
        this.decimalValue = Base36Util.toDecimalLong(base36Value);
        //Don't use input base36Value because it may begin with "000"
        this.base36Value = Base36Util.toBase36String(this.decimalValue);
    }

    public Base36Number(long decimalValue) {
        this.base36Value = Base36Util.toBase36String(decimalValue);
        this.decimalValue = decimalValue;
    }

    public String toString() {
        return this.base36Value;
    }

    @Override
    public Base36Number clone() throws CloneNotSupportedException {
        return (Base36Number) super.clone();
    }

    @Override
    public int intValue() {
        return (int) decimalValue;
    }

    @Override
    public long longValue() {
        return decimalValue;
    }

    @Override
    public float floatValue() {
        return (float) decimalValue;
    }

    @Override
    public double doubleValue() {
        return (double) decimalValue;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || !(o instanceof Base36Number)) {
            return 1;
        }
        Base36Number number = (Base36Number) o;
        long thisLong = this.longValue();
        long thatLong = number.longValue();
        if (thisLong > thatLong) {
            return 1;
        } else if (thisLong < thatLong) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        return (this.compareTo(o) == 0);
    }

    @Override
    public int hashCode() {
        //The equals is evaluated based on the field decimalValue only, so this method must be also based on that field only.
        return Objects.hash(this.decimalValue);
    }

}
