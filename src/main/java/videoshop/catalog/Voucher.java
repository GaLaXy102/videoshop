package videoshop.catalog;

import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;

/**
 * Class representing a gift voucher as a Buyable
 */
@Entity
public class Voucher extends Buyable {
    /**
     * Create a new voucher
     * @param price Price of the new voucher
     */

    public Voucher(MonetaryAmount price) {
        super("Temporary placeholder", price, BuyableType.VOUCHER);
        Assert.notNull(price, "Price must not be null!");
        Assert.isTrue(price.isPositive(), "Cannot create a worthless Voucher!");
        this.setName("Gift voucher of " + price.getNumber() + " " + price.getCurrency());
    }

    @SuppressWarnings("unused")
    private Voucher() {}

}
