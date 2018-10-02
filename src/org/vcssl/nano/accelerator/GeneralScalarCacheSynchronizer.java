/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoRuntimeException;
import org.vcssl.nano.memory.DataContainer;

class GeneralScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<?>[] containers;
	private final Object[] caches;
	private final boolean[] cached;

	public GeneralScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cached) {
		this.containers = containers;
		this.caches = caches;
		this.cached = cached;
	}

	public final void readCache() {
		int n = containers.length;
		for (int i=0; i<n; i++) {
			if (!this.cached[i]) {
				continue;
			}

			// 将来的に containers[i].getDataType() に変更？
			if (caches[i] instanceof Int64ScalarCache) {
				((long[])(containers[i].getData()))[ containers[i].getOffset() ] = ((Int64ScalarCache)caches[i]).value;

			} else if (caches[i] instanceof Float64ScalarCache) {
				((double[])(containers[i].getData()))[ containers[i].getOffset() ] = ((Float64ScalarCache)caches[i]).value;

			} else if (caches[i] instanceof BoolScalarCache) {
				((boolean[])(containers[i].getData()))[ containers[i].getOffset() ] = ((BoolScalarCache)caches[i]).value;
			} else {
				// 不明なキャッシュ
				throw new VnanoRuntimeException();
			}
		}
	}

	public final void writeCache() {
		int n = containers.length;
		for (int i=0; i<n; i++) {
			if (!this.cached[i]) {
				continue;
			}

			// 将来的に containers[i].getDataType() に変更？
			if (caches[i] instanceof Int64ScalarCache) {
				((Int64ScalarCache)caches[i]).value = ((long[])(containers[i].getData()))[ containers[i].getOffset() ];

			} else if (caches[i] instanceof Float64ScalarCache) {
				((Float64ScalarCache)caches[i]).value = ((double[])(containers[i].getData()))[ containers[i].getOffset() ];

			} else if (caches[i] instanceof BoolScalarCache) {
				((BoolScalarCache)caches[i]).value = ((boolean[])(containers[i].getData()))[ containers[i].getOffset() ];
			}
		}
	}
}
