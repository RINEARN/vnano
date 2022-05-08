/**
 * <p>
 * <span class="lang-en">
 * The package performing functions to manage and to provide some information
 * shared between multiple components in the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnanoのスクリプトエンジン内の各コンポーネント間で共有される,
 * いくつかの情報を管理・提供する機能を担うパッケージです
 * </span>
 * .
 * <span class="lang-en">
 * We refer this component as "Interconnect" in the script engine of the Vnano Engine.
 * </span>
 * <span class="lang-ja">
 * この機能を担うコンポーネントを, Vnanoのスクリプトエンジンでは "インターコネクト" と呼びます.
 * </span>
 * </p>
 *
 * <p>
 * &laquo; <a href="../package-summary.html">Upper layer package</a>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * The outer-frame of the interconnect provided by this package is {@link Interconnect Interconnect} class,
 * and others are internal components.
 * </span>
 * <span class="lang-ja">
 * このパッケージが提供するインターコネクトの外枠となるのは {@link Interconnect Interconnect} クラスで,
 * 他は内部の構成クラスです.
 * </span>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * For example, classes and interfaces to resolve references of variables and functions
 * are managed by this interconnect package.
 * Bindings to external functions/variables are intermediated by {@link Interconnect Interconnect} class,
 * so plug-ins of external functions/variables will be connected to it.
 * </span>
 *
 * <span class="lang-ja">
 * このインターコネクトパッケージが管理・提供するものの具体例としては,
 * 関数・変数の参照解決のためのクラスやインターフェースなどが挙げられます.
 * 外部変数・外部関数のバインディングも,{@link Interconnect Interconnect} クラスを介して行われます.
 * 従って, 外部変数・外部関数のプラグインも, Vnanoエンジン内でこのインターコネクト層に接続されます.
 * </span>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * Plug-in interfaces supported by this interconnect are as follows:
 * </span>
 * <span class="lang-ja">
 * このインターコネクトでサポートされているプラグインインターフェースは以下のとおりです:
 * </p>
 *
 * <div class="lang-ja" style="border-style: solid; padding-left: 10px; line-height: 160%;">
 * <ul>
 *   <li>XVCI 1 ({@link org.vcssl.connect.ExternalVariableConnectorInterface1 org.vcssl.connector.ExternalVariableConnector1})</li>
 *   <li>XFCI 1 ({@link org.vcssl.connect.ExternalFunctionConnectorInterface1 org.vcssl.connector.ExternalFunctionConnector1})</li>
 *   <li>XNCI 1 ({@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 org.vcssl.connector.ExternalNamespaceConnector1})</li>
 *   <li>java.lang.reflect.Field (内部で {@link org.vcssl.connect.FieldToXvci1Adapter FieldToXvci1Adapter} を介し、XVCI 1 で接続されます。)</li>
 *   <li>java.lang.reflect.Method (内部で {@link org.vcssl.connect.MethodToXfci1Adapter MethodToXfci1Adapter} を介し、XFCI 1 で接続されます。)</li>
 *   <li>java.lang.Class (内部で {@link org.vcssl.connect.ClassToXnci1Adapter ClassToXnci1Adapter} を介し、XNCI 1 で接続されます。)</li>
 *   <li>java.lang.Object (内部で {@link org.vcssl.connect.ClassToXnci1Adapter ClassToXnci1Adapter} を介し、XNCI 1 で接続されます。)</li>
 * </ul>
 * </div>
 *
 * <div class="lang-en" style="border-style: solid; padding-left: 10px; line-height: 160%;">
 * <ul>
 *   <li>XVCI 1 ({@link org.vcssl.connect.ExternalVariableConnectorInterface1 org.vcssl.connector.ExternalVariableConnector1})</li>
 *   <li>XFCI 1 ({@link org.vcssl.connect.ExternalFunctionConnectorInterface1 org.vcssl.connector.ExternalFunctionConnector1})</li>
 *   <li>XNCI 1 ({@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 org.vcssl.connector.ExternalNamespaceConnector1})</li>
 *   <li>java.lang.reflect.Field (will be connected by XVCI 1 thtough {@link org.vcssl.connect.FieldToXvci1Adapter FieldToXvci1Adapter})</li>
 *   <li>java.lang.reflect.Method (will be connected by XFCI 1 thtough {@link org.vcssl.connect.MethodToXfci1Adapter MethodToXfci1Adapter})</li>
 *   <li>java.lang.Class (will be connected by XNCI 1 thtough {@link org.vcssl.connect.ClassToXnci1Adapter ClassToXnci1Adapter})</li>
 *   <li>java.lang.Object (will be connected by XNCI 1 thtough {@link org.vcssl.connect.ClassToXnci1Adapter ClassToXnci1Adapter})</li>
 * </ul>
 * </div>
 */
package org.vcssl.nano.interconnect;

