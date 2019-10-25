package videoshop.inventory;

import com.mysema.commons.lang.Assert;
import net.bytebuddy.utility.RandomString;
import org.salespointframework.catalog.Product;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;

/**
 * Class representing a SoldVoucher (i.e. a persistent and personalized version of Voucher)
 */
@Entity
public class SoldVoucher extends Product {
    private final String identifier;
    private final String pass;
    private MonetaryAmount value;

    /**
     * Create a new instance with a given value
     * @param value Value of the SoldVoucher as javax.money.MonetaryAmount
     */
    public SoldVoucher(MonetaryAmount value) {
        super("Sold Gift Voucher", value);
        Assert.notNull(this.getId(), "Error while deploying SoldVoucher");
        this.identifier = this.getId().toString();
        this.setValue(value);
        this.pass = RandomString.make();
    }

    @SuppressWarnings("unused")
    private SoldVoucher() {
        this.identifier = null;
        this.pass = null;
        this.value = null;
    }

    /**
     * Getter for SoldVoucher's identifier
     *
     * @return identifier as java.lang.String
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Getter for SoldVoucher's pass
     *
     * @return pass as java.lang.String
     */
    public String getPass() {
        return this.pass;
    }

    /**
     * Check whether SoldVoucher's pass matches the given pass
     *
     * @param pass Password to match
     * @return whether the given pass is correct
     */
    public boolean matchPass(String pass) {
        return this.pass.equals(pass);
    }

    /**
     * Getter for SoldVoucher's value
     *
     * @return value as javax.money.MonetaryAmount
     */
    public MonetaryAmount getValue() {
        return this.value;
    }

    /**
     * Setter for SoldVoucher's value
     *
     * @param value as javax.money.MonetaryAmount
     */
    public void setValue(MonetaryAmount value) {
        Assert.notNull(value, "Value must not be null!");
        Assert.isTrue(value.isPositive(), "Value must be positive!");
        this.value = value;
    }
}
