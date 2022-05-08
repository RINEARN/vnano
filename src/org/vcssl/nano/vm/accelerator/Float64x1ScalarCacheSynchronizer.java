/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Float64x1ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<double[]> container;
	private final Float64ScalarCache cache;
	private final boolean cacheSyncEnabled;

	public Float64x1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		this(containers, caches, cacheSyncEnabled, 0);
	}

	@SuppressWarnings("unchecked" )
	public Float64x1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled, int operandIndex) {

		this.container = (DataContainer<double[]>)containers[operandIndex];
		this.cache = (Float64ScalarCache)caches[operandIndex];
		this.cacheSyncEnabled = cacheSyncEnabled[operandIndex];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled) container.getArrayData()[ container.getArrayOffset() ] = cache.data;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled) cache.data = container.getArrayData()[ container.getArrayOffset() ];
	}
}
