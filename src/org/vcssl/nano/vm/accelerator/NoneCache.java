package org.vcssl.nano.vm.accelerator;

/**
 * 読み書きしないプレースホルダとしてのオペランドに対応するキャッシュです。
 * 実体のあるオペランドとのキャッシュ管理を統一化するためのもので、
 * 特に何もしない実装となっています。
 */
public class NoneCache extends ScalarCache {
	@Override
	public NoneCache clone() {
		return new NoneCache();
	}
}
