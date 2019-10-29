package videoshop.order;

import org.springframework.data.repository.CrudRepository;

/**
 * Interface for JPA to store our UsedVouchers
 */
public interface UsedVoucherInventory extends CrudRepository<UsedVoucher, Long> {
}