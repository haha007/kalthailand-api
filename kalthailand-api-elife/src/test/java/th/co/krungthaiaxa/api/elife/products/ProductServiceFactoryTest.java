package th.co.krungthaiaxa.api.elife.products;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.model.Item;
import th.co.krungthaiaxa.api.elife.products.igen.IGenService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductServiceFactoryTest {
    @Inject
    private ProductItemFactory productFactory;

    @Inject
    private ProductServiceFactory productServiceFactory;

    @Test
    public void should_return_error_when_product_id_is_unknown() {
        assertThatThrownBy(() -> productServiceFactory.getProductService("something"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_return_10EC_product() {
        ProductService productService = productServiceFactory.getProductService(ProductType.PRODUCT_10_EC.getLogicName());
        assertThat(productService).isInstanceOf(Product10ECService.class);
    }

    @Test
    public void should_return_iBegin_product() {
        ProductService productService = productServiceFactory.getProductService(ProductType.PRODUCT_IBEGIN.getLogicName());
        assertThat(productService).isInstanceOf(ProductIBeginService.class);
    }

    @Test
    public void should_return_iFine_product() {
        ProductService productService = productServiceFactory.getProductService(ProductType.PRODUCT_IFINE.getLogicName());
        assertThat(productService).isInstanceOf(ProductIFineService.class);
    }

    @Test
    public void should_return_iGen_product() {
        ProductService productService = productServiceFactory.getProductService(ProductType.PRODUCT_IGEN.getLogicName());
        assertThat(productService).isInstanceOf(IGenService.class);
    }

    @Test
    public void should_return_product_criteria_list() {
        List<Item> productCriteriaList = productFactory.getProductItems();
        List<ProductType> allProductTypes = Arrays.asList(ProductType.values());
        productCriteriaList.stream().forEach((item) -> Assert.assertTrue(allProductTypes.contains(ProductType.valueOf(item.getValue()))));
        Assert.assertEquals(productCriteriaList.size(), allProductTypes.size());
    }
}
