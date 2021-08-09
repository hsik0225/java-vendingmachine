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
        List<Integer> coins = Coin.descendingOrder();
        int amount = money.getMoney();
        while (amount >= Coin.minValueCoin()) {
            final int coin = Randoms.pick(coins);
            if (amount >= coin) {
                coinCounts.merge(coin, 1, (oldValue, newValue) -> oldValue + 1);
                amount -= coin;
            }
        }
        return coinCounts;
    }

    public Map<Integer, Integer> returnChanges(Money remainingMoney) {
        Map<Integer, Integer> coinCount = new HashMap<>();
        int money = remainingMoney.getMoney();
        for (Integer coin : Coin.descendingOrder()) {
            final int storedCount = coinBox.get(coin);
            final int needCount = money / coin;
            final int min = Math.min(storedCount, needCount);
            if (min != 0) {
                coinCount.put(coin, min);
            }
            money -= min * coin;
        }
        return coinCount;
    }

    public void addMoney(Money inputMoney) {
        final Map<Integer, Integer> inputCoins = changeMoneyToCoins(inputMoney);
        for (int coin : Coin.descendingOrder()) {
            final Integer count = inputCoins.get(coin);
            if (count != null) {
                coinBox.merge(coin, count, Integer::sum);
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
