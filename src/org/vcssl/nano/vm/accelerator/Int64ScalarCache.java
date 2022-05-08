/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.connect.Int64ScalarDataAccessorInterface1;

public class Int64ScalarCache extends ScalarCache implements Int64ScalarDataAccessorInterface1 {
	public Int64ScalarCache(){}
	public long data;

	@Override
	public Int64ScalarCache clone() {
		Int64ScalarCache clonedInstance = new Int64ScalarCache();
		clonedInstance.data = this.data;
		return clonedInstance;
	}

	@Override
	public void setInt64ScalarData(long data) {
		this.data = data;
	}

	@Override
	public long getInt64ScalarData() {
		return this.data;
	}

	@Override
	public boolean hasInt64ScalarData() {
		return true;
	}
}
