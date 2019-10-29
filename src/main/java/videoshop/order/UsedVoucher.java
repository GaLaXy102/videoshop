package videoshop.order;


import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.springframework.util.Assert;
import videoshop.inventory.SoldVoucher;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import static org.salespointframework.core.Currencies.EURO;

/**
 * Final stage of the Voucher lifecycle
 */
@Entity
public class UsedVoucher extends Product {
    private MonetaryAmount availableValue;
    @OneToOne
    private SoldVoucher assignedSoldVoucher;

    /**
     * Create a new instance of a UsedVoucher
     * from a {@link SoldVoucher}
     *
     * @param soldVoucher associated {@link SoldVoucher}
     */
    public UsedVoucher(SoldVoucher soldVoucher) {
        // True value will be set later
        super("Using gift voucher", Money.of(0.0, EURO));
        Assert.notNull(soldVoucher, "soldVoucher must not be null");
        this.assignedSoldVoucher = soldVoucher;
        this.renewAvailableValue();
    }

    @SuppressWarnings("unused")
    private UsedVoucher() { }

    /**
     * Recalculate the available value of this {@link UsedVoucher} by quering the associated {@link SoldVoucher}
     *
     * @throws IllegalArgumentException when there is no assigned {@link SoldVoucher}
     */
    public void renewAvailableValue() {
        Assert.notNull(this.assignedSoldVoucher, "No assigned SoldVoucher. Find first!");
        // Copy data from assigned voucher by value, not by reference
        double value = this.assignedSoldVoucher.getValue().getNumber().doubleValueExact();
        CurrencyUnit currencyUnit = this.assignedSoldVoucher.getValue().getCurrency();
        this.availableValue = Money.of(value, currencyUnit);
        this.setPrice(this.availableValue.negate());
    }

    /**
     * Setter for available value
     *
     * @param updatedAvailableValue new available Value
     */
    public void renewAvailableValue(MonetaryAmount updatedAvailableValue) {
        Assert.notNull(this.assignedSoldVoucher, "No assigned SoldVoucher. Find first!");
        Assert.notNull(updatedAvailableValue, "New Value must not be null!");
        Assert.isTrue(updatedAvailableValue.isPositiveOrZero(), "UsedVouchers can't have negative Value!");
        // Set data in this structure
        this.setPrice(this.getAvailableValue().subtract(updatedAvailableValue).negate());
        this.availableValue = updatedAvailableValue;
        // Copy data to assigned voucher by value, not by reference
        double value = this.availableValue.getNumber().doubleValueExact();
        CurrencyUnit currencyUnit = this.availableValue.getCurrency();
        this.assignedSoldVoucher.setValue(Money.of(value, currencyUnit));
    }


    /**
     * Getter for UsedVoucher's availableValue
     *
     * @return availableValue as javax.money.MonetaryAmount
     */
    public MonetaryAmount getAvailableValue() {
        return this.availableValue;
    }

    /**
     * Getter for UsedVoucher's assignedSoldVoucher
     *
     * @return assignedSoldVoucher as videoshop.inventory.SoldVoucher
     */
    public SoldVoucher getAssignedSoldVoucher() {
        return this.assignedSoldVoucher;
    }
}
