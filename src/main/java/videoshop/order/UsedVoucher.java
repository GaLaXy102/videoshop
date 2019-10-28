package videoshop.order;


import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.springframework.util.Assert;
import videoshop.inventory.SoldVoucher;
import videoshop.inventory.VoucherInventory;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.concurrent.atomic.AtomicReference;

import static org.salespointframework.core.Currencies.EURO;

/**
 * Final stage of the Voucher lifecycle
 */
@Entity
public class UsedVoucher extends Product {
    private final String identifier;
    private MonetaryAmount availableValue;
    @OneToOne
    private SoldVoucher assignedSoldVoucher;

    /**
     * Create a new instance of a UsedVoucher
     * The SoldVoucher must be bound explictly by calling findAssignedSoldVoucher or setAssignedVoucher!
     *
     * @param identifier Identifier of used {@link SoldVoucher}
     */
    public UsedVoucher(String identifier) {
        // True value will be set later
        super("Using gift voucher", Money.of(0.0, EURO));
        Assert.notNull(identifier, "Identifier must not be null");
        Assert.isTrue(!identifier.isEmpty(), "Identfier must not be empty");
        this.identifier = identifier;
        this.assignedSoldVoucher = null;
        this.availableValue = Money.of(0, EURO);
    }

    @SuppressWarnings("unused")
    private UsedVoucher() {
        this.identifier = null;
    }

    /**
     * Find the associated {@link SoldVoucher} in the given inventory
     *
     * @param voucherInventory {@link VoucherInventory} to be searched
     */
    public void findAssignedSoldVoucher(VoucherInventory voucherInventory) {
        // Pass if assigned SoldVoucher is already found
        if (this.assignedSoldVoucher != null) return;
        AtomicReference<SoldVoucher> foundVoucher = new AtomicReference<>();
        voucherInventory.findAll().forEach(soldVoucher -> {
            if (soldVoucher.getIdentifier().equals(this.identifier)) {
                foundVoucher.set(soldVoucher);
            }
        });
        Assert.notNull(foundVoucher.get(), "Voucher not found in this inventory");
        this.assignedSoldVoucher = foundVoucher.get();
        this.renewAvailableValue();
    }

    /**
     * Set the assigned {@link SoldVoucher}
     *
     * @param assignedSoldVoucher the SoldVoucher to be assigned to this instance
     */
    public void setAssignedSoldVoucher(SoldVoucher assignedSoldVoucher) {
        Assert.notNull(assignedSoldVoucher, "assignedSoldVoucher must not be null!");
        this.assignedSoldVoucher = assignedSoldVoucher;
        this.renewAvailableValue();
    }

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
     * Getter for UsedVoucher's identifier
     *
     * @return identifier as java.lang.String
     */
    public String getIdentifier() {
        return this.identifier;
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
