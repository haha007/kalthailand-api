package th.co.krungthaiaxa.api.elife.products;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiApplication;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ProductServiceFactoryTest {
    @Inject
    private ProductServiceFactory productServiceFactory;

    @Test
    public void should_return_error_when_product_id_is_unknown() {
        assertThatThrownBy(() -> productServiceFactory.getProduct("something"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_return_10EC_product() {
        ProductService productService = productServiceFactory.getProduct(ProductType.PRODUCT_10_EC.getName());
        assertThat(productService).isInstanceOf(Product10ECService.class);
    }

    @Test
    public void should_return_iBegin_product() {
        ProductService productService = productServiceFactory.getProduct(ProductType.PRODUCT_IBEGIN.getName());
        assertThat(productService).isInstanceOf(ProductIBeginService.class);
    }

    @Test
    public void should_return_iFine_product() {
        ProductService productService = productServiceFactory.getProduct(ProductType.PRODUCT_IFINE.getName());
        assertThat(productService).isInstanceOf(ProductIFineService.class);
    }

    @Test
    public void should_return_iGen_product() {
        ProductService productService = productServiceFactory.getProduct(ProductType.PRODUCT_IGEN.getName());
        assertThat(productService).isInstanceOf(ProductIGenService.class);
    }
    
    @Test
    public void should_return_product_criteria_list(){
    	List<Map<String,Object>> productCriteriaList = productFactory.getProductCriteriaList();
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_10_EC.toString());
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_10_EC.getName());
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_IBEGIN.toString());
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_IBEGIN.getName());
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_IFINE.toString());
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_IFINE.getName());
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_IGEN.toString());
    	assertThat(productCriteriaList.toString()).containsOnlyOnce(ProductType.PRODUCT_IGEN.getName());
    }
}
