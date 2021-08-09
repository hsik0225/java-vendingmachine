package vendingmachine.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.woowahan.techcourse.utils.Scanners;

import vendingmachine.model.Money;
import vendingmachine.model.Name;
import vendingmachine.model.Product;
import vendingmachine.model.Products;

public class InputView {

    public static final int PRODUCT_NAME_INDEX = 0;
    public static final int PRODUCT_AMOUNT_INDEX = 1;
    public static final int PRODUCT_PRICE_INDEX = 2;

    public static final int BRACKET_BEGIN_INDEX = 1;
    public static final Pattern PRODUCT_PATTERN = Pattern.compile("((\\[[^\\]]+\\]),?)+?");

    public static Money askHoldingMoney() {
        return Money.from(Integer.parseInt(input("자판기가 보유하고 있는 금액을 입력해 주세요.")));
    }

    private static String input(String message) {
        System.out.println(message);
        final String input = Scanners.nextLine();
        System.out.println();
        return input;
    }

    public static Products askProducts() {
        return Products.from(convertInputToListOfProduct());
    }

    private static List<Product> convertInputToListOfProduct() {
        Matcher matcher = PRODUCT_PATTERN.matcher(input("상품명과 수량, 금액을 입력해 주세요.").trim());

        List<String> products = new ArrayList<>();
        while (matcher.find()) {
            products.add(matcher.group(2));
        }

        if (products.isEmpty()) {
            throw new IllegalArgumentException("상품 정보 포맷이 일치하지 않습니다. [<상품 이름>,<수량>,<가격>] 의 형태로 입력해주세요");
        }

        return products.stream()
                       .map(InputView::removeBrackets)
                       .map(product -> {
                           final String[] productInfo = product.split(",");
                           return Product.of(productInfo[PRODUCT_NAME_INDEX], productInfo[PRODUCT_AMOUNT_INDEX], productInfo[PRODUCT_PRICE_INDEX]);
                       })
                       .collect(Collectors.toList());
    }

    private static String removeBrackets(String product) {
        return product.substring(BRACKET_BEGIN_INDEX, product.length() - 1);
    }

    public static Money askInputMoney() {
        return Money.from(Integer.parseInt(input("투입할 금액을 입력해 주세요.")));
    }

    public static Name askProductToBuy() {
        return Name.from(input("구매할 상품명을 입력해 주세요."));
    }
}
