/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

public abstract class CacheSynchronizer {
	public abstract void synchronizeFromCacheToMemory();
	public abstract void synchronizeFromMemoryToCache();
}
