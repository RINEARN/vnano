/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

abstract class CacheSynchronizer {
	public abstract void readCache();
	public abstract void writeCache();
}