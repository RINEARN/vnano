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
			this.testDeepBlockDepthWhileStatements();
			this.testBreakStatementsInWhileStatements();

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


	private void testDeepBlockDepthWhileStatements() throws VnanoException {
		String scriptCode;
		String result;

		// 注： ループ回数を増やし過ぎると Accelerator 無効時のテストに時間がかかるので注意が必要。
		//      無効時は有効時より数十倍～数百倍遅くなる一方で、デフォルトでは有効になっているので、
		//      標準状態で負荷に余裕があっても、Accelerator 無効時のテストでは重過ぎになり得る。
		//      なので、ループ回数は Accelerator 無効時の負荷を基準に調整すべき。

		scriptCode =
			" int i = 0;                   \n" +
			" int j = 0;                   \n" +
			" int k = 0;                   \n" +
			" int l = 0;                   \n" +
			" int m = 0;                   \n" +
			" int n = 0;                   \n" +
			" while (i < 1) {              \n" +
			"   i++;                       \n" +
			"   j = 0;                     \n" +
			"   while (j < 2) {            \n" +
			"     j++;                     \n" +
			"     k = 0;                   \n" +
			"     while (k < 3) {          \n" +
			"       k++;                   \n" +
			"       l = 0;                 \n" +
			"       while (l < 4) {        \n" +
			"         l++;                 \n" +
			"         m = 0;               \n" +
			"         while (m < 5) {      \n" +
			"           m++;               \n" +
			"           n++;               \n" +
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
			" result += \",m=\" + m;       \n" +
			" result += \",n=\" + n;       \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		long n = 1 * 2 * 3 * 4 * 5;
		super.evaluateResult(result, "i=1,j=2,k=3,l=4,m=5,n=" + n, "while(...){... while(...){... while(...){... while(...){... while(...){...}}}}}", scriptCode);


		scriptCode =
			" int i = 0;                   \n" +
			" int j = 0;                   \n" +
			" int k = 0;                   \n" +
			" int l = 0;                   \n" +
			" int m = 0;                   \n" +
			" while ((i++) < 1) {          \n" +
			"   while ((j++) < 2) {        \n" +
			"     while ((k++) < 3) {      \n" +
			"       while ((l++) < 4) {    \n" +
			"         while ((m++) < 5) {  \n" +
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
		super.evaluateResult(result, "i=2,j=3,k=5,l=7,m=9", "while(i++<1){ while(j++<2){ while(k++<3){ while(l++<4){ while(m++<5){}}}}}", scriptCode);

		// 注： 上の結果は一見するとバグっぽく思えるけれど、バグではない。
		//      （実際に最初はバグかと思って他言語でも確認したところ同じ結果になった。）
		//
		//      バグのように思えるのは、例えば「 while( (m++) < 5 ){... 」の条件から
		//      m は 5 までしか回らないように錯覚してしまう事による。
		//
		//      実際には、while 文の条件式の値が true か false かに関わらず、
		//      条件式の評価処理自体は行われる事に注意（そうでないとそもそも値が決まらない）。
		//
		//      つまり、ループ条件が（結果的に）成立しているかどうかに関わらず、
		//      この「 (m++) < 5 」という式は while の行を踏む度に毎回評価されるので、
		//      例えその結果の値が false であったとしても、m の値は毎回加算される。
		//
		//      従って、「 (m++) < 5 」の式は、m ループ自身の 5 回と、
		//      その外を囲っている l ループの 4 回踏まれるので、
		//      トータルで 5 + 4 = 9 回評価されて、m の値は 9 になる。
		//      同様に l の値は、l ループの 4 回と、その外の k ループの 3 回踏まれて、合わせて 7 になる。
		//      さらに同様に k は 3 + 2 で 5、j は 2 + 1 で 3、i は外にループが無いので 1 になる。
		//      結果としてテストの期待値の "i=2,j=3,k=5,l=7,m=9" が得られる。ので正しい。バグじゃない。


		scriptCode =
			" int i = 0;                   \n" +
			" int j = 0;                   \n" +
			" int k = 0;                   \n" +
			" int l = 0;                   \n" +
			" int m = 0;                   \n" +
			" while ((++i) < 2) {          \n" + // ++i < 1 にすると 1 < 1 になるのでループ内側が1回も回らない
			"   while ((++j) < 3) {        \n" +
			"     while ((++k) < 4) {      \n" +
			"       while ((++l) < 5) {    \n" +
			"         while ((++m) < 6) {  \n" +
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
		super.evaluateResult(result, "i=2,j=3,k=5,l=7,m=9", "while(++i<1){ while(++j<2){ while(++k<3){ while(++l<4){ while(++m<5){}}}}}", scriptCode);
		// 上のテスト値がバグのように思えた場合は、1個前のテスト値に関する長いコメント参照。バグじゃない。
	}


	private void testBreakStatementsInWhileStatements() throws VnanoException {
		String scriptCode;
		String result;

		scriptCode =
			" int i = 0;             \n" +
			" while (i < 10) {       \n" +
			"     i++;               \n" +
			"     if (i == 7) {      \n" +
			"         break;         \n" +
			"     }                  \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=7", "while(...){... break; ...}", scriptCode);

		scriptCode =
			" int i = 0;             \n" +
			" int j = 0;             \n" +
			" while (i < 10) {       \n" +
			"     i++;               \n" +
			"     j = 0;             \n" +
			"     while (j < 10) {   \n" +
			"         j++;           \n" +
			"     }                  \n" +
			"     if (i == 7) {      \n" +
			"         break;         \n" +
			"     }                  \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=7,j=10", "while(...){... while(...){...} ... break; ...}", scriptCode);

		scriptCode =
			" int i = 0;             \n" +
			" int j = 0;             \n" +
			" while (i < 10) {       \n" +
			"     i++;               \n" +
			"     j = 0;             \n" +
			"     while (j < 10) {   \n" +
			"         j++;           \n" +
			"         if (j == 7) {  \n" +
			"             break;     \n" +
			"         }              \n" +
			"     }                  \n" +
			" }                      \n" +
			" string result = \"\";  \n" +
			" result += \"i=\" + i;  \n" +
			" result += \",j=\" + j; \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=10,j=7", "while(...){... while(...){... break; ...}}", scriptCode);

		scriptCode =
			" int i = 0;               \n" +
			" int j = 0;               \n" +
			" while (i < 10) {         \n" +
			"     i++;                 \n" +
			"     j = 0;               \n" +
			"     while (j < 10) {     \n" +
			"         j++;             \n" +
			"         if (j == 7) {    \n" +
			"             break;       \n" +
			"         }                \n" +
			"     }                    \n" +
			"     if (i == 3) {        \n" +
			"         break;           \n" +
			"     }                    \n" +
			" }                        \n" +
			" string result = \"\";    \n" +
			" result += \"i=\" + i;    \n" +
			" result += \",j=\" + j;   \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=3,j=7", "while(...){... while(...){... break; ...} ... break; ...}", scriptCode);

		scriptCode =
			" int i = 0;                            \n" +
			" int j = 0;                            \n" +
			" int k = 0;                            \n" +
			" int l = 0;                            \n" +
			" int m = 0;                            \n" +
			" int n = 0;                            \n" +
			" while (i < 3) {                       \n" +
			"     i++;                              \n" +
			"     j = 0;                            \n" +
			"     while (j < 5) {                   \n" +
			"         j++;                          \n" +
			"         if (j == 4) {                 \n" +
			"             break;                    \n" +
			"         }                             \n" +
			"         k = 0;                        \n" +
			"         while (k < 3) {               \n" +
			"             k++;                      \n" +
			"             l = 0;                    \n" +
			"             while (l < 5) {           \n" +
			"                 l++;                  \n" +
			"                 m = 0;                \n" +
			"                 while (m < 3) {       \n" +
			"                     m++;              \n" +
			"                     n = 0;            \n" +
			"                     while (n < 10) {  \n" +
			"                         n++;          \n" +
			"                         if (n == 8) { \n" +
			"                             break;    \n" +
			"                         }             \n" +
			"                     }                 \n" +
			"                 }                     \n" +
			"                 if (l == 2) {         \n" +
			"                     break;            \n" +
			"                 }                     \n" +
			"             }                         \n" +
			"         }                             \n" +
			"     }                                 \n" +
			" }                                     \n" +
			" string result = \"\";                 \n" +
			" result += \"i=\" + i;                 \n" +
			" result += \",j=\" + j;                \n" +
			" result += \",k=\" + k;                \n" +
			" result += \",l=\" + l;                \n" +
			" result += \",m=\" + m;                \n" +
			" result += \",n=\" + n;                \n" ;

		result = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, "i=3,j=4,k=3,l=2,m=3,n=8", "break from deep block-depth while loops", scriptCode);
	}
}
