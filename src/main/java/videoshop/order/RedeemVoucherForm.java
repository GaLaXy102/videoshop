package videoshop.order;

import javax.validation.constraints.NotBlank;

/**
 * Type to bind the form to redeem SoldVouchers to
 */
public class RedeemVoucherForm {
    private final @NotBlank String id;
    private final @NotBlank String pwd;

    public RedeemVoucherForm(String id, String pwd) {
        this.id = id;
        this.pwd = pwd;
    }

    /**
     * Getter for RedeemVoucherForm's id
     *
     * @return id as java.lang.String
     */
    public String getId() {
        return this.id;
    }

    /**
     * Getter for RedeemVoucherForm's pwd
     *
     * @return pwd as java.lang.String
     */
    public String getPwd() {
        return this.pwd;
    }

    public UsedVoucher toUsedVoucher() {
        return new UsedVoucher(id);
    }
}
