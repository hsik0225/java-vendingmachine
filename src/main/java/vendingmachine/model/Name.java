package vendingmachine.model;

import java.util.Objects;

public class Name {
    private final String name;

    public Name(String name) {
        this.name = name;
    }

    public static Name from(String name) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 이름은 공백일 수 없습니다.");
        }

        return new Name(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Name))
            return false;
        Name name1 = (Name) o;
        return Objects.equals(getName(), name1.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
