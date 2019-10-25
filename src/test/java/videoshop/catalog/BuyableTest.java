package videoshop.catalog;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.salespointframework.core.Currencies.EURO;

public class BuyableTest {
    @Test
    void create() {
        Buyable buyable = new Buyable("Test", Money.of(1, EURO), Buyable.BuyableType.DVD);
        assertThat(buyable).isNotNull();
        assertThat(buyable.getType()).isEqualTo(Buyable.BuyableType.DVD);
    }

    @Test
    void rejectInvalidValue() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Buyable("Test", Money.of(1, EURO), null));
    }
}
