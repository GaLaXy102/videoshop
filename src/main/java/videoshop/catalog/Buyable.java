package videoshop.catalog;

import org.salespointframework.catalog.Product;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;

/**
 * Class representing a Buyable Object, which is a Product with a specific type
 */
@Entity
public class Buyable extends Product {

    /**
     * Possible types of Buyables
     */
    public static enum BuyableType {
        BLURAY, DVD, VOUCHER;
    }

    private BuyableType type;

    /**
     * Create a new instance
     * @param name Name of the Buyable
     * @param price Price of the Buyable
     * @param type Type of the Buyable
     */
    public Buyable(String name, MonetaryAmount price, BuyableType type) {
        super(name, price);
        Assert.notNull(type, "Type must not be null!");
        this.type = type;
    }

    @SuppressWarnings("unused")
    Buyable() {
    }

    /**
     * Get the Type of a Buyable
     * @return BuyableType
     */
    public BuyableType getType() {
        return type;
    }

    /**
     * Check whether this Buyable is either a DVD or a BluRay
     * @return false if this Buyable is a Voucher, else true
     */
    public boolean isDisc() {
        return (this.type == BuyableType.BLURAY || this.type == BuyableType.DVD);
    }
}
