package th.co.krungthaiaxa.api.elife.model;

import th.co.krungthaiaxa.api.elife.model.enums.PeriodicityCode;

import java.io.Serializable;
import java.util.Objects;

public class Periodicity implements Serializable {
    private PeriodicityCode code;

    public PeriodicityCode getCode() {
        return code;
    }

    public void setCode(PeriodicityCode code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Periodicity that = (Periodicity) o;
        return code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
