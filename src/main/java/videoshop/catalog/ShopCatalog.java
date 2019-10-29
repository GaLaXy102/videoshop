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

import org.salespointframework.catalog.Catalog;
import org.springframework.data.domain.Sort;
import videoshop.catalog.Buyable.BuyableType;

/**
 * An extension of {@link Catalog} to add video shop specific query methods.
 *
 * @author Oliver Gierke
 */
public interface ShopCatalog extends Catalog<Buyable> {

	static final Sort DEFAULT_SORT = Sort.by("productIdentifier").descending();

	/**
	 * Returns all {@link Buyable}s by type ordered by the given {@link Sort}.
	 *
	 * @param type must not be {@literal null}.
	 * @param sort must not be {@literal null}.
	 * @return the discs of the given type, never {@literal null}.
	 */
	Iterable<Buyable> findByType(BuyableType type, Sort sort);

	/**
	 * Returns all {@link Disc}s by type ordered by their identifier.
	 *
	 * @param type must not be {@literal null}.
	 * @return the discs of the given type, never {@literal null}.
	 */
	default Iterable<Buyable> findByType(BuyableType type) {
		return findByType(type, DEFAULT_SORT);
	}
}