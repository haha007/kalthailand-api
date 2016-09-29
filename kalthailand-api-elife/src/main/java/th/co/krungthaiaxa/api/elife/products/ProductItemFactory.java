package th.co.krungthaiaxa.api.elife.products;

import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.elife.model.Item;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductItemFactory {

    public List<Item> getProductItems() {
        ProductType[] productTypes = ProductType.values();
        return Arrays.asList(productTypes).stream().map(productType -> toItem(productType)).collect(Collectors.toList());
    }

    private Item toItem(ProductType productType) {
        Item result = new Item();
        result.setName(productType.getLogicName());
        result.setValue(productType.name());
        return result;
    }
}
