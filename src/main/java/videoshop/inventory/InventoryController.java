/*
 * Copyright 2013-2019 the original author or authors.
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
package videoshop.inventory;

import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import videoshop.order.UsedVoucher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// Straight forward?

@Controller
class InventoryController {

    private final UniqueInventory<UniqueInventoryItem> inventory;

    InventoryController(UniqueInventory<UniqueInventoryItem> inventory) {
        this.inventory = inventory;
    }

    /**
     * Displays all {@link InventoryItem}s in the system
     *
     * @param model will never be {@literal null}.
     * @return the view name.
     */
    @GetMapping("/stock")
    @PreAuthorize("hasRole('BOSS')")
    String stock(Model model) {
        Iterable<UniqueInventoryItem> allItems = inventory.findAll();
        List<UniqueInventoryItem> allItemsButUsedVouchers = StreamSupport.stream(allItems.spliterator(), false)
                .filter(uniqueInventoryItem -> !(uniqueInventoryItem.getProduct() instanceof UsedVoucher))
                .collect(Collectors.toList());

        model.addAttribute("stock", allItemsButUsedVouchers);

        return "stock";
    }
}
