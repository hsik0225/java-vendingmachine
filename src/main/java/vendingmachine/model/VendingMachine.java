package vendingmachine.model;

import java.util.Map;

public class VendingMachine {

    private final Products products;
    private final ChangesModule changesModule;
    private Money remainingMoney;
    private boolean canReturnChanges;

    public VendingMachine(Products products, Money holdingMoney) {
        this(products, ChangesModule.from(holdingMoney), true);
    }

    public VendingMachine(Products products, ChangesModule changesModule, boolean canReturnChanges) {
        this.products = products;
        this.changesModule = changesModule;
        this.canReturnChanges = canReturnChanges;
    }

    public void insertMoney(Money inputMoney) {
        this.remainingMoney = inputMoney;
        this.changesModule.addMoney(inputMoney);
        this.canReturnChanges = changesModule.canReturnChanges(inputMoney);
    }

    public int getRemainingMoney() {
        return this.remainingMoney.getMoney();
    }

    public boolean hasMoneyMoreThanMinPrice() {
        return products.canBuyAnything(getRemainingMoney());
    }

    public Map<Integer, Integer> getCoinCount() {
        return changesModule.getCoinBox();
    }

    public boolean canBuy() {
        return hasMoneyMoreThanMinPrice() && this.canReturnChanges;
    }

    public void buy(Name productName) {
        final Product product = products.findByName(productName);
        final Money subtractedMoney = this.remainingMoney.subtract(product.getMoney());
        this.canReturnChanges = changesModule.canReturnChanges(subtractedMoney);
        if (canReturnChanges) {
            this.remainingMoney = subtractedMoney;
            product.decrease();
        }
    }

    public boolean canReturnChanges() {
        return this.canReturnChanges;
    }
}
