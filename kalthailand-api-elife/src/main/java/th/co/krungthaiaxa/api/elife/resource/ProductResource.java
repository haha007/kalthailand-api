package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.elife.model.Item;
import th.co.krungthaiaxa.api.elife.products.ProductAmounts;
import th.co.krungthaiaxa.api.elife.products.ProductFactory;
import th.co.krungthaiaxa.api.elife.products.ProductQuotation;
import th.co.krungthaiaxa.api.elife.products.ProductService;
import th.co.krungthaiaxa.api.elife.products.ProductServiceFactory;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@Api(value = "Products")
public class ProductResource {
    private final ProductServiceFactory productServiceFactory;
    private final ProductFactory productFactory;

    @Inject
    public ProductResource(ProductServiceFactory productServiceFactory, ProductFactory productFactory) {
        this.productServiceFactory = productServiceFactory;
        this.productFactory = productFactory;
    }

    @ApiOperation(value = "Product premiums", notes = "Gets product min and max amounts for sum insured and premium, " +
            "based on product specific algorithm", response = ProductAmounts.class)
    @RequestMapping(value = "/products/amounts", produces = APPLICATION_JSON_VALUE, method = PUT)
    public ProductAmounts getProductAmounts(
            @ApiParam(value = "The product details for which to get the min and max amounts")
            @Valid @RequestBody ProductQuotation productQuotation) {
        ProductService productService = productServiceFactory.getProduct(productQuotation.getProductType().getLogicName());
        return productService.calculateProductAmounts(productQuotation);
    }

    @ApiOperation(value = "Gets Product ID", notes = "Gets product ID, ", response = ProductType.class, responseContainer = "List")
    @RequestMapping(value = "/products/items", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<Item> getProductList() {
        return productFactory.getProductItems();
    }
}
