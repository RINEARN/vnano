/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Int64x1ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<long[]> container;
	private final Int64ScalarCache cache;
	private final boolean cacheSyncEnabled;

	public Int64x1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		this(containers, caches, cacheSyncEnabled, 0);
	}

	@SuppressWarnings("unchecked" )
	public Int64x1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled, int operandIndex) {

		this.container = (DataContainer<long[]>)containers[operandIndex];
		this.cache = (Int64ScalarCache)caches[operandIndex];
		this.cacheSyncEnabled = cacheSyncEnabled[operandIndex];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled) container.getArrayData()[ container.getArrayOffset() ] = cache.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled) cache.value = container.getArrayData()[ container.getArrayOffset() ];
	}
}
