package videoshop.order;

import org.javamoney.moneta.Money;
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

    @Test
    void rejectsEmpty() {
        assertThatIllegalArgumentException().isThrownBy(() -> new UsedVoucher(null));
        assertThatIllegalArgumentException().isThrownBy(() -> new UsedVoucher(""));
    }

    @Test
    void initialize() {
        UsedVoucher toTest = new UsedVoucher("toTest");
        assertThat(toTest.getIdentifier()).isEqualTo("toTest");
        assertThat(toTest.getAvailableValue()).isEqualTo(Money.of(0.0, EURO));
        assertThat(toTest.getAssignedSoldVoucher()).isNull();
        assertThat(toTest.getPrice()).isEqualTo(Money.of(0.0, EURO));
    }

    @Test
    void renewValueFromAssignedVoucher() {
        SoldVoucher sV = new SoldVoucher(Money.of(4, EURO));
        UsedVoucher toTest = new UsedVoucher(sV.getIdentifier());
        assertThatIllegalArgumentException().isThrownBy(() -> toTest.renewAvailableValue(Money.of(2, EURO)));
        toTest.setAssignedSoldVoucher(sV);
        assertThat(toTest.getAvailableValue()).isEqualTo(Money.of(4, EURO));
        assertThat(toTest.getPrice()).isEqualTo(Money.of(4, EURO).negate());
    }

    @Test
    void renewValueFromParameter() {
        SoldVoucher sV = new SoldVoucher(Money.of(4, EURO));
        UsedVoucher toTest = new UsedVoucher(sV.getIdentifier());
        toTest.setAssignedSoldVoucher(sV);
        assertThatIllegalArgumentException().isThrownBy(() -> toTest.renewAvailableValue(null));
        assertThatIllegalArgumentException().isThrownBy(() -> toTest.renewAvailableValue(Money.of(-1, EURO)));
        toTest.renewAvailableValue(Money.of(3, EURO));
        assertThat(toTest.getAvailableValue()).isEqualTo(Money.of(3, EURO));
        assertThat(toTest.getPrice()).isEqualTo(Money.of(1, EURO).negate());
        assertThat(toTest.getAssignedSoldVoucher().getValue()).isEqualTo(Money.of(3, EURO));
    }

    @Test
    void findAssignedVoucher() {
        SoldVoucher sV = new SoldVoucher(Money.of(4, EURO));
        UsedVoucher toTest = new UsedVoucher(sV.getIdentifier());
        // We can't save in this Data structure, so only checking for Exceptions
        assertThatIllegalArgumentException().isThrownBy(() -> toTest.findAssignedSoldVoucher(voucherInventory));
        assertThatIllegalArgumentException().isThrownBy(() -> toTest.setAssignedSoldVoucher(null));
        toTest.setAssignedSoldVoucher(sV);
        toTest.findAssignedSoldVoucher(voucherInventory);
        assertThat(toTest.getAssignedSoldVoucher()).isEqualTo(sV);
    }
}
