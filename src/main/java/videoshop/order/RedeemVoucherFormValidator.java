package videoshop.order;

import org.salespointframework.order.Cart;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import videoshop.inventory.SoldVoucher;
import videoshop.inventory.VoucherInventory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Validator for RedeemVoucherForm
 */
public class RedeemVoucherFormValidator implements Validator {

    private final VoucherInventory voucherInventory;
    private final Cart cart;

    /**
     * Initialize a new Validator instance
     *
     * @param voucherInventory Database ({@link VoucherInventory} of all sold vouchers (from JPA)
     * @param cart             {@link Cart} associated to the redemption
     */
    public RedeemVoucherFormValidator(VoucherInventory voucherInventory, Cart cart) {
        this.voucherInventory = voucherInventory;
        this.cart = cart;
    }

    /**
     * Check whether a class can be validated with this {@link Validator}
     *
     * @param aClass Class
     * @return true if this class can be validated
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return RedeemVoucherForm.class.equals(aClass);
    }

    /**
     * Validate the given Object and bind any Errors
     *
     * @param o      Object to be validated
     * @param errors Error data structure
     */
    @Override
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "id", "id.empty");
        ValidationUtils.rejectIfEmpty(errors, "pwd", "pwd.empty");
        RedeemVoucherForm redeemVoucherForm = (RedeemVoucherForm) o;
        AtomicReference<SoldVoucher> foundVoucher = new AtomicReference<>();
        this.voucherInventory.findAll().forEach(soldVoucher -> {
            if (soldVoucher.getIdentifier().equals(redeemVoucherForm.getId())) {
                foundVoucher.set(soldVoucher);
            }
        });
        if (foundVoucher.get() == null) {
            errors.rejectValue("id", "id.invalid");
        } else if (cart.get().anyMatch(cartItem -> {
            if (cartItem.getProduct() instanceof UsedVoucher) {
                UsedVoucher uv = (UsedVoucher) cartItem.getProduct();
                return uv.getAssignedSoldVoucher().getIdentifier().equals(foundVoucher.get().getIdentifier());
            } else {
                return false;
            }
        })) {
            errors.rejectValue("id", "id.used");
        } else if (!foundVoucher.get().matchPass(redeemVoucherForm.getPwd())) {
            errors.rejectValue("pwd", "pwd.invalid");
        }
    }
}
