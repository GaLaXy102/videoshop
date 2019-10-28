/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package videoshop.order;

import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.AbstractEntity;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.order.Cart;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManager;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import videoshop.catalog.Buyable;
import videoshop.catalog.Disc;
import videoshop.catalog.Voucher;
import videoshop.inventory.SoldVoucher;
import videoshop.inventory.VoucherInventory;

import javax.money.MonetaryAmount;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.salespointframework.core.Currencies.EURO;

/**
 * A Spring MVC controller to manage the {@link Cart}. {@link Cart} instances are held in the session as they're
 * specific to a certain user. That's also why the entire controller is secured by a {@code PreAuthorize} clause.
 *
 * @author Paul Henke
 * @author Oliver Gierke
 */
@Controller
@PreAuthorize("isAuthenticated()")
@SessionAttributes("cart")
class OrderController {

    private final OrderManager<Order> orderManager;
    private final VoucherInventory voucherInventory;
    private final UsedVoucherInventory usedVoucherInventory;
    private final UniqueInventory<UniqueInventoryItem> inventory;

    /**
     * Creates a new {@link OrderController} with the given {@link OrderManager}, {@link VoucherInventory},
     * {@link UsedVoucherInventory} and {@link UniqueInventory}.
     *
     * @param orderManager         must not be {@literal null}.
     * @param voucherInventory     must not be {@literal null}.
     * @param usedVoucherInventory must not be {@literal null}
     * @param inventory            must not be {@literal null}.
     */
    OrderController(OrderManager<Order> orderManager, VoucherInventory voucherInventory,
                    UsedVoucherInventory usedVoucherInventory, UniqueInventory<UniqueInventoryItem> inventory) {

        Assert.notNull(orderManager, "OrderManager must not be null!");
        Assert.notNull(voucherInventory, "VoucherInventory must not be null");
        Assert.notNull(usedVoucherInventory, "UsedVoucherInventory must not be null");
        Assert.notNull(inventory, "UniqueInventory must not be null");
        this.orderManager = orderManager;
        this.voucherInventory = voucherInventory;
        this.usedVoucherInventory = usedVoucherInventory;
        this.inventory = inventory;
    }

    /**
     * Creates a new {@link Cart} instance to be stored in the session (see the class-level {@link SessionAttributes}
     * annotation).
     *
     * @return a new {@link Cart} instance.
     */
    @ModelAttribute("cart")
    Cart initializeCart() {
        return new Cart();
    }

    /**
     * Adds a {@link Disc} to the {@link Cart}. Note how the type of the parameter taking the request parameter
     * {@code pid} is {@link Disc}. For all domain types extending {@link AbstractEntity} (directly or indirectly) a tiny
     * Salespoint extension will directly load the object instance from the database. If the identifier provided is
     * invalid (invalid format or no {@link Product} with the id found), {@literal null} will be handed into the method.
     *
     * @param buyable the disc that should be added to the cart (may be {@literal null}).
     * @param number  number of discs that should be added to the cart.
     * @param cart    must not be {@literal null}.
     * @return the view name.
     */
    @PostMapping("/cart")
    String addItem(@RequestParam("pid") Buyable buyable, @RequestParam("number") int number, @ModelAttribute Cart cart) {

        // (｡◕‿◕｡)
        // Das Inputfeld im View ist eigentlich begrenzt, allerdings sollte man immer auch serverseitig validieren
        int amount = number <= 0 || number > 5 ? 1 : number;

        // (｡◕‿◕｡)
        // Wir fügen dem Warenkorb die Disc in entsprechender Anzahl hinzu.
        cart.addOrUpdateItem(buyable, Quantity.of(amount));

        // (｡◕‿◕｡)
        // Je nachdem ob disc eine DVD oder eine Bluray ist, leiten wir auf die richtige Seite weiter

        switch (buyable.getType()) {
            case DVD:
                return "redirect:dvds";
            case BLURAY:
                return "redirect:blurays";
            case VOUCHER:
            default:
                return "redirect:vouchers";
        }
    }

    /**
     * Redeem a previously bought Voucher
     *
     * @param redeemVoucherForm Form data binding
     * @param errors            Errors while binding form data
     * @param cart              Cart to apply the Voucher to
     * @return next view name
     */
    @PostMapping("/cart/redeem")
    String redeemVoucher(@ModelAttribute("redeemVoucherForm") @Validated RedeemVoucherForm redeemVoucherForm, Errors errors, @ModelAttribute Cart cart) {
        // How is validation thought to be done?
        Validator validator = new RedeemVoucherFormValidator(voucherInventory, cart);
        validator.validate(redeemVoucherForm, errors);
        if (errors.hasErrors()) {
            return basket(redeemVoucherForm);
        }
        UsedVoucher toRedeem = redeemVoucherForm.toUsedVoucher();
        toRedeem.findAssignedSoldVoucher(voucherInventory);
        toRedeem.renewAvailableValue();
        cart.addOrUpdateItem(toRedeem, Quantity.of(1));
        return "redirect:/cart";
    }

    /**
     * Create binding for cart with associated Voucher form
     *
     * @param redeemVoucherForm Form data binding
     * @return next view name
     */
    @GetMapping("/cart")
    String basket(@ModelAttribute("redeemVoucherForm") RedeemVoucherForm redeemVoucherForm) {
        return "cart";
    }

    /**
     * Checks out the current state of the {@link Cart}. Using a method parameter of type {@code Optional<UserAccount>}
     * annotated with {@link LoggedIn} you can access the {@link UserAccount} of the currently logged in user.
     *
     * @param cart        will never be {@literal null}.
     * @param userAccount will never be {@literal null}.
     * @param model       data structure for next view
     * @return the view name.
     */
    @PostMapping("/checkout")
    String buy(@ModelAttribute Cart cart, @LoggedIn Optional<UserAccount> userAccount, Model model) {

        return userAccount.map(account -> {// (｡◕‿◕｡)
            // Mit completeOrder(…) wird der Warenkorb in die Order überführt, diese wird dann bezahlt und abgeschlossen.
            // Orders können nur abgeschlossen werden, wenn diese vorher bezahlt wurden.
            var order = new Order(account, Cash.CASH);
            cart.addItemsTo(order);
            List<UsedVoucher> usedVouchers = getUsedVouchers(cart);
            recalculateVoucherValues(order, usedVouchers);
            MonetaryAmount remainder = calculateRemainderAndUpdateDatabase(usedVouchers);
            order.addChargeLine(remainder, "Remaining voucher value");
            orderManager.payOrder(order);
            orderManager.completeOrder(order);
            List<SoldVoucher> soldVouchersInOrder = createSoldVouchersFromCart(cart);
            cart.clear();
            if (soldVouchersInOrder.isEmpty() && usedVouchers.isEmpty()) {
                return "redirect:/";
            } else {
                // Show SoldVouchers' details
                model.addAttribute("soldVouchers", soldVouchersInOrder);
                model.addAttribute("usedVouchers", usedVouchers);
                return "checkout";
            }
        }).orElse("redirect:/cart");
    }

    /**
     * Calculate the total of remaining value on the usedVouchers and make the usage persistent
     *
     * @param usedVouchers affected Vouchers
     * @return Remaining sum on these Vouchers
     */
    private MonetaryAmount calculateRemainderAndUpdateDatabase(List<UsedVoucher> usedVouchers) {
        MonetaryAmount remainder = Money.of(0, EURO);
        for (UsedVoucher usedVoucher : usedVouchers) {
            // Update all SoldVouchers
            this.voucherInventory.save(usedVoucher.getAssignedSoldVoucher());
            // Add UsedVouchers to inventory to make them sellable
            this.usedVoucherInventory.save(usedVoucher);
            this.inventory.save(new UniqueInventoryItem(usedVoucher, Quantity.of(1)));
            remainder = remainder.add(usedVoucher.getAvailableValue());
        }
        return remainder;
    }

    /**
     * Helper method to calculate the remaining value of the usedVouchers in this order
     *
     * @param order        Order containing the vouchers
     * @param usedVouchers List of usedVouchers
     */
    private void recalculateVoucherValues(Order order, List<UsedVoucher> usedVouchers) {
        Iterator<UsedVoucher> usedVoucherIterator = usedVouchers.iterator();
        UsedVoucher nextUsedVoucher;
        MonetaryAmount available;
        // Use a copy of order total
        MonetaryAmount dueSum = Money.of(order.getTotal().getNumber().doubleValue(), order.getTotal().getCurrency());
        while (usedVoucherIterator.hasNext()) {
            nextUsedVoucher = usedVoucherIterator.next();
            if (dueSum.isNegative()) {
                available = nextUsedVoucher.getAvailableValue();
                if (dueSum.add(available).isPositiveOrZero()) {
                    // This voucher is partly needed to pay the bill
                    nextUsedVoucher.renewAvailableValue(dueSum.negate());
                    dueSum = Money.of(0, EURO);
                } else {
                    // This voucher is not needed to pay the bill
                    dueSum = dueSum.add(available);
                    nextUsedVoucher.renewAvailableValue(available);
                }
            } else {
                // This voucher is needed to pay the bill
                nextUsedVoucher.renewAvailableValue(Money.of(0, EURO));
            }
        }
    }

    /**
     * Helper method to retrieve usedVouchers from cart
     *
     * @param cart Cart containing the Vouchers
     * @return List of usedVouchers
     */
    private List<UsedVoucher> getUsedVouchers(@ModelAttribute Cart cart) {
        List<UsedVoucher> usedVouchers = new LinkedList<>();
        // Find all used vouchers
        cart.get().filter(cartItem -> cartItem.getProduct() instanceof UsedVoucher)
                .forEach(cartItem -> usedVouchers.add((UsedVoucher) cartItem.getProduct()));
        return usedVouchers;
    }

    /**
     * Helper method used to create soldVouchers from a given cart
     *
     * @param cart Cart containing the Vouchers
     * @return created SoldVouchers in a List
     */
    private List<SoldVoucher> createSoldVouchersFromCart(@ModelAttribute Cart cart) {
        // Temporary data structure
        List<SoldVoucher> soldVouchersInOrder = new LinkedList<>();
        // Filter all vouchers
        cart.get().filter(cartItem -> cartItem.getProduct() instanceof Voucher)
                // Create a new SoldVoucher for each bought one and make it persistent and reference it in our
                // temporary structure
                .forEach(cartItem -> {
                    int count = 0;
                    SoldVoucher newVoucher;
                    while (cartItem.getQuantity().isGreaterThan(Quantity.of(count))) {
                        newVoucher = new SoldVoucher((cartItem.getProduct()).getPrice());
                        voucherInventory.save(newVoucher);
                        soldVouchersInOrder.add(newVoucher);
                        ++count;
                    }
                });
        return soldVouchersInOrder;
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('BOSS')")
    String orders(Model model) {

        model.addAttribute("ordersCompleted", orderManager.findBy(OrderStatus.COMPLETED));
        // Clean up first
        // Converting to List such that check for size is possible
        List<SoldVoucher> validVouchers = new LinkedList<>();
        voucherInventory.findAll().forEach(soldVoucher -> {
            if (soldVoucher.getValue().isPositive()) {
                validVouchers.add(soldVoucher);
            }
        });
        model.addAttribute("validVouchers", validVouchers);
        return "orders";
    }
}
