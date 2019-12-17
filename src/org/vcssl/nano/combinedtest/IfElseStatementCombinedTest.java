package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class IfElseStatementCombinedTest extends CombinedTestElement {

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
			this.testIfStatements();
			this.testElseStatements();
			this.testIfElseStatements();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testIfStatements() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int a = 1;   \n" +
			" if (true) {  \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2, "if(true){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 1, "if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 1, "if(false){...} if(false){...} if(false){...} if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (true)  { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2, "if(true){...} if(false){...} if(false){...} if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (true)  { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 3, "if(false){...} if(true){...} if(false){...} if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true)  { \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 4, "if(false){...} if(false){...} if(true){...} if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(false){...} if(false){...} if(false){...} if(true){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (true) {  \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 4, "if(true){...} if(false){...} if(true){...} if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 4, "if(false){...} if(true){...} if(true){...} if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(false){...} if(false){...} if(true){...} if(true){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (true) {  \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(true){...} if(false){...} if(false){...} if(true){...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (true) {  \n" +
			"     a = 2;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 4;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(true){...} if(true){...} if(true){...} if(true){...}", scriptCode);
	}

	private void testElseStatements() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int a = 1;  \n" +
			" if (true) { \n" +
			"     a = 2;  \n" +
			" } else {    \n" +
			"     a = 3;  \n" +
			" }           \n" +
			" a;          \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2, "if(true){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" } else {     \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 3, "if(false){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" } else {     \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" } else {     \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 6;   \n" +
			" } else {     \n" +
			"     a = 7;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 8;   \n" +
			" } else {     \n" +
			"     a = 9;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 10;  \n" +
			" } else {     \n" +
			"     a = 11;  \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 11, "if(false){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (true) {  \n" +
			"     a = 2;   \n" +
			" } else {     \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" } else {     \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 6;   \n" +
			" } else {     \n" +
			"     a = 7;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 8;   \n" +
			" } else {     \n" +
			"     a = 9;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 10;  \n" +
			" } else {     \n" +
			"     a = 11;  \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 11, "if(true){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" } else {     \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 4;   \n" +
			" } else {     \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 6;   \n" +
			" } else {     \n" +
			"     a = 7;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 8;   \n" +
			" } else {     \n" +
			"     a = 9;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 10;  \n" +
			" } else {     \n" +
			"     a = 11;  \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "if(false){...} else{...} if(true){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" } else {     \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 4;   \n" +
			" } else {     \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 6;   \n" +
			" } else {     \n" +
			"     a = 7;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 8;   \n" +
			" } else {     \n" +
			"     a = 9;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 10;  \n" +
			" } else {     \n" +
			"     a = 11;  \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "if(false){...} else{...} if(true){...} else{...} if(false){...} else{...} if(false){...} else{...} if(true){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (false) { \n" +
			"     a = 2;   \n" +
			" } else {     \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 4;   \n" +
			" } else {     \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 6;   \n" +
			" } else {     \n" +
			"     a = 7;   \n" +
			" }            \n" +
			" if (false) { \n" +
			"     a = 8;   \n" +
			" } else {     \n" +
			"     a = 9;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 10;  \n" +
			" } else {     \n" +
			"     a = 11;  \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "if(false){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...} if(true){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;   \n" +
			" if (true) {  \n" +
			"     a = 2;   \n" +
			" } else {     \n" +
			"     a = 3;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 4;   \n" +
			" } else {     \n" +
			"     a = 5;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 6;   \n" +
			" } else {     \n" +
			"     a = 7;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 8;   \n" +
			" } else {     \n" +
			"     a = 9;   \n" +
			" }            \n" +
			" if (true) {  \n" +
			"     a = 10;  \n" +
			" } else {     \n" +
			"     a = 11;  \n" +
			" }            \n" +
			" a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "if(false){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...} if(false){...} else{...}", scriptCode);
	}


	private void testIfElseStatements() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int a = 1;          \n" +
			" if (true) {         \n" +
			"     a = 2;          \n" +
			" } else if (false) { \n" +
			"     a = 3;          \n" +
			" }                   \n" +
			" a;                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2, "if(true){...} else if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;          \n" +
			" if (false) {        \n" +
			"     a = 2;          \n" +
			" } else if (false) { \n" +
			"     a = 3;          \n" +
			" }                   \n" +
			" a;                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 1, "if(false){...} else if(false){...}", scriptCode);

		scriptCode =
			" int a = 1;          \n" +
			" if (false) {        \n" +
			"     a = 2;          \n" +
			" } else if (true) {  \n" +
			"     a = 3;          \n" +
			" }                   \n" +
			" a;                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 3, "if(false){...} else if(true){...}", scriptCode);

		scriptCode =
			" int a = 1;          \n" +
			" if (false) {        \n" +
			"     a = 2;          \n" +
			" } else if (false) { \n" +
			"     a = 3;          \n" +
			" } else if (false) { \n" +
			"     a = 4;          \n" +
			" } else if (true)  { \n" +
			"     a = 5;          \n" +
			" } else if (false) { \n" +
			"     a = 6;          \n" +
			" } else if (false) { \n" +
			"     a = 7;          \n" +
			" } else if (false) { \n" +
			"     a = 8;          \n" +
			" } else if (false) { \n" +
			"     a = 9;          \n" +
			" } else {            \n" +
			"     a = 10;         \n" +
			" }                   \n" +
			" a;                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(false){...} else if(false){...} x2 else if(true){...} else if(false){...} x4 else{...}", scriptCode);

		scriptCode =
			" int a = 1;          \n" +
			" if (false) {        \n" +
			"     a = 2;          \n" +
			" } else if (false) { \n" +
			"     a = 3;          \n" +
			" } else if (false) { \n" +
			"     a = 4;          \n" +
			" } else if (true)  { \n" +
			"     a = 5;          \n" +
			" } else if (false) { \n" +
			"     a = 6;          \n" +
			" } else if (false) { \n" +
			"     a = 7;          \n" +
			" } else if (true) {  \n" +
			"     a = 8;          \n" +
			" } else if (false) { \n" +
			"     a = 9;          \n" +
			" } else {            \n" +
			"     a = 10;         \n" +
			" }                   \n" +
			" a;                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(false){...} else if(false){...} x2 else if(true){...} else if(false){...} x2 else if(true){...} else if(false){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;          \n" +
			" if (false) {        \n" +
			"     a = 2;          \n" +
			" } else if (false) { \n" +
			"     a = 3;          \n" +
			" } else if (false) { \n" +
			"     a = 4;          \n" +
			" } else if (false) { \n" +
			"     a = 5;          \n" +
			" } else if (false) { \n" +
			"     a = 6;          \n" +
			" } else if (false) { \n" +
			"     a = 7;          \n" +
			" } else if (true) {  \n" +
			"     a = 8;          \n" +
			" } else if (false) { \n" +
			"     a = 9;          \n" +
			" } else {            \n" +
			"     a = 10;         \n" +
			" }                   \n" +
			" a;                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 8, "if(false){...} else if(false){...} x5 if(true){...} else if(false){...} else{...}", scriptCode);

		scriptCode =
			" int a = 1;          \n" +
			" if (false) {        \n" +
			"     a = 2;          \n" +
			" } else if (false) { \n" +
			"     a = 3;          \n" +
			" } else if (false) { \n" +
			"     a = 4;          \n" +
			" } else if (false) { \n" +
			"     a = 5;          \n" +
			" } else if (false) { \n" +
			"     a = 6;          \n" +
			" } else if (false) { \n" +
			"     a = 7;          \n" +
			" } else if (false) { \n" +
			"     a = 8;          \n" +
			" } else if (false) { \n" +
			"     a = 9;          \n" +
			" } else {            \n" +
			"     a = 10;         \n" +
			" }                   \n" +
			" a;                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "if(false){...} else if(false){...} x7 else{...}", scriptCode);

	}
}
