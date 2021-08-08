package vendingmachine.model;

public class Money {
    private int money;

    private Money(int money) {
        this.money = money;
    }

    public static Money from(String money) {
        int parsedMoney;

        try {
            parsedMoney = Integer.parseInt(money);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자가 아닌 문자가 입력되었습니다.");
        }

        return from(parsedMoney);
    }

    public static Money from(int money) {
        if (money <= 0) {
            throw new IllegalArgumentException("0 이하의 숫자가 입력되었습니다.");
        }

        if (money % 10 != 0) {
            throw new IllegalArgumentException("돈은 10의 배수이어야 합니다.");
        }

        return new Money(money);
    }

    public int getMoney() {
        return money;
    }

    public void subtract(Money money) {
        final int changes = this.money - money.money;
        if (changes < 0) {
            throw new IllegalArgumentException("차감하려는 금액이 현재 입금 금액보다 큽니다.");
        }

        this.money = changes;
    }
}
