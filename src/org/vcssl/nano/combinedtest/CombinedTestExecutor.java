package org.vcssl.nano.combinedtest;

import java.util.LinkedList;
import java.util.List;

import org.vcssl.nano.VnanoEngine;

public class CombinedTestExecutor {
	public void test(VnanoEngine engine) {

		System.out.println("");
		System.out.println(" - Vnano Engine Combined Test -");
		System.out.println("");
		System.out.println("----------------------------------------------------------------");
		System.out.println("");

		List<CombinedTestElement> testElementList = new LinkedList<CombinedTestElement>();


		// Add test element here
		testElementList.add(new AssignmentExpressionCombinedTest());
		testElementList.add(new ArithmeticExpressionCombinedTest());
		testElementList.add(new LogicalExpressionCombinedTest());
		testElementList.add(new ComparisonExpressionCombinedTest());
		testElementList.add(new CompoundAssignmentExpressionCombinedTest());
		testElementList.add(new IfElseStatementCombinedTest());
		testElementList.add(new WhileStatementCombinedTest());
		testElementList.add(new ForStatementCombinedTest());

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
