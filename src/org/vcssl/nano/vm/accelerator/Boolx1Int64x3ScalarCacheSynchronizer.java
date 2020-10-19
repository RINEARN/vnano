/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Boolx1Int64x3ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<boolean[]> container0;
	private final DataContainer<long[]> container1;
	private final DataContainer<long[]> container2;
	private final BoolScalarCache cache0;
	private final Int64ScalarCache cache1;
	private final Int64ScalarCache cache2;
	private final boolean cacheSyncEnabled0;
	private final boolean cacheSyncEnabled1;
	private final boolean cacheSyncEnabled2;

	@SuppressWarnings("unchecked" )
	public Boolx1Int64x3ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		container0 = (DataContainer<boolean[]>)containers[0];
		container1 = (DataContainer<long[]>)containers[1];
		container2 = (DataContainer<long[]>)containers[2];
		cache0 = (BoolScalarCache)caches[0];
		cache1 = (Int64ScalarCache)caches[1];
		cache2 = (Int64ScalarCache)caches[2];
		cacheSyncEnabled0 = cacheSyncEnabled[0];
		cacheSyncEnabled1 = cacheSyncEnabled[1];
		cacheSyncEnabled2 = cacheSyncEnabled[2];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled0) container0.getData()[ container0.getOffset() ] = cache0.value;
		if (cacheSyncEnabled1) container1.getData()[ container1.getOffset() ] = cache1.value;
		if (cacheSyncEnabled2) container2.getData()[ container2.getOffset() ] = cache2.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled0) cache0.value = container0.getData()[ container0.getOffset() ];
		if (cacheSyncEnabled1) cache1.value = container1.getData()[ container1.getOffset() ];
		if (cacheSyncEnabled2) cache2.value = container2.getData()[ container2.getOffset() ];
	}
}