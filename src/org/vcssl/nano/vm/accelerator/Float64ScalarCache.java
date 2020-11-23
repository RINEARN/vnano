/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.connect.Float64ScalarDataAccessorInterface1;

public class Float64ScalarCache implements ScalarCache, Float64ScalarDataAccessorInterface1 {
	public Float64ScalarCache(){}
	public double data;

	@Override
	public void setFloat64ScalarData(double data) {
		this.data = data;
	}

	@Override
	public double getFloat64ScalarData() {
		return this.data;
	}
}
