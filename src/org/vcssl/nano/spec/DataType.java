/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The enum which defines the data types available in scripts.
 * 
 * Nemes of data types are defined in {@link DataTypeName DataTypeName} class,
 * and conversion methods between elements of this enum and names of data types
 * are provided by the class.
 */
public enum DataType {

	/** The 64-bit signed integer type (name in scripts: "int"). */
	INT64,

	/** The 64-bit floating-point number type (name: "float"). */
	FLOAT64,

	/** The boolean type (name: "bool"). */
	BOOL,

	/** The character string type (name: "string"). */
	STRING,

	/** The special type to represent that any type is available for the argument, the return value, and so on (name: "any"). */
	ANY,

	/** The void type (name: "void"). */
	VOID;
}
