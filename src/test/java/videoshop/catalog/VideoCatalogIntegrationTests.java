/*
 * Copyright 2013-2017 the original author or authors.
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
package videoshop.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import videoshop.AbstractIntegrationTests;
import videoshop.catalog.Buyable.BuyableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ShopCatalog}.
 * 
 * @author Oliver Gierke
 * @author Andreas Zaschka
 */
class ShopCatalogIntegrationTests extends AbstractIntegrationTests {

	@Autowired
    ShopCatalog catalog;

	@Test
	void findsAllBluRays() {

		Iterable<Buyable> result = catalog.findByType(BuyableType.BLURAY);
		assertThat(result).hasSize(9);
	}

	/**
	 * @see #50
	 */
	@Test
	void discsDontHaveAnyCategoriesAssigned() {

		for (Buyable buyable : catalog.findByType(BuyableType.BLURAY)) {
			assertThat(((Disc) buyable).getCategories()).isEmpty();
		}
	}
}
