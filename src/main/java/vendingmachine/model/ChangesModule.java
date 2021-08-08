package vendingmachine.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.woowahan.techcourse.utils.Randoms;

public class ChangesModule {

    private final Map<Integer, Integer> coinBox;

    public ChangesModule(Map<Integer, Integer> coinBox) {
        this.coinBox = new HashMap<>(coinBox);
    }

    public static ChangesModule from(Money money) {
        return new ChangesModule(changeMoneyToCoins(money));
    }

    private static Map<Integer, Integer> changeMoneyToCoins(Money money) {
        Map<Integer, Integer> coinCounts = new HashMap<>();
        List<Integer> coins = Coin.coins();
        int amount = money.getMoney();
        if (amount < 10) {
            return new HashMap<>();
        }

        while (amount > 0) {
            final int coin = Randoms.pick(coins);
            if (amount >= coin) {
                coinCounts.merge(coin, 1, (oldValue, newValue) -> oldValue + 1);
                amount -= coin;
            }
        }
        return coinCounts;
    }

    public Map<Integer, Integer> getCoinBox() {
        return new HashMap<>(coinBox);
    }

    public void addMoney(Money inputMoney) {
        final Map<Integer, Integer> inputCoins = changeMoneyToCoins(inputMoney);
        for (int coin : Coin.coins()) {
            final Integer count = inputCoins.get(coin);
            if (count != null) {
                coinBox.put(coin, count);
            }
        }
    }

    public boolean canReturnChanges(Money remainingMoney) {
        int amount = remainingMoney.getMoney();
        for (int coin : Coin.descendingOrder()) {
            final int storedCoinCount = coinBox.getOrDefault(coin, 0);
            final int returnCoinCount = amount / coin;
            amount -= Math.min(storedCoinCount, returnCoinCount) * coin;
        }
        return amount == 0;
    }
}
