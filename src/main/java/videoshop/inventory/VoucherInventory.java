package videoshop.inventory;

import org.springframework.data.repository.CrudRepository;

/**
 * Interface for JPA to store our SoldVouchers
 */
public interface VoucherInventory extends CrudRepository<SoldVoucher, Long> {
}
