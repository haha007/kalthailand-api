package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import th.co.krungthaiaxa.api.elife.model.enums.ProductDividendOption;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "iGen Life Insurance specific Premiums Data")
public class ProductIGenPremium extends PremiumDetail implements Serializable {

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
