/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.connect.Int64ScalarDataAccessorInterface1;

public class Int64ScalarCache implements ScalarCache, Int64ScalarDataAccessorInterface1 {
	public Int64ScalarCache(){}
	public long data;

	@Override
	public void setInt64ScalarData(long data) {
		this.data = data;
	}

	@Override
	public long getInt64ScalarData() {
		return this.data;
	}
}
