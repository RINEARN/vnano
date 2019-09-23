package org.vcssl.nano.combinedtest;

import java.util.LinkedList;
import java.util.List;

public class CombinedTestExecutor {
	public void test() {

		System.out.println("");
		System.out.println(" - Vnano Engine Combined Test -");
		System.out.println("");
		System.out.println("----------------------------------------------------------------");

		List<CombinedTestElement> testElementList = new LinkedList<CombinedTestElement>();

		// [ Add test element here ]

		for (CombinedTestElement testElement: testElementList) {
			testElement.initialize();
			testElement.test();
			testElement.finalize();
		}

		System.out.println("----------------------------------------------------------------");
		System.out.println("");
		System.out.println("All combined tests have been completed successfully.");
		System.out.println("");
	}
}
