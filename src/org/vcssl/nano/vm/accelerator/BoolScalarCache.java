/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.connect.BoolScalarDataAccessorInterface1;

public class BoolScalarCache implements ScalarCache, BoolScalarDataAccessorInterface1 {
	public BoolScalarCache(){}
	public boolean data;

	@Override
	public void setBoolScalarData(boolean data) {
		this.data = data;
	}

	@Override
	public boolean getBoolScalarData() {
		return this.data;
	}
}
