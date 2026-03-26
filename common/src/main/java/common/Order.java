package common;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    public String customerName;
    public String branch;
    public List<OrderItem> items;

    public Order(String customerName, String branch, List<OrderItem> items) {
        this.customerName = customerName;
        this.branch = branch;
        this.items = items;
    }
}
