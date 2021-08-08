package vendingmachine.model;

public class Product {
    private final String name;
    private int amount;
    private final Money money;

    public Product(String name, int amount, Money money) {
        this.name = name;
        this.amount = amount;
        this.money = money;
    }

    public static Product of(String name, String amount, String money) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 이름은 공백일 수 없습니다.");
        }

        int parsedAmount;
        try {
            parsedAmount = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("입력한 수량이 숫자가 아닙니다.");
        }

        validateAmount(parsedAmount);

        return new Product(name, parsedAmount, Money.from(money));
    }

    private static void validateAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("수량은 음수일 수 없습니다.");
        }
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

    public void sold() {
        this.amount -= 1;
        validateAmount(this.amount);
    }
}
