package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ForStatementCombinedTest extends CombinedTestElement {

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
			this.testForStatements();
			this.testMultipleForStatements();
			this.testDeepBlockForLoops();
			this.testBreakStatementsInForLoops();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testForStatements() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int i = 123;           \n" +
			" for (i=0; i<10; i++) { \n" +
			" }                      \n" +
			" i;                     \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=123; for(i=0;i<10;i++){}", scriptCode);

		scriptCode =
			" int i = 123;           \n" +
			" for (i=0; i>10; i++) { \n" +
			" }                      \n" +
			" i;                     \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 0, "i=0; for(i=0;i>10;i++){}", scriptCode);

		scriptCode =
			" int a = 123;               \n" +
			" for (int i=0; i<10; i++) { \n" +
			"     a++;                   \n" +
			" }                          \n" +
			" a;                         \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 133, "a=123; for(int i=0;i<10;i++){a++;}", scriptCode);

		scriptCode =
			" int a = 123;               \n" +
			" for (int i=0; i>10; i++) { \n" +
			"     a++;                   \n" +
			" }                          \n" +
			" a;                         \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 123, "a=123; for(int i=0;i>10;i++){a++;}", scriptCode);

		scriptCode =
			" int i = 0;     \n" +
			" for (;i<10;) { \n" +
			"     i++;       \n" +
			" }              \n" +
			" i;             \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=0; for(;i<10;){ i++; }", scriptCode);

		scriptCode =
			" int i = 0;         \n" +
			" for (;(i++)<10;) { \n" +
			" }                  \n" +
			" i;                 \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 11, "i=0; for(;(i++)<10;){}", scriptCode);
		// 後置インクリメントは値を読んだ後に加算されるので、
		// iが10の時に条件式が評価されて10<10の判定になってループを抜けつつ、
		// i は加算されて最終的に11になる。

		scriptCode =
			" int i = 0;         \n" +
			" for (;(++i)<10;) { \n" +
			" }                  \n" +
			" i;                 \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=0; for(;(++i)<10;){}", scriptCode);
		// 前置インクリメントは値を読む前に加算されるので、
		// iが9の時に条件式が評価されて、加算された後に10<10の判定になってループを抜ける。
		// なので脱出時の i は10。
	}

	private void testMultipleForStatements() throws VnanoException {
		String scriptCode;
		String result;

		scriptCode =
			" int i;                 \n" +
			" int j;                 \n" +
			" int k;                 \n" +
			" int l;                 \n" +
			" int m;                 \n" +
			" for (i=0; i<10; i++) { \n" +
			" }                      \n" +
			" for (j=0; j<20; j++) { \n" +
			" }                      \n" +
			" for (k=0; k<30; k++) { \n" +
			" }                      \n" +
			" for (l=0; l<40; l++) { \n" +
			" }                      \n" +
			" for (m=0; m<50; m++) { \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" +
			" result += \",k=\" + k; \n" +
			" result += \",l=\" + l; \n" +
			" result += \",m=\" + m; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=10,j=20,k=30,l=40,m=50", "for(x<y){...} x 5", scriptCode);

		scriptCode =
			" int i;                 \n" +
			" int j;                 \n" +
			" int k;                 \n" +
			" int l;                 \n" +
			" int m;                 \n" +
			" for (i=0; (i++)<10;) { \n" +
			" }                      \n" +
			" for (j=0; (j++)<20;) { \n" +
			" }                      \n" +
			" for (k=0; (k++)<30;) { \n" +
			" }                      \n" +
			" for (l=0; (l++)<40;) { \n" +
			" }                      \n" +
			" for (m=0; (m++)<50;) { \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" +
			" result += \",k=\" + k; \n" +
			" result += \",l=\" + l; \n" +
			" result += \",m=\" + m; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=11,j=21,k=31,l=41,m=51", "for((x++)<y){...} x 5", scriptCode);

		scriptCode =
			" int i;                 \n" +
			" int j;                 \n" +
			" int k;                 \n" +
			" int l;                 \n" +
			" int m;                 \n" +
			" for (i=0; (++i)<10;) { \n" +
			" }                      \n" +
			" for (j=0; (++j)<20;) { \n" +
			" }                      \n" +
			" for (k=0; (++k)<30;) { \n" +
			" }                      \n" +
			" for (l=0; (++l)<40;) { \n" +
			" }                      \n" +
			" for (m=0; (++m)<50;) { \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" +
			" result += \",k=\" + k; \n" +
			" result += \",l=\" + l; \n" +
			" result += \",m=\" + m; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=10,j=20,k=30,l=40,m=50", "for((++x)<y){...} x 5", scriptCode);
	}


	private void testDeepBlockForLoops() throws VnanoException {
		String scriptCode;
		String result;

		// 注： ループ回数を増やし過ぎると Accelerator 無効時のテストに時間がかかるので注意が必要。
		//      無効時は有効時より数十倍～数百倍遅くなる一方で、デフォルトでは有効になっているので、
		//      標準状態で負荷に余裕があっても、Accelerator 無効時のテストでは重過ぎになり得る。
		//      なので、ループ回数は Accelerator 無効時の負荷を基準に調整すべき。

		scriptCode =
			" int i;                         \n" +
			" int j;                         \n" +
			" int k;                         \n" +
			" int l;                         \n" +
			" int m;                         \n" +
			" int n;                         \n" +
			" for (i=0; i<1; i++) {          \n" +
			"   for (j=0; j<2; j++) {        \n" +
			"     for (k=0; k<3; k++) {      \n" +
			"       for (l=0; l<4; l++) {    \n" +
			"         for (m=0; m<5; m++) {  \n" +
			"           n++;                 \n" +
			"         }                      \n" +
			"       }                        \n" +
			"     }                          \n" +
			"   }                            \n" +
			" }                              \n" +
			" string result = \"\";          \n" +
			" result += \"i=\" + i;          \n" +
			" result += \",j=\" + j;         \n" +
			" result += \",k=\" + k;         \n" +
			" result += \",l=\" + l;         \n" +
			" result += \",m=\" + m;         \n" +
			" result += \",n=\" + n;         \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		long n = 1 * 2 * 3 * 4 * 5;
		super.evaluateResult(result, "i=1,j=2,k=3,l=4,m=5,n=" + n, "for(...){ for(...){ for(...){ for(...){ for(...){ n++; }}}}}", scriptCode);

		scriptCode =
			" int i = 0;                   \n" +
			" int j = 0;                   \n" +
			" int k = 0;                   \n" +
			" int l = 0;                   \n" +
			" int m = 0;                   \n" +
			" for (; (i++)<1; ) {          \n" +
			"   for (; (j++)<2; ) {        \n" +
			"     for (; (k++)<3; ) {      \n" +
			"       for (; (l++)<4; ) {    \n" +
			"         for (; (m++)<5; ) {  \n" +
			"         }                    \n" +
			"       }                      \n" +
			"     }                        \n" +
			"   }                          \n" +
			" }                            \n" +
			" string result = \"\";        \n" +
			" result += \"i=\" + i;        \n" +
			" result += \",j=\" + j;       \n" +
			" result += \",k=\" + k;       \n" +
			" result += \",l=\" + l;       \n" +
			" result += \",m=\" + m;       \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=2,j=3,k=5,l=7,m=9", "for(;i++<1;){ for(;j++<2;){ for(;k++<3;){ for(;l++<4;){ for(;m++<5;){ }}}}}", scriptCode);

		// 注： 上の結果は一見するとバグっぽく思えるけれど、バグではない。
		//      同パッケージ内にある WhileStatementCombinedTest クラスの
		//      testDeepBlockDepthWhileLoops メソッド内コメント参照

		scriptCode =
			" int i = 0;                   \n" +
			" int j = 0;                   \n" +
			" int k = 0;                   \n" +
			" int l = 0;                   \n" +
			" int m = 0;                   \n" +
			" for (; (++i)<2; ) {          \n" + // ++i < 1 にすると 1 < 1 になるのでループ内側が1回も回らない
			"   for (; (++j)<3; ) {        \n" +
			"     for (; (++k)<4; ) {      \n" +
			"       for (; (++l)<5; ) {    \n" +
			"         for (; (++m)<6; ) {  \n" +
			"         }                    \n" +
			"       }                      \n" +
			"     }                        \n" +
			"   }                          \n" +
			" }                            \n" +
			" string result = \"\";        \n" +
			" result += \"i=\" + i;        \n" +
			" result += \",j=\" + j;       \n" +
			" result += \",k=\" + k;       \n" +
			" result += \",l=\" + l;       \n" +
			" result += \",m=\" + m;       \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=2,j=3,k=5,l=7,m=9", "for(;++i<2;){ for(;++j<3;){ for(;++k<4;){ for(;++l<5;){ for(;++m<6;){ }}}}}", scriptCode);

		// 上のテスト値がバグのように思えた場合は、1個前のテスト値に関するコメント参照。バグじゃない。
	}


	private void testBreakStatementsInForLoops() throws VnanoException {
		String scriptCode;
		String result;

		scriptCode =
			" int i;                  \n" +
			" for (i=0; i<10; i++) {  \n" +
			"     if (i == 7) {       \n" +
			"         break;          \n" +
			"     }                   \n" +
			" }                       \n" +
			" string result = \"\";   \n" +
			" result += \"i=\" + i;   \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=7", "for(...){... break; ...}", scriptCode);

		scriptCode =
			" int i;                      \n" +
			" int j;                      \n" +
			" for (i=0; i<10; i++) {      \n" +
			"     for (j=0; j<10; j++) {  \n" +
			"     }                       \n" +
			"     if (i == 7) {           \n" +
			"         break;              \n" +
			"     }                       \n" +
			" }                           \n" +
			" string result = \"\";       \n" +
			" result += \"i=\" + i;       \n" +
			" result += \",j=\" + j;      \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=7,j=10", "for(...){ for(...){ } ... break; ...}", scriptCode);

		scriptCode =
			" int i;                      \n" +
			" int j;                      \n" +
			" for (i=0; i<10; i++) {      \n" +
			"     for (j=0; j<10; j++) {  \n" +
			"         if (j == 7) {       \n" +
			"             break;          \n" +
			"         }                   \n" +
			"     }                       \n" +
			" }                           \n" +
			" string result = \"\";       \n" +
			" result += \"i=\" + i;       \n" +
			" result += \",j=\" + j;      \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=10,j=7", "for(...){ for(...){... break; ...}}", scriptCode);

		scriptCode =
			" int i;                                      \n" +
			" int j;                                      \n" +
			" int k;                                      \n" +
			" int l;                                      \n" +
			" int m;                                      \n" +
			" int n;                                      \n" +
			" for (i=0; i<3; i++) {                       \n" +
			"     for (j=0; j<5; j++) {                   \n" +
			"         if (j == 4) {                       \n" +
			"             break;                          \n" +
			"         }                                   \n" +
			"         for (k=0; k<3; k++) {               \n" +
			"             for (l=0; l<5; l++) {           \n" +
			"                 for (m=0; m<3; m++) {       \n" +
			"                     for (n=0; n<10; n++) {  \n" +
			"                         if (n == 8) {       \n" +
			"                             break;          \n" +
			"                         }                   \n" +
			"                     }                       \n" +
			"                 }                           \n" +
			"                 if (l == 2) {               \n" +
			"                     break;                  \n" +
			"                 }                           \n" +
			"             }                               \n" +
			"         }                                   \n" +
			"     }                                       \n" +
			" }                                           \n" +
			" string result = \"\";                       \n" +
			" result += \"i=\" + i;                       \n" +
			" result += \",j=\" + j;                      \n" +
			" result += \",k=\" + k;                      \n" +
			" result += \",l=\" + l;                      \n" +
			" result += \",m=\" + m;                      \n" +
			" result += \",n=\" + n;                      \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=3,j=4,k=3,l=2,m=3,n=8", "very complicated combinations of break-statements and deep for-loops", scriptCode);
	}
}
