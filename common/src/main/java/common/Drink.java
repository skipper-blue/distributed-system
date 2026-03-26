package common;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Drink implements Serializable {
    private static final long serialVersionUID = 2986677574050457488L;
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");

    private int id;
    private String name;
    private double price;

    public Drink(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " - KSh " + PRICE_FORMAT.format(price);
    }
}
