package vendingmachine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Products {

    private final List<Product> products;

    public Products() {
        this(new ArrayList<>());
    }

    public Products(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    public static Products from(List<Product> products) {
        Products productCollection = new Products();
        products.forEach(productCollection::add);
        return productCollection;
    }

    public void add(Product product) {
        if (findOptionalProductByName(product.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 자판기에 등록되어 있는 상품입니다.");
        }

        products.add(product);
    }

    public Product findByName(Name name) {
        return findOptionalProductByName(name).orElseThrow(() -> new IllegalArgumentException("자판기에 등록되지 않은 상품 이름입니다."));
    }

    private Optional<Product> findOptionalProductByName(Name name) {
        return products.stream()
                       .filter(product -> product.isNameEqualsTo(name))
                       .findAny();
    }

    public boolean canBuyAnything(int remainingMoney) {
        return products.stream()
                       .anyMatch(product -> product.getMoney().getMoney() <= remainingMoney);
    }
}
