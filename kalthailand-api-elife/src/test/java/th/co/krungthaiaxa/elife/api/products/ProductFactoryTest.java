package th.co.krungthaiaxa.elife.api.products;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductFactoryTest {

    @Test
    public void should_return_error_when_product_id_is_unknown() {
        assertThatThrownBy(() -> ProductFactory.getProduct("something"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_return_10EC_product() {
        Product product = ProductFactory.getProduct(ProductType.PRODUCT_10_EC.getName());
        assertThat(product).isInstanceOf(Product10EC.class);
    }

    @Test
    public void should_return_iBegin_product() {
        Product product = ProductFactory.getProduct(ProductType.PRODUCT_IBEGIN.getName());
        assertThat(product).isInstanceOf(ProductIBegin.class);
    }

    @Test
    public void should_return_iFine_product() {
        Product product = ProductFactory.getProduct(ProductType.PRODUCT_IFINE.getName());
        assertThat(product).isInstanceOf(ProductIFine.class);
    }
}
