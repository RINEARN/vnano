package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class WhileStatementCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	@Override
	public void initializeTest(VnanoEngine engine) {
		this.engine = engine;
	}

	@Override
	public void finalizeTest() {
		this.engine = null;
	}

	@Override
	public void executeTest() {
		try {
			this.testWhileStatements();
			this.testMultipleWhileStatements();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testWhileStatements() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int i = 0;       \n" +
			" while (i < 10) { \n" +
			"     i++;         \n" +
			" }                \n" +
			" i;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=1; while(i<10){i++;}", scriptCode);

		scriptCode =
			" int i = 0;       \n" +
			" while (i > 10) { \n" +
			"     i++;         \n" +
			" }                \n" +
			" i;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 0, "i=1; while(i>10){i++;}", scriptCode);

		scriptCode =
			" int i = 0;           \n" +
			" while ((i++) < 10) { \n" +
			" }                    \n" +
			" i;                   \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 11, "i=1; while((i++)<10){}", scriptCode);

		scriptCode =
			" int i = 0;           \n" +
			" while ((++i) < 10) { \n" +
			" }                    \n" +
			" i;                   \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=1; while((++i)<10){}", scriptCode);
	}


	private void testMultipleWhileStatements() throws VnanoException {
		String scriptCode;
		String result;

		scriptCode =
			" int i = 0;             \n" +
			" int j = 0;             \n" +
			" int k = 0;             \n" +
			" int l = 0;             \n" +
			" int m = 0;             \n" +
			" while (i < 10) {       \n" +
			"     i++;               \n" +
			" }                      \n" +
			" while (j < 20) {       \n" +
			"     j++;               \n" +
			" }                      \n" +
			" while (k < 30) {       \n" +
			"     k++;               \n" +
			" }                      \n" +
			" while (l < 40) {       \n" +
			"     l++;               \n" +
			" }                      \n" +
			" while (m < 50) {       \n" +
			"     m++;               \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" +
			" result += \",k=\" + k; \n" +
			" result += \",l=\" + l; \n" +
			" result += \",m=\" + m; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=10,j=20,k=30,l=40,m=50", "while(...){...} x 5", scriptCode);

		scriptCode =
			" int i = 0;             \n" +
			" int j = 0;             \n" +
			" int k = 0;             \n" +
			" int l = 0;             \n" +
			" int m = 0;             \n" +
			" while ((i++) < 10) {   \n" +
			" }                      \n" +
			" while ((j++) < 20) {   \n" +
			" }                      \n" +
			" while ((k++) < 30) {   \n" +
			" }                      \n" +
			" while ((l++) < 40) {   \n" +
			" }                      \n" +
			" while ((m++) < 50) {   \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" +
			" result += \",k=\" + k; \n" +
			" result += \",l=\" + l; \n" +
			" result += \",m=\" + m; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=11,j=21,k=31,l=41,m=51", "while((x++)<y){...} x 5", scriptCode);

		scriptCode =
			" int i = 0;             \n" +
			" int j = 0;             \n" +
			" int k = 0;             \n" +
			" int l = 0;             \n" +
			" int m = 0;             \n" +
			" while ((++i) < 10) {   \n" +
			" }                      \n" +
			" while ((++j) < 20) {   \n" +
			" }                      \n" +
			" while ((++k) < 30) {   \n" +
			" }                      \n" +
			" while ((++l) < 40) {   \n" +
			" }                      \n" +
			" while ((++m) < 50) {   \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" +
			" result += \",k=\" + k; \n" +
			" result += \",l=\" + l; \n" +
			" result += \",m=\" + m; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=10,j=20,k=30,l=40,m=50", "while((++x)<y){...} x 5", scriptCode);
	}


}
