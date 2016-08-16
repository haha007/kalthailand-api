package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import static th.co.krungthaiaxa.api.elife.products.ProductType.*;

@Component
public class ProductFactory {
    private final ProductIBegin productIBegin;
    private final ProductIFine productIFine;
    private final ProductIGen productIGen;

    @Inject
    public ProductFactory(ProductIBegin productIBegin, ProductIFine productIFine, ProductIGen productIGen) {
        this.productIBegin = productIBegin;
        this.productIFine = productIFine;
        this.productIGen = productIGen;
    }

    public Product getProduct(String productId) {
        if (productId.equals(ProductType.PRODUCT_10_EC.getName())) {
            return new Product10EC();
        } else if (productId.equals(ProductType.PRODUCT_IBEGIN.getName())) {
            return productIBegin;
        } else if (productId.equals(ProductType.PRODUCT_IFINE.getName())) {
            return productIFine;
        } else if (productId.equals(ProductType.PRODUCT_IGEN.getName())) {
            return productIGen;
        } else {
            throw new IllegalArgumentException("The Product [" + productId + "] is unknown.");
        }
    }
    
    public List<Map<String,Object>> getProductCriteriaList(){
    	
    	String id = "id";
    	String name = "name";
    	
    	List<Map<String,Object>> productCriteriaList = new ArrayList<>();
    	
    	Map<String,Object> mapIBegin = new HashMap<>();
    	mapIBegin.put(id, ProductType.PRODUCT_IBEGIN);
    	mapIBegin.put(name, ProductType.PRODUCT_IBEGIN.getName());
    	
    	Map<String,Object> mapIFine = new HashMap<>();
    	mapIFine.put(id, ProductType.PRODUCT_IFINE);
    	mapIFine.put(name, ProductType.PRODUCT_IFINE.getName());
    	
    	Map<String,Object> mapIGen = new HashMap<>();
    	mapIGen.put(id, ProductType.PRODUCT_IGEN);
    	mapIGen.put(name, ProductType.PRODUCT_IGEN.getName());
    	
    	Map<String,Object> map10EC = new HashMap<>();
    	map10EC.put(id, ProductType.PRODUCT_10_EC);
    	map10EC.put(name, ProductType.PRODUCT_10_EC.getName());
    	
    	productCriteriaList.add(map10EC);
    	productCriteriaList.add(mapIBegin);
    	productCriteriaList.add(mapIFine);
    	productCriteriaList.add(mapIGen);
    	
    	return productCriteriaList;
    	
    }
}
