package vendingmachine.view;

import java.util.Map;

public class OutputView {

    public static void printRemainingMoney(int holdingMoney) {
        System.out.printf("투입된 금액: %d원\n", holdingMoney);
    }

    public static void printChanges(int holdingMoney) {
        System.out.printf("잔돈\n%d원", holdingMoney);
    }

    public static void printChanges(Map<Integer, Integer> coinCount) {
        System.out.println("잔돈");

        if (coinCount.isEmpty()) {
            System.out.println("0원");
        }

        coinCount.entrySet()
                 .stream()
                 .sorted((c1, c2) -> c2.getKey().compareTo(c1.getKey()))
                 .forEach(entry -> System.out.printf("%d원 - %d 개\n", entry.getKey(), entry.getValue()));
    }
}
