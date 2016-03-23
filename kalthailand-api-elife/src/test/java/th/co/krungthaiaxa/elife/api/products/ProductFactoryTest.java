package th.co.krungthaiaxa.elife.api.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductFactoryTest {
    @Inject
    private ProductFactory productFactory;

    @Test
    public void should_return_error_when_product_id_is_unknown() {
        assertThatThrownBy(() -> productFactory.getProduct("something"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_return_10EC_product() {
        Product product = productFactory.getProduct(ProductType.PRODUCT_10_EC.getName());
        assertThat(product).isInstanceOf(Product10EC.class);
    }

    @Test
    public void should_return_iBegin_product() {
        Product product = productFactory.getProduct(ProductType.PRODUCT_IBEGIN.getName());
        assertThat(product).isInstanceOf(ProductIBegin.class);
    }

    @Test
    public void should_return_iFine_product() {
        Product product = productFactory.getProduct(ProductType.PRODUCT_IFINE.getName());
        assertThat(product).isInstanceOf(ProductIFine.class);
    }
}
