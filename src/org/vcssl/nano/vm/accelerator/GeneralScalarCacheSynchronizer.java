/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class GeneralScalarCacheSynchronizer extends CacheSynchronizer {
	private final DataContainer<?>[] containers;
	private final Object[] caches;
	private final boolean[] cacheSyncEnabled;

	public GeneralScalarCacheSynchronizer(DataContainer<?>[] containers, Object[] caches, boolean[] cacheSyncEnabled) {
		this.containers = containers;
		this.caches = caches;
		this.cacheSyncEnabled = cacheSyncEnabled;
	}


	public final void synchronizeFromCacheToMemory() {
		int n = containers.length;
		for (int i=0; i<n; i++) {
			if (!this.cacheSyncEnabled[i]) {
				continue;
			}

			// 将来的に containers[i].getDataType() に変更？
			if (caches[i] instanceof Int64ScalarCache) {
				((long[])(containers[i].getData()))[ containers[i].getOffset() ] = ((Int64ScalarCache)caches[i]).value;

			} else if (caches[i] instanceof Float64ScalarCache) {
				((double[])(containers[i].getData()))[ containers[i].getOffset() ] = ((Float64ScalarCache)caches[i]).value;

			} else if (caches[i] instanceof BoolScalarCache) {
				((boolean[])(containers[i].getData()))[ containers[i].getOffset() ] = ((BoolScalarCache)caches[i]).value;

			} else if (caches[i] instanceof NoneCache) {
				// プレースホルダの空オペランドなので、何もしない

			} else {
				// 不明なキャッシュ
				throw new VnanoFatalException("Unknown Cache Type: " + caches[1]);
			}
		}
	}

	public final void synchronizeFromMemoryToCache() {
		int n = containers.length;
		for (int i=0; i<n; i++) {
			if (!this.cacheSyncEnabled[i]) {
				continue;
			}

			// 将来的に containers[i].getDataType() に変更？
			if (caches[i] instanceof Int64ScalarCache) {
				((Int64ScalarCache)caches[i]).value = ((long[])(containers[i].getData()))[ containers[i].getOffset() ];

			} else if (caches[i] instanceof Float64ScalarCache) {
				((Float64ScalarCache)caches[i]).value = ((double[])(containers[i].getData()))[ containers[i].getOffset() ];

			} else if (caches[i] instanceof BoolScalarCache) {
				((BoolScalarCache)caches[i]).value = ((boolean[])(containers[i].getData()))[ containers[i].getOffset() ];

			} else if (caches[i] instanceof NoneCache) {
				// プレースホルダの空オペランドなので、何もしない

			} else {
				// 不明なキャッシュ
				throw new VnanoFatalException("Unknown Cache Type: " + caches[1]);
			}
		}
	}
}
