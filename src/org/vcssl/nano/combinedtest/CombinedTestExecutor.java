package org.vcssl.nano.combinedtest;

import java.util.ArrayList;
import java.util.List;

import org.vcssl.nano.VnanoEngine;

public class CombinedTestExecutor {
	public void test(VnanoEngine engine) {

		System.out.println("");
		System.out.println(" - Vnano Engine Combined Test -");
		System.out.println("");
		System.out.println("----------------------------------------------------------------");
		System.out.println("");

		List<CombinedTestElement> testElementList = new ArrayList<CombinedTestElement>();

		// Add test element here
		testElementList.add(new LiteralCombinedTest());
		testElementList.add(new ScalarAssignmentOperationCombinedTest());
		testElementList.add(new ScalarArithmeticOperationCombinedTest());
		testElementList.add(new ScalarLogicalOperationCombinedTest());
		testElementList.add(new ScalarComparisonOperationCombinedTest());
		testElementList.add(new ScalarCompoundAssignmentOperationCombinedTest());
		testElementList.add(new VectorAssignmentOperationCombinedTest());
		testElementList.add(new VectorArithmeticOperationCombinedTest());
		testElementList.add(new VectorLogicalOperationCombinedTest());
		testElementList.add(new VectorComparisonOperationCombinedTest());
		testElementList.add(new VectorCompoundAssignmentOperationCombinedTest());
		testElementList.add(new VectorCastOperationCombinedTest());
		testElementList.add(new SubscriptOperationCombinedTest());
		testElementList.add(new SubscriptedAssignmentOperationCombinedTest());
		testElementList.add(new SubscriptedArithmeticOperationCombinedTest());
		testElementList.add(new SubscriptedLogicalOperationCombinedTest());
		testElementList.add(new SubscriptedComparisonOperationCombinedTest());
		testElementList.add(new SubscriptedCompoundAssignmentOperationCombinedTest());
		testElementList.add(new EmptyStatementCombinedTest());
		testElementList.add(new VariableDeclarationStatementCombinedTest());
		testElementList.add(new BlockStatementCombinedTest());
		testElementList.add(new IfElseStatementCombinedTest());
		testElementList.add(new WhileStatementCombinedTest());
		testElementList.add(new ForStatementCombinedTest());
		testElementList.add(new FunctionCombinedTest());
		testElementList.add(new ActivationDeactivationCombinedTest());
		testElementList.add(new RepetitiveExecutionCombinedTest());

		for (CombinedTestElement testElement: testElementList) {
			System.out.println("[ " + testElement.getClass().getCanonicalName() + " ]");
			testElement.initializeTest(engine);
			testElement.executeTest();
			testElement.finalizeTest();
			System.out.println("");
		}

		System.out.println("----------------------------------------------------------------");
		System.out.println("");
		System.out.println("All combined tests have been completed successfully.");
		System.out.println("");
	}
}
