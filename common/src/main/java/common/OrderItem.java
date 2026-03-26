package common;

import java.io.Serializable;

public class OrderItem implements Serializable {
    public String drinkName;
    public int quantity;

    public OrderItem(String drinkName, int quantity) {
        this.drinkName = drinkName;
        this.quantity = quantity;
    }
}
