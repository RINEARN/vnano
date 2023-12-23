package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.OptionKey;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ExternalVariableConnectorInterface1;
import org.vcssl.connect.EngineConnectorInterface1;

import java.util.Map;
import java.util.HashMap;

public class ActivationDeactivationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;
	ActivationCounterPlugin counterPlugin = null;

	@Override
	public void initializeTest(VnanoEngine engine) {
		this.engine = engine;
		this.counterPlugin = new ActivationCounterPlugin();
		try {
			this.engine.connectPlugin("dummyVariable", this.counterPlugin);
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	@Override
	public void finalizeTest() {
		try {
			this.engine.disconnectAllPlugins();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
		this.engine = null;
		this.counterPlugin = null;
	}

	@Override
	public void executeTest() {
		try {
			this.testAutoActivations();
			this.testManualActivations();

			// Re-test on the same engine.
			this.testAutoActivations();
			this.testManualActivations();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAutoActivations() throws VnanoException {
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put(OptionKey.AUTOMATIC_ACTIVATION_ENABLED, Boolean.TRUE);
		engine.setOptionMap(optionMap);

		String scriptCode = ";";
		this.counterPlugin.initializedCounter = 0;
		this.counterPlugin.finalizedCounter = 0;

		super.evaluateResult(this.counterPlugin.initializedCounter, 0, "initialized count 0 (auto activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 0, "finalized count 0 (auto activation)", "none");

		engine.executeScript(scriptCode);

		super.evaluateResult(this.counterPlugin.initializedCounter, 1, "initialized count 1 (auto activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 1, "finalized count 1 (auto activation)", "none");

		engine.executeScript(scriptCode);

		super.evaluateResult(this.counterPlugin.initializedCounter, 2, "initialized count 2 (auto activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 2, "finalized count 2 (auto activation)", "none");

		engine.executeScript(scriptCode);

		super.evaluateResult(this.counterPlugin.initializedCounter, 3, "initialized count 3 (auto activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 3, "finalized count 3 (auto activation)", "none");
	}

	private void testManualActivations() throws VnanoException {
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put(OptionKey.AUTOMATIC_ACTIVATION_ENABLED, Boolean.FALSE);
		engine.setOptionMap(optionMap);

		String scriptCode = ";";
		this.counterPlugin.initializedCounter = 0;
		this.counterPlugin.finalizedCounter = 0;

		super.evaluateResult(this.counterPlugin.initializedCounter, 0, "initialized count 0 (manual activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 0, "finalized count 0 (manual activation)", "none");

		engine.activate();

		super.evaluateResult(this.counterPlugin.initializedCounter, 1, "initialized count 1 after 1 activation (manual activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 0, "finalized count 0 after 1 activation (manual activation)", "none");

		engine.executeScript(scriptCode);
		engine.executeScript(scriptCode);
		engine.executeScript(scriptCode);

		super.evaluateResult(this.counterPlugin.initializedCounter, 1, "initialized count 1 after 1 activation and 3 executions (manual activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 0, "finalized count 0 after 1 activation 3 executions (manual activation)", "none");

		engine.deactivate();

		super.evaluateResult(this.counterPlugin.initializedCounter, 1, "initialized count 1 after 1 deactivation (manual activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 1, "finalized count 1 after 1 deactivation (manual activation)", "none");

		engine.activate();
		engine.deactivate();

		super.evaluateResult(this.counterPlugin.initializedCounter, 2, "initialized count 2 after 2 deactivations/deactivations (manual activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 2, "finalized count 2 after 2 deactivations/deactivations (manual activation)", "none");

		engine.activate();
		engine.deactivate();

		super.evaluateResult(this.counterPlugin.initializedCounter, 3, "initialized count 3 after 3 deactivations/deactivations (manual activation)", "none");
		super.evaluateResult(this.counterPlugin.finalizedCounter, 3, "finalized count 3 after 3 deactivations/deactivations (manual activation)", "none");
	}


	public class ActivationCounterPlugin implements ExternalVariableConnectorInterface1 {
		public volatile int initializedCounter = 0;
		public volatile int finalizedCounter = 0;

		@Override
		public void initializeForConnection(Object engineConnector) throws ConnectorException {
		}
		@Override
		public void finalizeForDisconnection(Object engineConnector) throws ConnectorException {
		}
		@Override
		public void initializeForExecution(Object engineConnector) throws ConnectorException {
			this.initializedCounter++;
		}
		@Override
		public void finalizeForTermination(Object engineConnector) throws ConnectorException {
			this.finalizedCounter++;
		}

		@Override
		public String getVariableName() {
			return "dummyVariable";
		}
		@Override
		public Class<?> getDataClass() {
			return double.class;
		}
		@Override
		public Class<?> getDataUnconvertedClass() {
			return null;
		}
		@Override
		public boolean isConstant() {
			return false;
		}
		@Override
		public boolean isReference() {
			return false;
		}
		@Override
		public boolean isDataTypeArbitrary() {
			return false;
		}
		@Override
		public boolean isArrayRankArbitrary() {
			return false;
		}
		@Override
		public boolean isDataConversionNecessary() {
			return true;
		}
		@Override
		public Object getData() throws ConnectorException {
			return Double.NaN;
		}
		@Override
		public void getData(Object dataContainer) throws ConnectorException {
		}
		@Override
		public void setData(Object data) throws ConnectorException {
		}
		@Override
		public Class<?> getEngineConnectorClass() {
			return EngineConnectorInterface1.class;
		}
	}
}
