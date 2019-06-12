/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

public class OptionKey {

	public static final String LOCALE = "LOCALE";

	public static final String EVAL_SCRIPT_NAME = "EVAL_SCRIPT_NAME";
	public static final String EVAL_NUMBER_AS_FLOAT = "EVAL_NUMBER_AS_FLOAT";

	public static final String LIBRARY_SCRIPTS = "LIBRARY_SCRIPTS";
	public static final String LIBRARY_SCRIPT_NAMES = "LIBRARY_SCRIPT_NAMES";

	public static final String ACCELERATOR_ENABLED = "ACCELERATOR_ENABLED";

	public static final String DUMPER_ENABLED = "DUMPER_ENABLED";
	public static final String DUMPER_TARGET = "DUMPER_TARGET";
	public static final String DUMPER_STREAM = "DUMPER_OUTPUT_STREAM";

	public static final String RUNNING_ENABLED = "RUNNING_ENABLED";


	// 以下は将来的に追加するオプション項目の暫定案（未サポート）

	public static final String EVAL_CACHE_ENABLED = "EVAL_CACHE_ENABLED";
	public static final String LOOP_ENABLED = "LOOP_ENABLED";
	public static final String BRANCH_ENABLED = "BRANCH_ENABLED";
	public static final String INTERNAL_FUNCTION_ENABLED = "INTERNAL_FUNCTION_ENABLED";
	public static final String INTERNAL_SCALAR_VARIABLE_ENABLED = "INTERNAL_SCALAR_VARIABLE_ENABLED";
	public static final String INTERNAL_ARRAY_VARIABLE_ENABLED = "INTERNAL_ARRAY_VARIABLE_ENABLED";
	public static final String VECTOR_OPERATION_ENABLED = "VECTOR_OPERATION_ENABLED";
}
