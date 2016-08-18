package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ProductFactory {

    public List<ProductType> getProductCriteriaList() {
        return Arrays.asList(ProductType.PRODUCT_10_EC, ProductType.PRODUCT_IBEGIN, ProductType.PRODUCT_IFINE, ProductType.PRODUCT_IGEN, ProductType.PRODUCT_IPROTECT);
    }

}
