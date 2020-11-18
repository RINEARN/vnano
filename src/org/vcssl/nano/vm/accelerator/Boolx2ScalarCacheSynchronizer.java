/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

public class Boolx2ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<boolean[]> container0;
	private final DataContainer<boolean[]> container1;
	private final BoolScalarCache cache0;
	private final BoolScalarCache cache1;
	private final boolean cacheSyncEnabled0;
	private final boolean cacheSyncEnabled1;

	@SuppressWarnings("unchecked" )
	public Boolx2ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches,
			boolean[] cacheSyncEnabled) {

		container0 = (DataContainer<boolean[]>)containers[0];
		container1 = (DataContainer<boolean[]>)containers[1];
		cache0 = (BoolScalarCache)caches[0];
		cache1 = (BoolScalarCache)caches[1];
		cacheSyncEnabled0 = cacheSyncEnabled[0];
		cacheSyncEnabled1 = cacheSyncEnabled[1];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cacheSyncEnabled0) container0.getArrayData()[ container0.getArrayOffset() ] = cache0.value;
		if (cacheSyncEnabled1) container1.getArrayData()[ container1.getArrayOffset() ] = cache1.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cacheSyncEnabled0) cache0.value = container0.getArrayData()[ container0.getArrayOffset() ];
		if (cacheSyncEnabled1) cache1.value = container1.getArrayData()[ container1.getArrayOffset() ];
	}
}
