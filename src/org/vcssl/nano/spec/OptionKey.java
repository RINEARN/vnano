/*
 * Copyright(C) 2019-2023 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The class defining keys of the option map (option names).
 */
public class OptionKey {

	/**
	 * The locale to switch the language of error messages.
	 *
	 * The value of this option is "Locale" type.
	 */
	public static final String LOCALE = "LOCALE";


	/**
	 * The default/specified name of the execution target script.
	 *
	 * The value of this option is "String" type.
	 */
	public static final String MAIN_SCRIPT_NAME = "MAIN_SCRIPT_NAME";


	/**
	 * The path of the directory in which the execution target script is locating.
	 *
	 * The value of this option is "String" type.
	 */
	public static final String MAIN_SCRIPT_DIRECTORY = "MAIN_SCRIPT_DIRECTORY";


	/**
	 * An option to regard integer literals as float type
	 * in the execution/evaluation target expressions and scripts (excepting library scripts).
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.
	 */
	public static final String EVAL_INT_LITERAL_AS_FLOAT = "EVAL_INT_LITERAL_AS_FLOAT";


	/**
	 * An option to restrict types of available statements
	 * in the execution target scripts (excepting library scripts) to only "expression".
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.
	 */
	public static final String EVAL_ONLY_EXPRESSION = "EVAL_ONLY_EXPRESSION";


	/**
	 * An option to restrict available data types of operators/operands in
	 * in the execution target scripts (excepting library scripts) to only "float".
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.
	 */
	public static final String EVAL_ONLY_FLOAT = "EVAL_ONLY_FLOAT";


	/**
	 * An option to enable/disable {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator},
	 * which is the high-speed virtual processor implementation in the VM.
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.FALSE" to disable this option.
	 */
	public static final String ACCELERATOR_ENABLED = "ACCELERATOR_ENABLED";


	/**
	 * An option to control the optimization level of processing in
	 * {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator},
	 * which is the high-speed virtual processor implementation in the VM.
	 *
	 * The value of this option is "Integer" type.
	 * For details about the value, see the document of {@link org.vcssl.nano.spec.OptionValue OptionValue}.
	 */
	public static final String ACCELERATOR_OPTIMIZATION_LEVEL = "ACCELERATOR_OPTIMIZATION_LEVEL";


	/**
	 * An option to enable/disable the feature for terminating a running script.
	 *
	 * If you enable this option, you become to able to terminate a running script BY OPERATION OF THE SCRIPT ENGINE,
	 * but the maximum numerical operating speed (and so on) may decreases slightly.
	 * Probably, for most cases, users hardly can recognize the decreasing of the operating speed caused by this option.
	 * However, for highly optimized numerical computation scripts, the operating speed may decrease about 10% or more.
	 * Note that, the script will be terminated when all procedures in the script completed,
	 * or when any errors occurred in the script, or when exit() function is called in the script,
	 * regardless whether this option is enabled or disabled.
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.
	 */
	public static final String TERMINATOR_ENABLED = "TERMINATOR_ENABLED";


	/**
	 * An option to enable/disable the performance monitor.
	 *
	 * If you enable this option, you become to get performance monitoring values of the engine,
	 * but the maximum numerical operating speed (and so on) may decreases to some extent.
	 * Probably, for most cases, decreasing of the operating speed caused by this option is not so heavy.
	 * However, for highly optimized numerical computation scripts, the operating speed may decrease about 25% or more.
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.
	 */
	public static final String PERFORMANCE_MONITOR_ENABLED = "PERFORMANCE_MONITOR_ENABLED";


	/**
	 * An option to dump states and intermediate representations in the compiler, VM, etc.
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.
	 */
	public static final String DUMPER_ENABLED = "DUMPER_ENABLED";


	/**
	 * Specify the target of to dump.
	 *
	 * The value of this option is "String" type, defined in {@link OptionValue}.
	 */
	public static final String DUMPER_TARGET = "DUMPER_TARGET";


	/**
	 * Specify the stream to output dumped contents.
	 *
	 * The value of this option is "PrintStream" type.
	 */
	public static final String DUMPER_STREAM = "DUMPER_OUTPUT_STREAM";


	/**
	 * An option to switch whether execute script.
	 * This option might be useful when you want to dump the compiled result for debugging but don't want to run it.
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.FALSE" to disable this option.
	 */
	public static final String RUNNING_ENABLED = "RUNNING_ENABLED";


	/**
	 * An option to switch whether activate/deactivate the Vnano engine automatically before/after executing a script
	 * ("automatic activation" feature).
	 *
	 * This option is enabled by default so that users can execute scripts any time.
	 * However, activations/deactivations of the engine entail some overhead costs.
	 * Especially when the engine repetitively executes scripts in high frequency,
	 * this "activation costs" may result serious degradation of processing speed, if this option is enabled.
	 * In such case, disable this option, and activate/deactivate the engine manually at suitable timing
	 * (typically before/after a set of repetitive executions).
	 *
	 * The value of this option is "Boolean" type. Specify "Boolean.FALSE" to disable this option.
	 */
	public static final String AUTOMATIC_ACTIVATION_ENABLED = "AUTOMATIC_ACTIVATION_ENABLED";


	/**
	 * Specify the mode of UI for inputting/outputting values and so on.
	 *
	 * The value of this option is "String" type. Specify "GUI" or "CUI".
	 * The default value is "GUI", but it will be set to "CUI" automatically when you execute the Vnano engine in the command-line mode.
	 *
	 * This option is referred by I/O plug-ins if they are connected.
	 */
	public static final String UI_MODE = "UI_MODE";


	/**
	 * Specify the default line-feed code on the environment.
	 *
	 * The value of this option is "String" type.
	 *
	 * This option is referred by plug-ins providing environment-dependent values, if they are connected.
	 */
	public static final String ENVIRONMENT_EOL = "ENVIRONMENT_EOL";


	/**
	 * Specify the default line-feed code for file I/O.
	 *
	 * The value of this option is "String" type.
	 *
	 * This option is referred by I/O plug-ins if they are connected.
	 */
	public static final String FILE_IO_EOL = "FILE_IO_EOL";


	/**
	 * Specify the default line-feed code for terminal I/O.
	 *
	 * The value of this option is "String" type.
	 *
	 * This option is referred by I/O plug-ins if they are connected.
	 */
	public static final String TERMINAL_IO_EOL = "TERMINAL_IO_EOL";


	/**
	 * Specify the name of the default encoding for writing to / reading files in scripts.
	 *
	 * The value of this option is "String" type.
	 *
	 * This option is referred by I/O plug-ins if they are connected.
	 */
	public static final String FILE_IO_ENCODING = "FILE_IO_ENCODING";


	/**
	 * Specify the stream for standard input used when {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} is set to "CUI".
	 *
	 * The value of this option is "InputStream" type.
	 *
	 * This option is referred by I/O plug-ins if they are connected.
	 */
	public static final String STDIN_STREAM = "STDIN_STREAM";


	/**
	 * Specify the stream for standard output used when {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} is set to "CUI".
	 *
	 * The value of this option is "PrintStream" type.
	 *
	 * This option is referred by I/O plug-ins if they are connected.
	 */
	public static final String STDOUT_STREAM = "STDOUT_STREAM";


	/**
	 * Specify the stream for standard error output when {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} is set to "CUI".
	 *
	 * The value of this option is "PrintStream" type.
	 *
	 * This option is referred by I/O plug-ins if they are connected.
	 */
	public static final String STDERR_STREAM = "STDERR_STREAM";
}

