package vendingmachine.model;

import java.util.Map;

public class VendingMachine {

    private final Products products;
    private final ChangesModule changesModule;
    private Money remainingMoney;

    public VendingMachine(Products products, Money holdingMoney) {
        this(products, ChangesModule.from(holdingMoney));
    }

    public VendingMachine(Products products, ChangesModule changesModule) {
        this.products = products;
        this.changesModule = changesModule;
    }

    public void insertMoney(Money inputMoney) {
        this.remainingMoney = inputMoney;
        this.changesModule.addMoney(inputMoney);
    }

    public int getRemainingMoney() {
        return this.remainingMoney.getMoney();
    }

    public boolean canBuyAnything() {
        return products.canBuyAnything(getRemainingMoney()) && changesModule.canReturnChanges(this.remainingMoney);
    }

    public void buy(String productName) {
        final Product product = products.findByName(productName);
        this.remainingMoney.subtract(product.getMoney());
    }

    public Map<Integer, Integer> getCoinCount() {
        return changesModule.getCoinBox();
    }
}
