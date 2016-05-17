package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.elife.products.Product;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductFactory;

import javax.inject.Inject;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static th.co.krungthaiaxa.api.elife.utils.JsonUtil.getJson;

@RestController
@Api(value = "Products")
public class ProductResource {
    private final ProductFactory productFactory;

    @Inject
    public ProductResource(ProductFactory productFactory) {
        this.productFactory = productFactory;
    }

    @ApiOperation(value = "Product premiums", notes = "Gets product min and max amounts for sum insured and premium, " +
            "based on product specific algorithm", response = ProductAmounts.class)
    @RequestMapping(value = "/products/amounts", produces = APPLICATION_JSON_VALUE, method = PUT)
    public ResponseEntity<byte[]> getProductAmounts(
            @ApiParam(value = "The product details for which to get the min and max amounts")
            @RequestBody ProductQuotation productQuotationJson) {
        Product product = productFactory.getProduct(productQuotationJson.getProductType().getName());
        return new ResponseEntity<>(getJson(product.getProductAmounts(productQuotationJson)), OK);
    }
}