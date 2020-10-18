/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Int64x4ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<long[]> container0;
	private final DataContainer<long[]> container1;
	private final DataContainer<long[]> container2;
	private final DataContainer<long[]> container3;
	private final Int64ScalarCache cache0;
	private final Int64ScalarCache cache1;
	private final Int64ScalarCache cache2;
	private final Int64ScalarCache cache3;
	private final boolean cacheSyncEnabled0;
	private final boolean cacheSyncEnabled1;
	private final boolean cacheSyncEnabled2;
	private final boolean cacheSyncEnabled3;

	@SuppressWarnings("unchecked" )
	public Int64x4ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		container0 = (DataContainer<long[]>)containers[0];
		container1 = (DataContainer<long[]>)containers[1];
		container2 = (DataContainer<long[]>)containers[2];
		container3 = (DataContainer<long[]>)containers[3];
		cache0 = (Int64ScalarCache)caches[0];
		cache1 = (Int64ScalarCache)caches[1];
		cache2 = (Int64ScalarCache)caches[2];
		cache3 = (Int64ScalarCache)caches[3];
		cacheSyncEnabled0 = cacheSyncEnabled[0];
		cacheSyncEnabled1 = cacheSyncEnabled[1];
		cacheSyncEnabled2 = cacheSyncEnabled[2];
		cacheSyncEnabled3 = cacheSyncEnabled[3];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled0) container0.getData()[ container0.getOffset() ] = cache0.value;
		if (cacheSyncEnabled1) container1.getData()[ container1.getOffset() ] = cache1.value;
		if (cacheSyncEnabled2) container2.getData()[ container2.getOffset() ] = cache2.value;
		if (cacheSyncEnabled3) container3.getData()[ container3.getOffset() ] = cache3.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled0) cache0.value = container0.getData()[ container0.getOffset() ];
		if (cacheSyncEnabled1) cache1.value = container1.getData()[ container1.getOffset() ];
		if (cacheSyncEnabled2) cache2.value = container2.getData()[ container2.getOffset() ];
		if (cacheSyncEnabled3) cache3.value = container3.getData()[ container3.getOffset() ];
	}
}
