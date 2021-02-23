/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.connect.BoolScalarDataAccessorInterface1;

public class BoolScalarCache extends ScalarCache implements BoolScalarDataAccessorInterface1 {
	public BoolScalarCache(){}
	public boolean data;

	@Override
	public BoolScalarCache clone() {
		BoolScalarCache clonedInstance = new BoolScalarCache();
		clonedInstance.data = this.data;
		return clonedInstance;
	}

	@Override
	public void setBoolScalarData(boolean data) {
		this.data = data;
	}

	@Override
	public boolean getBoolScalarData() {
		return this.data;
	}

	@Override
	public boolean hasBoolScalarData() {
		return true;
	}
}
