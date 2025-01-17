package videoshop.order;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.order.Cart;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManager;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import videoshop.catalog.Buyable;
import videoshop.catalog.Disc;
import videoshop.catalog.Voucher;
import videoshop.inventory.VoucherInventory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.salespointframework.core.Currencies.EURO;

/**
 * TestCases for OrderController
 */
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
    @Mock
    OrderManager<Order> orderManager;
    @Mock
    VoucherInventory voucherInventory;
    @Mock
    UsedVoucherInventory usedVoucherInventory;
    @Mock
    UniqueInventory<UniqueInventoryItem> inventory;
    private Model model = new ExtendedModelMap();
    @Mock
    UserAccount userAccount;

    @Test
    void addVoucherToCart() {
        OrderController controller = new OrderController(orderManager, voucherInventory, usedVoucherInventory, inventory);
        Cart cart = controller.initializeCart();
        Voucher voucher = new Voucher(Money.of(12, EURO));
        controller.addItem(voucher, 1, cart);
        assertThat(cart.iterator().next().getProduct()).isEqualTo(voucher);
    }

    @Test
    void buyVoucher() {
        OrderController controller = new OrderController(orderManager, voucherInventory, usedVoucherInventory, inventory);
        Cart cart = controller.initializeCart();
        Voucher voucher = new Voucher(Money.of(12, EURO));
        Voucher voucher2 = new Voucher(Money.of(24, EURO));
        Disc disc = new Disc("Test", null, Money.of(1, EURO), null, Buyable.BuyableType.DVD);
        controller.addItem(voucher, 2, cart);
        controller.addItem(disc, 3, cart);
        controller.addItem(voucher2, 1, cart);
        String viewName = controller.buy(cart, Optional.of(userAccount), model);
        assertThat(viewName).isEqualTo("checkout");
        assertThat(model.getAttribute("soldVouchers")).isNotNull();
        assertThat(model.getAttribute("soldVouchers")).asList().hasSize(3);
    }

    @Test
    void getBasket() {
        OrderController controller = new OrderController(orderManager, voucherInventory, usedVoucherInventory, inventory);
        assertThat(controller.basket(new RedeemVoucherForm("test", "test"))).isEqualTo("cart");
    }

    @Test
    void buyDisc() {
        OrderController controller = new OrderController(orderManager, voucherInventory, usedVoucherInventory, inventory);
        Cart cart = controller.initializeCart();
        Disc disc = new Disc("Test", null, Money.of(1, EURO), null, Buyable.BuyableType.DVD);
        String viewName = controller.addItem(disc, 3, cart);
        assertThat(viewName).isEqualTo("redirect:dvds");
        Disc disc2 = new Disc("Test2", null, Money.of(1, EURO), null, Buyable.BuyableType.BLURAY);
        viewName = controller.addItem(disc2, 3, cart);
        assertThat(viewName).isEqualTo("redirect:blurays");
        viewName = controller.buy(cart, Optional.of(userAccount), model);
        assertThat(viewName).isEqualTo("redirect:/");
        assertThat(model.getAttribute("soldVouchers")).isNull();
    }


    @Test
    void voucherIsShown() {
        // Make our user the a Boss
        userAccount.add(Role.of("BOSS"));
        OrderController controller = new OrderController(orderManager, voucherInventory, usedVoucherInventory, inventory);
        String viewName = controller.orders(model);
        assertThat(viewName).isEqualTo("orders");
        assertThat(model.getAttribute("validVouchers")).isNotNull();
        assertThat(model.getAttribute("validVouchers")).isInstanceOfAny(List.class);
        // We won't test JPA here
    }

}
