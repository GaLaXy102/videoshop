package videoshop.catalog;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.salespointframework.core.Currencies.EURO;
import static videoshop.catalog.Buyable.BuyableType.VOUCHER;

/**
 * Unittest for the Voucher class
 */
public class VoucherTest {

    @Test
    void create() {
        MonetaryAmount value = Money.of(1, EURO);
        Voucher voucher = new Voucher(value);
        assertThat(voucher).isNotNull();
        assertThat(voucher.getType()).isEqualTo(VOUCHER);
        assertThat(voucher.getId()).isNotNull();
        assertThat(voucher.getName()).isEqualTo("Gift voucher of 1 EUR");
        assertThat(voucher.getPrice()).isEqualTo(value);
    }

    @Test
    void rejectInvalidValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Voucher(null));
        assertThatIllegalArgumentException().isThrownBy(() -> new Voucher(Money.of(0, EURO)));
        assertThatIllegalArgumentException().isThrownBy(() -> new Voucher(Money.of(-1.23, EURO)));
    }

}
