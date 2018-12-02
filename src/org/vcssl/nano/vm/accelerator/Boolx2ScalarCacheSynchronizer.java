/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.vm.memory.DataContainer;

class Boolx2ScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<boolean[]> container0;
	private final DataContainer<boolean[]> container1;
	private final BoolScalarCache cache0;
	private final BoolScalarCache cache1;
	private final boolean cached0;
	private final boolean cached1;

	@SuppressWarnings("unchecked" )
	public Boolx2ScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cached) {
		container0 = (DataContainer<boolean[]>)containers[0];
		container1 = (DataContainer<boolean[]>)containers[1];
		cache0 = (BoolScalarCache)caches[0];
		cache1 = (BoolScalarCache)caches[1];
		cached0 = cached[0];
		cached1 = cached[1];
	}

	public final void readCache() {
		if (cached0) container0.getData()[ container0.getOffset() ] = cache0.value;
		if (cached1) container1.getData()[ container1.getOffset() ] = cache1.value;
	}

	public final void writeCache() {
		if (cached0) cache0.value = container0.getData()[ container0.getOffset() ];
		if (cached1) cache1.value = container1.getData()[ container1.getOffset() ];
	}
}
