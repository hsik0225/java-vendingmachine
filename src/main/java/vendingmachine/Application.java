package vendingmachine;

import vendingmachine.model.Money;
import vendingmachine.model.Products;
import vendingmachine.model.VendingMachine;
import vendingmachine.view.InputView;
import vendingmachine.view.OutputView;

public class Application {
    public static void main(String[] args) {
        final Money holdingMoney = InputView.askHoldingMoney();
        final Products products = InputView.askProducts();
        final VendingMachine vendingMachine = new VendingMachine(products, holdingMoney);

        final Money inputMoney = InputView.askInputMoney();
        vendingMachine.insertMoney(inputMoney);

        while (vendingMachine.canBuyAnything()) {
            OutputView.printRemainingMoney(vendingMachine.getRemainingMoney());
            final String productName = InputView.askProductToBuy();
            vendingMachine.buy(productName);
        }

        OutputView.printCoinCount(vendingMachine.getCoinCount());
    }
}
