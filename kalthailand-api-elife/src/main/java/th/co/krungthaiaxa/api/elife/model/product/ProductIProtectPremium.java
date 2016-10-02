package th.co.krungthaiaxa.api.elife.model.product;

import io.swagger.annotations.ApiModel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@ApiModel(description = "IProtect Life Insurance specific Premiums Data")
public class ProductIProtectPremium extends PremiumDetail implements Serializable {

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
