/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

class Boolx1ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<boolean[]> container0;
	private final BoolScalarCache cache0;
	private final boolean cached0;

	@SuppressWarnings("unchecked" )
	public Boolx1ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cached) {
		container0 = (DataContainer<boolean[]>)containers[0];
		cache0 = (BoolScalarCache)caches[0];
		cached0 = cached[0];
	}

	public final void synchronizeFromCacheToMemory() {
		if (cached0) container0.getData()[ container0.getOffset() ] = cache0.value;
	}

	public final void synchronizeFromMemoryToCache() {
		if (cached0) cache0.value = container0.getData()[ container0.getOffset() ];
	}
}
