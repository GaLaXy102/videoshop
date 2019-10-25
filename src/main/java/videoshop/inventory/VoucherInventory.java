package videoshop.inventory;

import org.javamoney.moneta.Money;
import org.springframework.data.repository.CrudRepository;

import static org.salespointframework.core.Currencies.EURO;

/**
 * Interface for JPA to store our SoldVouchers
 */
public interface VoucherInventory extends CrudRepository<SoldVoucher,Long> {

    /**
     * Create a method (for later) to disable all SoldVouchers without remaining value
     */
    default void clearZeroValue() {
        this.findAll().forEach(soldVoucher -> {
            if (soldVoucher.getValue().equals(Money.of(0, EURO))) {
                this.delete(soldVoucher);
            }
        });
    }

}
