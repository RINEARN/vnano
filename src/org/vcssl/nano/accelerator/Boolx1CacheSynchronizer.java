/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.memory.DataContainer;

class Boolx1CacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<boolean[]> container0;
	private final BoolCache cache0;
	private final boolean cached0;

	@SuppressWarnings("unchecked" )
	public Boolx1CacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cached) {
		container0 = (DataContainer<boolean[]>)containers[0];
		cache0 = (BoolCache)caches[0];
		cached0 = cached[0];
	}

	public final void readCache() {
		if (cached0) container0.getData()[ container0.getOffset() ] = cache0.value;
	}

	public final void writeCache() {
		if (cached0) cache0.value = container0.getData()[ container0.getOffset() ];
	}
}
