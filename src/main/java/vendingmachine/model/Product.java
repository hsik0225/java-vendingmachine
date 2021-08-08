package vendingmachine.model;

public class Product {
    private final String name;
    private final int amount;
    private final Money money;

    public Product(String name, String amount, String money) {
        this(name, Integer.parseInt(amount), Money.from(money));
    }

    public Product(String name, int amount, Money money) {
        this.name = name;
        this.amount = amount;
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public Money getMoney() {
        return money;
    }

    public boolean isNameEqualsTo(String name) {
        return this.name.equals(name);
    }
}
