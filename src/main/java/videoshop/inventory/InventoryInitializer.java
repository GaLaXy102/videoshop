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

import org.salespointframework.core.DataInitializer;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import videoshop.catalog.Buyable;
import videoshop.catalog.ShopCatalog;

/**
 * A {@link DataInitializer} implementation that will create dummy data for the application on application startup.
 *
 * @author Paul Henke
 * @author Oliver Gierke
 * @see DataInitializer
 */
@Component
@Order(20)
class InventoryInitializer implements DataInitializer {

	private final UniqueInventory<UniqueInventoryItem> inventory;
	private final ShopCatalog shopCatalog;

	InventoryInitializer(UniqueInventory<UniqueInventoryItem> inventory, ShopCatalog shopCatalog) {

		Assert.notNull(inventory, "Inventory must not be null!");
		Assert.notNull(shopCatalog, "ShopCatalog must not be null!");

		this.inventory = inventory;
		this.shopCatalog = shopCatalog;
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.core.DataInitializer#initialize()
	 */
	@Override
	public void initialize() {

		// (｡◕‿◕｡)
		// Über alle Voucher iterieren und jeweils ein InventoryItem mit der Quantity 100000 setzen
		// Das heißt: Von jedem Voucher sind 100000 Stück im Inventar.

		shopCatalog.findByType(Buyable.BuyableType.VOUCHER).forEach(voucher -> {
			inventory.findByProduct(voucher)
					.orElseGet(() -> inventory.save(new UniqueInventoryItem(voucher, Quantity.of(100000))));
		});

		// (｡◕‿◕｡)
		// Über alle Discs iterieren und jeweils ein InventoryItem mit der Quantity 10 setzen
		// Das heißt: Von jeder Disc sind 10 Stück im Inventar.
		shopCatalog.findAll().forEach(disc -> {

			// Try to find an InventoryItem for the project and create a default one with 10 items if none available
			inventory.findByProduct(disc) //
					.orElseGet(() -> inventory.save(new UniqueInventoryItem(disc, Quantity.of(10))));
		});
	}
}
