package th.co.krungthaiaxa.ebiz.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(description = "Data common to all quotes commercial types")
public class CommonData implements Serializable {
    private String productId;
    private String productName;

    @ApiModelProperty(required = true, value = "The unique identifier of the product specification the quote is " +
            "based on")
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @ApiModelProperty(required = true, value = "The name assigned to the product for marketing purposes")
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
