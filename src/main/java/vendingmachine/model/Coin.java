package vendingmachine.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Coin {
    FIVE_HUNDRED(500), ONE_HUNDRED(100), FIFTY(50), TEN(10);

    private final int value;

    Coin(int value) {
        this.value = value;
    }

    public static List<Integer> descendingOrder() {
        return Arrays.stream(Coin.values())
                     .map(Coin::getValue)
                     .sorted((c1, c2) -> c2 - c1)
                     .collect(Collectors.toList());
    }

    public static int minValueCoin() {
        final List<Integer> coins = descendingOrder();
        return coins.get(coins.size() - 1);
    }

    public int getValue() {
        return value;
    }
}
