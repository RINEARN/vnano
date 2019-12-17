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
			this.testIfElseStatements();
			this.testElseIfStatements();

			this.testMultipleIfStatements();
			this.testMultipleIfElseStatements();
			this.testMultipleElseIfStatements();

			this.testDeepBlockDepthCases();
			this.testVeryComplicatedCases();

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
	}


	private void testIfElseStatements() throws VnanoException {
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
	}


	private void testElseIfStatements() throws VnanoException {
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
	}


	private void testMultipleIfStatements() throws VnanoException {
		String scriptCode;
		long result;

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



	private void testMultipleIfElseStatements() throws VnanoException {
		String scriptCode;
		long result;

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


	private void testMultipleElseIfStatements() throws VnanoException {
		String scriptCode;
		long result;

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


	private void testDeepBlockDepthCases() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int a = 1;       \n" +
			" if (false) {     \n" +
			"     a = 2;       \n" +
			"     if (false) { \n" +
			"         a = 3;   \n" +
			"     }            \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 1, "if(false){... if(false) {...} }", scriptCode);

		scriptCode =
			" int a = 1;       \n" +
			" if (true) {      \n" +
			"     a = 2;       \n" +
			"     if (true) {  \n" +
			"         a = 3;   \n" +
			"     }            \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 3, "if(true){... if(true) {...} }", scriptCode);

		scriptCode =
			" int a = 1;       \n" +
			" if (true) {      \n" +
			"     a = 2;       \n" +
			"     if (false) { \n" +
			"         a = 3;   \n" +
			"     }            \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2, "if(true){... if(true) {...} }", scriptCode);

		scriptCode =
			" int a = 1;       \n" +
			" if (false) {     \n" +
			"     a = 2;       \n" +
			"     if (true) {  \n" +
			"         a = 3;   \n" +
			"     }            \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 1, "if(false){... if(true) {...} }", scriptCode);

		scriptCode =
			" int a = 1;       \n" +
			" if (true) {      \n" +
			"     a = 2;       \n" +
			"     if (false) { \n" +
			"         a = 3;   \n" +
			"     } else {     \n" +
			"         a = 4;   \n" +
			"     }            \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 4, "if(true){... if(false) {...} else {...} }", scriptCode);

		scriptCode =
			" int a = 1;       \n" +
			" if (true) {      \n" +
			"     a = 2;       \n" +
			"     if (true)  { \n" +
			"         a = 3;   \n" +
			"     } else {     \n" +
			"         a = 4;   \n" +
			"     }            \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 3, "if(true){... if(true) {...} else {...} }", scriptCode);

		scriptCode =
			" int a = 1;       \n" +
			" if (true) {      \n" +
			"     a = 2;       \n" +
			"     if (true)  { \n" +
			"         a = 3;   \n" +
			"     } else {     \n" +
			"         a = 4;   \n" +
			"     }            \n" +
			" } else {         \n" +
			"     a = 5;       \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 3, "if(true){... if(true) {...} else {...} } else {...}", scriptCode);

		scriptCode =
			" int a = 1;       \n" +
			" if (false) {     \n" +
			"     a = 2;       \n" +
			"     if (true)  { \n" +
			"         a = 3;   \n" +
			"     } else {     \n" +
			"         a = 4;   \n" +
			"     }            \n" +
			" } else {         \n" +
			"     a = 5;       \n" +
			" }                \n" +
			" a;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(false){... if(true) {...} else {...} } else {...}", scriptCode);

		scriptCode =
			" int a = 1;               \n" +
			" if (true) {              \n" +
			"     a = 2;               \n" +
			"     if (true)  {         \n" +
			"         a = 3;           \n" +
			"         if (true)  {     \n" +
			"             a = 4;       \n" +
			"             if (true)  { \n" +
			"                 a = 5;   \n" +
			"             }            \n" +
			"         }                \n" +
			"     } else {             \n" +
			"         a = 6;           \n" +
			"     }                    \n" +
			" } else {                 \n" +
			"     a = 7;               \n" +
			" }                        \n" +
			" a;                       \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 5, "if(true){... if(true){... if(true){... if(false){...} } } else{...} } else{...}", scriptCode);

		scriptCode =
			" int a = 1;               \n" +
			" if (true) {              \n" +
			"     a = 2;               \n" +
			"     if (true)  {         \n" +
			"         a = 3;           \n" +
			"         if (true)  {     \n" +
			"             a = 4;       \n" +
			"             if (false) { \n" +
			"                 a = 5;   \n" +
			"             }            \n" +
			"         }                \n" +
			"     } else {             \n" +
			"         a = 6;           \n" +
			"     }                    \n" +
			" } else {                 \n" +
			"     a = 7;               \n" +
			" }                        \n" +
			" a;                       \n" ;

		result = (long)this.engine.executeScript(scriptCode);

		super.evaluateResult(result, 4, "if(true){... if(false){... if(true){... if(false){...} } } else{...} } else{...}", scriptCode);
		scriptCode =
			" int a = 1;               \n" +
			" if (true) {              \n" +
			"     a = 2;               \n" +
			"     if (false)  {        \n" +
			"         a = 3;           \n" +
			"         if (true)  {     \n" +
			"             a = 4;       \n" +
			"             if (false) { \n" +
			"                 a = 5;   \n" +
			"             }            \n" +
			"         }                \n" +
			"     } else {             \n" +
			"         a = 6;           \n" +
			"     }                    \n" +
			" } else {                 \n" +
			"     a = 7;               \n" +
			" }                        \n" +
			" a;                       \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 6, "if(true){... if(false){... if(true){... if(false){...} } } else{...} } else{...}", scriptCode);

		scriptCode =
			" int a = 1;               \n" +
			" if (false) {             \n" +
			"     a = 2;               \n" +
			"     if (false)  {        \n" +
			"         a = 3;           \n" +
			"         if (true)  {     \n" +
			"             a = 4;       \n" +
			"             if (false) { \n" +
			"                 a = 5;   \n" +
			"             }            \n" +
			"         }                \n" +
			"     } else {             \n" +
			"         a = 6;           \n" +
			"     }                    \n" +
			" } else {                 \n" +
			"     a = 7;               \n" +
			" }                        \n" +
			" a;                       \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 7, "if(false){... if(false){... if(true){... if(false){...} } } else{...} } else{...}", scriptCode);
	}


	private void testVeryComplicatedCases() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int a = 1;              \n" +
			" if (false) {            \n" +
			"     a = 2;              \n" +
			"     if (true)  {        \n" +
			"         a = 3;          \n" +
			"     } else {            \n" +
			"         a = 4;          \n" +
			"     }                   \n" +
			" } else if (true) {      \n" +
			"     a = 5;              \n" +
			"     if (false)  {       \n" +
			"         a = 6;          \n" +
			"     } else if (false) { \n" +
			"         a = 7;          \n" +
			"     } else if (false) { \n" +
			"         a = 8;          \n" +
			"     } else if (true) {  \n" +
			"         a = 9;          \n" +
			"         if (false) {    \n" +
			"             a = 10;     \n" +
			"         } else {        \n" +
			"             a = 11;     \n" +
			"         }               \n" +
			"     } else if (false) { \n" +
			"         a = 10;         \n" +
			"     } else {            \n" +
			"         a = 7;          \n" +
			"     }                   \n" +
			" } else {                \n" +
			"     a = 5;              \n" +
			" }                       \n" +
			" a;                      \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 11, "very complicated case 1", scriptCode);

		scriptCode =
			" int a = 1;                          \n" +
			" if (false) {                        \n" +
			"     a = 2;                          \n" +
			"     if (true)  {                    \n" +
			"         a = 3;                      \n" +
			"     } else {                        \n" +
			"         a = 4;                      \n" +
			"     }                               \n" +
			" } else if (true) {                  \n" +
			"     a = 5;                          \n" +
			"     if (false)  {                   \n" +
			"         a = 6;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 7;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 8;                      \n" +
			"     } else if (true) {              \n" +
			"         a = 9;                      \n" +
			"         if (false) {                \n" +
			"             a = 10;                 \n" +
			"         } else {                    \n" +
			"             if (false) {            \n" +
			"                 a = 11;             \n" +
			"             } else {                \n" +
			"                 if(true) {          \n" +
			"                    a = 12;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 13;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 14;          \n" +
			"                 } else {            \n" +
			"                    a = 15;          \n" +
			"                 }                   \n" +
			"             }                       \n" +
			"         }                           \n" +
			"     } else if (false) {             \n" +
			"         a = 16;                     \n" +
			"     } else {                        \n" +
			"         a = 17;                     \n" +
			"     }                               \n" +
			" } else {                            \n" +
			"     a = 18;                         \n" +
			" }                                   \n" +
			" a;                                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 12, "very complicated case 2", scriptCode);

		scriptCode =
			" int a = 1;                          \n" +
			" if (false) {                        \n" +
			"     a = 2;                          \n" +
			"     if (true)  {                    \n" +
			"         a = 3;                      \n" +
			"     } else {                        \n" +
			"         a = 4;                      \n" +
			"     }                               \n" +
			" } else if (true) {                  \n" +
			"     a = 5;                          \n" +
			"     if (false)  {                   \n" +
			"         a = 6;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 7;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 8;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 9;                      \n" +
			"         if (false) {                \n" +
			"             a = 10;                 \n" +
			"         } else {                    \n" +
			"             if (false) {            \n" +
			"                 a = 11;             \n" +
			"             } else {                \n" +
			"                 if(true) {          \n" +
			"                    a = 12;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 13;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 14;          \n" +
			"                 } else {            \n" +
			"                    a = 15;          \n" +
			"                 }                   \n" +
			"             }                       \n" +
			"         }                           \n" +
			"     } else if (true) {              \n" +
			"         a = 16;                     \n" +
			"     } else {                        \n" +
			"         a = 17;                     \n" +
			"     }                               \n" +
			" } else {                            \n" +
			"     a = 18;                         \n" +
			" }                                   \n" +
			" a;                                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 16, "very complicated case 3", scriptCode);

		scriptCode =
			" int a = 1;                          \n" +
			" if (false) {                        \n" +
			"     a = 2;                          \n" +
			"     if (true)  {                    \n" +
			"         a = 3;                      \n" +
			"     } else {                        \n" +
			"         a = 4;                      \n" +
			"     }                               \n" +
			" } else if (true) {                  \n" +
			"     a = 5;                          \n" +
			"     if (false)  {                   \n" +
			"         a = 6;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 7;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 8;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 9;                      \n" +
			"         if (false) {                \n" +
			"             a = 10;                 \n" +
			"         } else {                    \n" +
			"             if (false) {            \n" +
			"                 a = 11;             \n" +
			"             } else {                \n" +
			"                 if(true) {          \n" +
			"                    a = 12;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 13;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 14;          \n" +
			"                 } else {            \n" +
			"                    a = 15;          \n" +
			"                 }                   \n" +
			"             }                       \n" +
			"         }                           \n" +
			"     } else if (false) {             \n" +
			"         a = 16;                     \n" +
			"     } else {                        \n" +
			"         a = 17;                     \n" +
			"     }                               \n" +
			" } else {                            \n" +
			"     a = 18;                         \n" +
			" }                                   \n" +
			" a;                                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 17, "very complicated case 4", scriptCode);

		scriptCode =
			" int a = 1;                          \n" +
			" if (true) {                         \n" +
			"     a = 2;                          \n" +
			"     if (true)  {                    \n" +
			"         a = 3;                      \n" +
			"     } else {                        \n" +
			"         a = 4;                      \n" +
			"     }                               \n" +
			" } else if (true) {                  \n" +
			"     a = 5;                          \n" +
			"     if (false)  {                   \n" +
			"         a = 6;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 7;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 8;                      \n" +
			"     } else if (false) {             \n" +
			"         a = 9;                      \n" +
			"         if (false) {                \n" +
			"             a = 10;                 \n" +
			"         } else {                    \n" +
			"             if (false) {            \n" +
			"                 a = 11;             \n" +
			"             } else {                \n" +
			"                 if(true) {          \n" +
			"                    a = 12;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 13;          \n" +
			"                 } else if (false) { \n" +
			"                    a = 14;          \n" +
			"                 } else {            \n" +
			"                    a = 15;          \n" +
			"                 }                   \n" +
			"             }                       \n" +
			"         }                           \n" +
			"     } else if (true) {              \n" +
			"         a = 16;                     \n" +
			"     } else {                        \n" +
			"         a = 17;                     \n" +
			"     }                               \n" +
			" } else {                            \n" +
			"     a = 18;                         \n" +
			" }                                   \n" +
			" a;                                  \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 3, "very complicated case 5", scriptCode);
	}
}
