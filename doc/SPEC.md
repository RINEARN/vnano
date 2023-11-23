
# Specifications of Vnano Engine

## Index
( &raquo; [Japanese](SPEC_JAPANESE.md) )

* [Engine Methods](#methods)
* [Option Items](#options)
* [Permission Items](#permissions)

<hr />


<a id="methods"></a>
## Engine Methods

The following is the list of all methods of Vnano Engine (org.vcssl.nano.VnanoEngine class).

| Signature |Object executeScript(String script) |
|:---|:---|
| Description | Executes an expression or script code specified as an argument. |
| Parameters | script: An expression or script code to be executed |
| Return | The evaluated value of the expression, or the last expression statement in script code. If there is no evaluated value, returns null. |
| Exception | VnanoException will be thrown when any error has detected for the content or the processing of the script. |


| Signature | void terminateScript() |
|:---|:---|
| Description | <p>Terminates the currently running script as soon as possible.</p> <p>To be precise, the VirtualMachine (which is processing instructions compiled from the script) in the engine will be terminated after when the processing of a currently executed instruction has been completed, without processing remained instructions. Usually it ends in a moment, but sometimes it takes time. For example, it can't end while external function provided by a plug-in is being executed.</p> <p>Also, if you used this method, call "resetTerminator()" method before the next execution of a new script, otherwise the next execution will end immediately without processing any instructions (By this behavior, even when a termination-request and an execution-request from another thread are conflict, the execution will be terminated certainly).</p> |
| Parameters | None |
| Return | None |
| Exception | <p>VnanoFatalException (unchecked exception) will be thrown if this method is called in a state in which isTerminatorEnabled() method returns false.</p> <p>Note that, if any exceptions occurred on the finalization processes of the connected plug-ins, it will be throws by the currently running executeScript(String script) method, not by this method.</p> <p>This method throws the exception only when it failed requesting the termination.</p> |


| Signature |boolean isTerminatorEnabled() |
|:---|:---|
| Description | <p>Returns whether the "terminator" which is the feature to terminate scripts, is enabled.</p> <p>Internally, this method checks the value of "TERMINATOR_ENABLED" option (disabled by default) and returns it.</p> <p>If this method returns true, terminateScript() method and resetTerminator() method are available.</p> <p>Please note that, even when this method returns true, some errors may occur in the termination processes (for example, erros caused by failures of finalization processes of the connected plug-ins, and so on). For details, see the explanation about exceptions, in the description of terminateScript() method.</p> |
| Parameters | None |
| Return | Returns true if the terminator is enabled. |
| Exception | None |


| Signature |void resetTerminator() |
|:---|:---|
| Description | <p>Resets the engine which had terminated by terminateScript() method, for processing new scripts.</p> <p>Please note that, if an execution of code is requested by another thread when this method is being processed, the execution request might be missed.</p> |
| Parameters | None |
| Return | None |
| Exception | VnanoFatalException (unchecked exception) will be thrown if this method is called in a state in which isTerminatorEnabled() method returns false. |


| Signature |void activateEngine() |
|:---|:---|
| Description | <p>Activates this engine. By this activation, the engine become available for executing scripts.</p> <p>By default, the engine is activated automatically just before executing a script. However, when "AUTOMATIC_ACTIVATION_ENABLED" option is set to FALSE, the engine is NOT activated automatically. In such case, activate the engine manually at suitable timing (for details, see the description of "AUTOMATIC_ACTIVATION_ENABLED" option).</p> <p>In this activation step, initialization procedures of all connected plug-ins (implemented as initializeForExecution() method in each plug-in) are processed. Hence, required time of this method depends on the number of the connected plug-ins, and implementations of them.</p> |
| Parameters | None |
| Return | None |
| Exception | VnanoException will be thrown if any error has occurred in the initialization procedure of any plug-in. |


| Signature |void deactivateEngine() |
|:---|:---|
| Description | <p>Deactivates this engine. This deactivation canceles the state of the engine which is "activated" for executing scripts. It also performs some finalization procedures.</p> <p>By default, the engine is deactivated automatically just after executing a script. However, when "AUTOMATIC_ACTIVATION_ENABLED" option is set to FALSE, the engine is NOT deactivated automatically. In such case, deactivate the engine manually at suitable timing (for details, see the description of "AUTOMATIC_ACTIVATION_ENABLED" option).</p> <p>In this deactivation step, finalization procedures of all connected plug-ins (implemented as finalizeForTermination() method in each plug-in) are processed. Hence, required time of this method depends on the number of the connected plug-ins, and implementations of them.</p> |
| Parameters | None |
| Return | None |
| Exception | VnanoException will be thrown if any error has occurred in the finalization procedure of any plug-in. |


| Signature | void connectPlugin(String bindingName, Object plugin) |
|:---|:---|
| Description | Connects various types of plug-ins which provides external functions/variables and so on. |
| Parameters | <p>bindingName:  A name in scripts of the variable/function/namespace provided by the connected plug-in. If the passed argument contains a white space or a character "(", the content after it will be ignored. Also, you can specify "___VNANO_AUTO_KEY" for using a valid value generated automatically.</p><p>plugin: The plug-in providing external function/variable and so on. As the type of plug-ins, java.lang.reflect.Field and Method, java.lang.Class and Object, org.vcssl.connect.ExternalVariableConnectorInterface1,  ExternalFunctionConnectorInterface1, ExternalNamespaceConnectorInterface1, and PermissionAuthorizerConnectorInterface1 are available.</p> |
| Return | None |
| Exception | VnanoException will be thrown when it failed to connect/initialize the specified plug-in. |


| Signature | void disconnectAllPlugins() |
|:---|:---|
| Description | Disconnects all plug-ins. |
| Parameters | None |
| Return | None |
| Exception | VnanoException will be thrown when any error occurred in finalizations of plug-ins. |


| Signature | void includeLibraryScript(String libraryScriptName, String libraryScriptContent) |
|:---|:---|
| Description | Add a library script which will be "include"-ed at the head of a executed script. |
| Parameters | <p>libraryScriptName: Names of the library script (displayed in error messages)</p> <p>libraryScriptContent: Contents (code) of the library script</p> |
| Return | None |
| Exception | VnanoException will be thrown when incorrect something is detected for specified scripts. |


| Signature | void unincludeAllLibraryScripts() |
|:---|:---|
| Description | Uninclude all library scripts. |
| Parameters | None |
| Return | None |
| Exception | No Exceptions will not be thrown on the current implementation, but it requires to be "catch"-ed for keeping compatibility in future. |


| Signature | void setOptionMap(Map&lt;String,Object&gt; optionMap) |
|:---|:---|
| Description | <p>Sets options, by a Map (option map) storing names and values of options you want to set.</p> <p>Type of the option map is Map&lt;String,Object&gt;, and its keys represents option names. For details, see [Option Items](#options) section.</p><p>Please note that, if any value stored in the option map is changed after setting the map by this method, it is NOT guaranteed that the engine reflects the change. Hence, when any value is changed, re-set the option map again by this method.</p> |
| Parameters | optionMap: A Map (option map) storing names and values of options |
| Return | None |
| Exception | VnanoException will be thrown if invalid option settings is detected. |


| Signature | boolean hasOptionMap() |
|:---|:---|
| Description | Returns whether getOptionMap() method can return a Map. |
| Parameters | None |
| Return | Returns true if getOptionMap() method can return a Map. |
| Exception | None |


| Signature | Map&lt;String,Object&gt; getOptionMap() |
|:---|:---|
| Description | Gets the Map (option map) storing names and values of options. |
| Parameters | None |
| Return | A Map (option map) storing names and values of options |
| Exception | VnanoFatalException (unchecked exception) will be thrown if this method is called in a state in which hasOptionMap() method returns false. |



| Signature | void setPermissionMap(Map&lt;String, String&gt; permissionMap) |
|:---|:---|
| Description | <p>Sets permissions, by a Map (permission map) storing names and values of permission items you want to set.</p> <p>Type of the permission map is Map&lt;String,String&gt;, and its keys represents names of permission items. For details, see [Permission Items](#permissions) section.</p><p>Please note that, if any value stored in the permission map is changed after setting the map by this method, it is NOT guaranteed that the engine reflects the change. Hence, when any value is changed, re-set the permission map again by this method.</p>  |
| Parameters | permissionMap: A Map (permission map) storing names and values of permission items |
| Return | None |
| Exception | VnanoException will be thrown if invalid permission settings is detected. |


| Signature | boolean hasPermissionMap() |
|:---|:---|
| Description | Returns whether getPermissionMap() method can return a Map. |
| Parameters | None |
| Return | Returns true if getPermissionMap() method can return a Map. |
| Exception | None |


| Signature | Map&lt;String,String&gt; getPermissionMap() |
|:---|:---|
| Description | Gets the Map (permission map) storing names and values of permission items. |
| Parameters | None |
| Return | A Map (permission map) storing names and values of permission items |
| Exception | VnanoFatalException (unchecked exception) will be thrown if this method is called in a state in which hasPermissionMap() method returns false. |


| Signature | boolean hasPerformanceMap() |
|:---|:---|
| Description | <p>Returns whether getPerformanceMap() method can return a Map.</p> <p>Internally, this method checks the value of "PERFORMANCE_MONITOR_ENABLED" option (disabled by default) and returns it.</p> |
| Parameters | None |
| Return | Returns true if getPerformanceMap() method can return a Map. |
| Exception | None |


| Signature | Map&lt;String, Object&gt; getPerformanceMap() |
|:---|:---|
| Description | <p>Gets the Map (performance map) storing names and values of performance monitoring items.</p> <p>Note that, when some measured values for some monitoring items don't exist (e.g.: when any scripts are not running, or running but their performance values are not measualable yet), the returned performance map does not contain values for such monitoring items, so sometimes the returned performance map is incomplete (missing values for some items) or empty. Please be careful of the above point when you "get" measured performance values from the returned performance map.</p> |
| Parameters | None |
| Return | A Map (performance map) storing names and values of performance monitoring items |
| Exception | VnanoFatalException (unchecked exception) will be thrown if this method is called in a state in which hasPerformanceMap() method returns false. |




<a id="options"></a>
## Engine Options

The following is the list of all available options of Vnano Engine. The type of names is "String".
Also, "Env." in the "Default Value" column means "Environment Dependent".

| Name | Type of Value | Default Value | Description |
|:--|:--|:--|:--|
| LOCALE | java.util.Locale | Env. | The locale to switch the language of error messages. |
| MAIN_SCRIPT_NAME  | String | "main script" | The name of the execution target script. |
| MAIN_SCRIPT_DIRECTORY  | String | "." | The path of the directory (folder) in which the execution target script is locating. |
| EVAL_INT_LITERAL_AS_FLOAT  | Boolean | FALSE | An option to regard integer literals as float type in the execution/evaluation target expressions and scripts (excepting library scripts). |
| EVAL_ONLY_EXPRESSION  | Boolean | FALSE | An option to restrict types of available statements in the execution target scripts (excepting library scripts) to only "expression". |
| EVAL_ONLY_FLOAT  | Boolean | FALSE | An option to restrict available data types of operators/operands in the execution target scripts (excepting library scripts) to only "float". |
| ACCELERATOR_ENABLED | Boolean | TRUE | An option to enable/disable the Accelerator (org.vcssl.nano.vm.accelerator.Accelerator), which is the high-speed virtual processor implementation in the VM. |
| ACCELERATOR_OPTIMIZATION_LEVEL | Integer | 3 | <p>An option to control the optimization level of processing in the Accelerator. The value is:</p> <p>0: Dont't optimize.</p> <p>1: Optimize data accesses for reducing overhead costs (e.g.: caching operand/operated values).</p> <p>2: In addition to the above, optimize instructions with keeping code structures (e.g.: join multiple instructions by reordering them).</p> <p>3: In addition to the above, optimize with modifying code structures (e.g.: inline expansion).</p> |
| TERMINATOR_ENABLED | Boolean | FALSE | <p>An option to enable/disable the feature for terminating a running script.</p> <p>If you enable this option, you become to able to terminate a running script BY OPERATION OF THE SCRIPT ENGINE, but the maximum numerical operating speed (and so on) may decreases slightly. Probably, for most cases, users hardly can recognize the decreasing of the operating speed caused by this option. However, for highly optimized numerical computation scripts, the operating speed may decrease about 10% or more. Note that, the script will be terminated when all procedures in the script completed, or when any errors occurred in the script, or when exit() function is called in the script, regardless whether this option is enabled or disabled.</p> |
| PERFORMANCE_MONITOR_ENABLED | Boolean | FALSE | <p>An option to enable/disable the performance monitor.</p> <p>If you enable this option, you become to get performance monitoring values of the engine, but the maximum numerical operating speed (and so on) may decreases to some extent. Probably, for most cases, decreasing of the operating speed caused by this option is not so heavy. However, for highly optimized numerical computation scripts, the operating speed may decrease about 25% or more.</p> |
| DUMPER_ENABLED | Boolean | FALSE | An option to dump states and intermediate representations in the compiler, VM, etc. |
| DUMPER_TARGET | String | "ALL" | <p>Specify the target of to dump. Values are the followings:</p> <p>"ALL": Dump all contents.</p> <p>"INPUTTED_CODE": Dump the inutted script code.</p> <p>"PREPROCESSED_CODE": Dump pre-processed script code, from which comments are removed.</p> <p>"TOKEN": Dump tokens, which are output of the LexicalAnalyzer.</p> <p>"PARSED_AST": Dump the Abstract Syntax Tree (AST), which is the output of the Parser.</p> <p>"ANALYZED_AST": Dump the semantic-analyzed AST, which is the output of the SemanticAnalyzer.</p> <p>"ASSEMBLY_CODE": Dump the VRIL code, which is the compilation result, output of the CodeGenerator.</p> <p>"OBJECT_CODE": Dump the VM object code (unoptimized), which is output of the Assembler.</p> <p>"ACCELERATOR_CODE": Dump optimized instructions for the Accelerator, which are output of the AcceleratorOptimizationUnit.</p> <p>"ACCELERATOR_STATE": Dump the internal state (dispatchments of execution units, and so on) of the Accelerator.</p> |
| DUMPER_STREAM | java.io.PrintStream | System.out | Specify the stream to output dumped contents. |
| RUNNING_ENABLED  | Boolean | TRUE | <p>An option to switch whether execute script or don't.</p> <p>This option might be useful when you want to dump the compiled result for debugging but don't want to run it.</p> |
| AUTOMATIC_ACTIVATION_ENABLED  | Boolean | TRUE | <p>An option to switch whether activate/deactivate the Vnano engine automatically before/after executing a script ("automatic activation" feature).</p> <p>This option is enabled by default so that users can execute scripts any time. However, activations/deactivations of the engine entail some overhead costs. Especially when the engine repetitively executes scripts in high frequency, this "activation costs" may result serious degradation of processing speed, if this option is enabled. In such case, disable this option, and activate/deactivate the engine manually at suitable timing (typically before/after a set of repetitive executions).</p> |
| UI_MODE | String | "GUI" | <p>Specify the mode of UI for inputting/outputting values and so on.</p> <p>As the value, specify "GUI" or "CUI". The default value is "GUI", but it will be set to "CUI" automatically when you execute the Vnano engine in the command-line mode.</p> <p>This option is referred by I/O plug-ins if they are connected.</p> |
| ENVIRONMENT_EOL | String | Env. | <p>Specify the default line-feed code on the environment.</p> <p>This option is referred by plug-ins providing environment-dependent values, if they are connected.</p> |
| FILE_IO_EOL | String | Env. | <p>Specify the default line-feed code for file I/O.</p> <p>This option is referred by I/O plug-ins if they are connected.</p> |
| TERMINAL_IO_EOL | String | Env. | <p>Specify the default line-feed code for terminal I/O.</p> <p>This option is referred by I/O plug-ins if they are connected.</p> |
| FILE_IO_ENCODING | String | "UTF-8" | <p>Specify the name of the default encoding for writing to / reading files in scripts.</p> <p>This option is referred by I/O plug-ins if they are connected.</p> |
| STDIN_STREAM | Java.io.InputStream | System.in | <p>Specify the stream for standard input used when the option TERMINAL_IO_UI is set to "CUI".</p> <p>This option is referred by I/O plug-ins if they are connected.</p> |
| STDOUT_STREAM | Java.io.PrintStream | System.out | <p>Specify the stream for standard output used when the option TERMINAL_IO_UI is set to "CUI".</p> <p>This option is referred by I/O plug-ins if they are connected.</p> |
| STDERR_STREAM | Java.io.PrintStream | System.err | <p>Specify the stream for standard error output when the option TERMINAL_IO_UI is set to "CUI".</p> <p>This option is referred by I/O plug-ins if they are connected.</p> |



<a id="permissions"></a>
## Permission Items

The following is the list of permission items.
The type of names is "String". The type of values is also "String", and it takes either "DENY", "ALLOW", or "ASK". A permission value "ASK" means that: "determine whether allow or deny by asking the user". By defailt, all values are set to "DENY".

| Name | Description
|:--|:--|
| PROGRAM_EXIT | The permission to exit the currently executed program (script). |
| PROGRAM_RESET | The permission to reset/restart the currently executed program (script). |
| PROGRAM_CHANGE | The permission to change the currently executed program (script). |
| SYSTEM_PROCESS | The permission to execute commands or other programs through the Operating System and so on. |
| DIRECTORY_CREATE | The permission to create a new directory (folder) |
| DIRECTORY_DELETE | The permission to delete a directory (folder). |
| DIRECTORY_LIST | The permission to get the list of files in a directory (folder). |
| FILE_CREATE | The permission to create a new file. |
| FILE_DELETE | The permission to delete a file. |
| FILE_WRITE | The permission to write contents of a file. |
| FILE_READ | The permission to read contents of a file. |
| FILE_OVERWRITE | The permission to overwrite contents of a file. |
| FILE_INFORMATION_CHANGE | The permission to change information (last modified date, and so on) of a file. |
| ALL | The name of the meta item representing all permission items. |
| NONE | The name of the meta item representing no permission item. |
| DEFAULT  | <p>The name of the meta item storing the default permission value.</p> <p>For permission items of which values are not specified explicitly, a default value (e.g. "DENY") will be set automatically. You can change that default value by setting the value to this meta permission item. For example, if you set the value "ASK" to this permission item "DEFAULT", the script engine will ask to the user when non-specified permissions are required.</p> |

