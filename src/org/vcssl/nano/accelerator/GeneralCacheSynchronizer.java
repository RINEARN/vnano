/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoRuntimeException;
import org.vcssl.nano.memory.DataContainer;

class GeneralCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<?>[] containers;
	private final Object[] caches;
	private final boolean[] cached;

	public GeneralCacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cached) {
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
			if (caches[i] instanceof Int64Cache) {
				((long[])(containers[i].getData()))[ containers[i].getOffset() ] = ((Int64Cache)caches[i]).value;

			} else if (caches[i] instanceof Float64Cache) {
				((double[])(containers[i].getData()))[ containers[i].getOffset() ] = ((Float64Cache)caches[i]).value;

			} else if (caches[i] instanceof BoolCache) {
				((boolean[])(containers[i].getData()))[ containers[i].getOffset() ] = ((BoolCache)caches[i]).value;
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
			if (caches[i] instanceof Int64Cache) {
				((Int64Cache)caches[i]).value = ((long[])(containers[i].getData()))[ containers[i].getOffset() ];

			} else if (caches[i] instanceof Float64Cache) {
				((Float64Cache)caches[i]).value = ((double[])(containers[i].getData()))[ containers[i].getOffset() ];

			} else if (caches[i] instanceof BoolCache) {
				((BoolCache)caches[i]).value = ((boolean[])(containers[i].getData()))[ containers[i].getOffset() ];
			}
		}
	}
}
