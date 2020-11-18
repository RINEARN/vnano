/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Int64x3ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<long[]> container0;
	private final DataContainer<long[]> container1;
	private final DataContainer<long[]> container2;
	private final Int64ScalarCache cache0;
	private final Int64ScalarCache cache1;
	private final Int64ScalarCache cache2;
	private final boolean cacheSyncEnabled0;
	private final boolean cacheSyncEnabled1;
	private final boolean cacheSyncEnabled2;

	@SuppressWarnings("unchecked" )
	public Int64x3ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		container0 = (DataContainer<long[]>)containers[0];
		container1 = (DataContainer<long[]>)containers[1];
		container2 = (DataContainer<long[]>)containers[2];
		cache0 = (Int64ScalarCache)caches[0];
		cache1 = (Int64ScalarCache)caches[1];
		cache2 = (Int64ScalarCache)caches[2];
		cacheSyncEnabled0 = cacheSyncEnabled[0];
		cacheSyncEnabled1 = cacheSyncEnabled[1];
		cacheSyncEnabled2 = cacheSyncEnabled[2];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled0) container0.getArrayData()[ container0.getArrayOffset() ] = cache0.value;
		if (cacheSyncEnabled1) container1.getArrayData()[ container1.getArrayOffset() ] = cache1.value;
		if (cacheSyncEnabled2) container2.getArrayData()[ container2.getArrayOffset() ] = cache2.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled0) cache0.value = container0.getArrayData()[ container0.getArrayOffset() ];
		if (cacheSyncEnabled1) cache1.value = container1.getArrayData()[ container1.getArrayOffset() ];
		if (cacheSyncEnabled2) cache2.value = container2.getArrayData()[ container2.getArrayOffset() ];
	}
}
