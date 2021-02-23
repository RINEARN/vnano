/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.connect.Float64ScalarDataAccessorInterface1;

public class Float64ScalarCache extends ScalarCache implements Float64ScalarDataAccessorInterface1 {
	public Float64ScalarCache(){}
	public double data;

	@Override
	public Float64ScalarCache clone() {
		Float64ScalarCache clonedInstance = new Float64ScalarCache();
		clonedInstance.data = this.data;
		return clonedInstance;
	}

	@Override
	public void setFloat64ScalarData(double data) {
		this.data = data;
	}

	@Override
	public double getFloat64ScalarData() {
		return this.data;
	}

	@Override
	public boolean hasFloat64ScalarData() {
		return true;
	}
}
