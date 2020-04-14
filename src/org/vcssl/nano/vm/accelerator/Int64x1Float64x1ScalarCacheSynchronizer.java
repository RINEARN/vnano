/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Int64x1Float64x1ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<long[]> container0;
	private final DataContainer<double[]> container1;
	private final Int64ScalarCache cache0;
	private final Float64ScalarCache cache1;
	private final boolean cacheSyncEnabled0;
	private final boolean cacheSyncEnabled1;

	@SuppressWarnings("unchecked" )
	public Int64x1Float64x1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		container0 = (DataContainer<long[]>)containers[0];
		container1 = (DataContainer<double[]>)containers[1];
		cache0 = (Int64ScalarCache)caches[0];
		cache1 = (Float64ScalarCache)caches[1];
		cacheSyncEnabled0 = cacheSyncEnabled[0];
		cacheSyncEnabled1 = cacheSyncEnabled[1];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled0) container0.getData()[ container0.getOffset() ] = cache0.value;
		if (cacheSyncEnabled1) container1.getData()[ container1.getOffset() ] = cache1.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled0) cache0.value = container0.getData()[ container0.getOffset() ];
		if (cacheSyncEnabled1) cache1.value = container1.getData()[ container1.getOffset() ];
	}
}
