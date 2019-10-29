package videoshop.order;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import videoshop.inventory.SoldVoucher;
import videoshop.inventory.VoucherInventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.salespointframework.core.Currencies.EURO;

@ExtendWith(MockitoExtension.class)
public class UsedVoucherTest {

    @Mock
    VoucherInventory voucherInventory;
    private SoldVoucher sV;
    private UsedVoucher toTest;

    @BeforeEach
    void setup() {
        sV = new SoldVoucher(Money.of(4, EURO));
        toTest = new UsedVoucher(sV);
    }

    @Test
    void rejectsEmpty() {
        assertThatIllegalArgumentException().isThrownBy(() -> new UsedVoucher(null));
    }

    @Test
    void initialize() {
        assertThat(toTest.getAssignedSoldVoucher()).isEqualTo(sV);
        assertThat(toTest.getAvailableValue()).isEqualTo(Money.of(4.0, EURO));
        assertThat(toTest.getPrice()).isEqualTo(Money.of(4.0, EURO).negate());
    }

    @Test
    void renewValueFromParameter() {
        assertThatIllegalArgumentException().isThrownBy(() -> toTest.renewAvailableValue(null));
        assertThatIllegalArgumentException().isThrownBy(() -> toTest.renewAvailableValue(Money.of(-1, EURO)));
        toTest.renewAvailableValue(Money.of(3, EURO));
        assertThat(toTest.getAvailableValue()).isEqualTo(Money.of(3, EURO));
        assertThat(toTest.getPrice()).isEqualTo(Money.of(1, EURO).negate());
        assertThat(toTest.getAssignedSoldVoucher().getValue()).isEqualTo(Money.of(3, EURO));
    }
}
