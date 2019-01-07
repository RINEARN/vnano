/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

class Boolx1ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<boolean[]> container;
	private final BoolScalarCache cache;
	private final boolean cached;

	public Boolx1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cached) {
		this(containers, caches, cached, 0);
	}

	@SuppressWarnings("unchecked" )
	public Boolx1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cached, int operandIndex) {
		this.container = (DataContainer<boolean[]>)containers[operandIndex];
		this.cache = (BoolScalarCache)caches[operandIndex];
		this.cached = cached[operandIndex];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cached) container.getData()[ container.getOffset() ] = cache.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cached) cache.value = container.getData()[ container.getOffset() ];
	}
}
