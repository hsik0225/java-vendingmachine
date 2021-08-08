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

    public static List<Integer> coins() {
        return Arrays.stream(Coin.values())
                     .map(Coin::getValue)
                     .collect(Collectors.toList());
    }

    public int getValue() {
        return value;
    }
}
