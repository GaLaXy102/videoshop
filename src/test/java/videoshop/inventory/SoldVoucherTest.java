package videoshop.inventory;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.salespointframework.core.Currencies.EURO;

/**
 * TestCases for SoldVoucher
 */
public class SoldVoucherTest {
    @Test
    void create() {
        MonetaryAmount value = Money.of(1, EURO);
        SoldVoucher soldVoucher = new SoldVoucher(value);
        assertThat(soldVoucher).isNotNull();
        assertThat(soldVoucher.getValue()).isEqualTo(value);
        assertThat(soldVoucher.getIdentifier()).isNotNull();
        assertThat(soldVoucher.getIdentifier()).isNotEmpty();
        assertThat(soldVoucher.getPass()).isNotNull();
        assertThat(soldVoucher.getPass()).isNotEmpty();
    }

    @Test
    void rejectValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SoldVoucher(null));
        assertThatIllegalArgumentException().isThrownBy(() -> new SoldVoucher(Money.of(-1, EURO)));
        assertThatIllegalArgumentException().isThrownBy(() -> new SoldVoucher(Money.of(0, EURO)));
    }

    @Test
    void testMatchPass() {
        MonetaryAmount value = Money.of(1, EURO);
        SoldVoucher soldVoucher = new SoldVoucher(value);
        assertThat(soldVoucher.matchPass(soldVoucher.getPass())).isTrue();
    }
}
