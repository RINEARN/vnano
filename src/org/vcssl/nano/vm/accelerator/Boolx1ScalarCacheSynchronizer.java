/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Boolx1ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<boolean[]> container;
	private final BoolScalarCache cache;
	private final boolean cacheSyncEnabled;

	public Boolx1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		this(containers, caches, cacheSyncEnabled, 0);
	}

	@SuppressWarnings("unchecked" )
	public Boolx1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled, int operandIndex) {

		this.container = (DataContainer<boolean[]>)containers[operandIndex];
		this.cache = (BoolScalarCache)caches[operandIndex];
		this.cacheSyncEnabled = cacheSyncEnabled[operandIndex];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled) container.getData()[ container.getOffset() ] = cache.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled) cache.value = container.getData()[ container.getOffset() ];
	}
}
