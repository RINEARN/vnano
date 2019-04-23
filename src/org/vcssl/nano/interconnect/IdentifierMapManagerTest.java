/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IdentifierMapManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testConstructor() {
		// staticメソッドだけしかないものの、一応コンストラクタを呼んでも落ちない事のテスト
		// （他のテストで一度も呼ばないので）
		new IdentifierMapManager();
	}


	@Test
	public void testPutToMap() {

		// 操作対象のマップを用意
		LinkedHashMap<String, LinkedList<String>> map = new LinkedHashMap<String, LinkedList<String>>();

		// 要素を追加
		IdentifierMapManager.putToMap(map, "a", "A0");
		IdentifierMapManager.putToMap(map, "b", "B0");
		IdentifierMapManager.putToMap(map, "b", "B1");
		IdentifierMapManager.putToMap(map, "c", "C0");
		IdentifierMapManager.putToMap(map, "c", "C1");
		IdentifierMapManager.putToMap(map, "c", "C2");

		// 指定したキーで要素が登録されているか確認
		assertTrue(map.containsKey("a"));
		assertTrue(map.containsKey("b"));
		assertTrue(map.containsKey("c"));
		assertFalse(map.containsKey("d"));
		assertFalse(map.containsKey("e"));

		// キー「a」の要素の内容を確認
		assertEquals(1, map.get("a").size());
		assertEquals("A0", map.get("a").get(0));

		// キー「b」の要素の内容を確認
		assertEquals(2, map.get("b").size());
		assertEquals("B0", map.get("b").get(0));
		assertEquals("B1", map.get("b").get(1));

		// キー「c」の要素の内容を確認
		assertEquals(3, map.get("c").size());
		assertEquals("C0", map.get("c").get(0));
		assertEquals("C1", map.get("c").get(1));
		assertEquals("C2", map.get("c").get(2));
	}


	@Test
	public void testGetFromMap() {

		// 操作対象のマップを用意
		LinkedHashMap<String, LinkedList<String>> map = new LinkedHashMap<String, LinkedList<String>>();

		// 要素を追加
		IdentifierMapManager.putToMap(map, "a", "A0");
		IdentifierMapManager.putToMap(map, "b", "B0");
		IdentifierMapManager.putToMap(map, "b", "B1");
		IdentifierMapManager.putToMap(map, "c", "C0");
		IdentifierMapManager.putToMap(map, "c", "C1");
		IdentifierMapManager.putToMap(map, "c", "C2");

		// 各の要素を取得して検査（重複キーで複数の要素がある場合、最後の要素を取得するはず）
		assertEquals("A0", IdentifierMapManager.getLastFromMap(map, "a"));
		assertEquals("B1", IdentifierMapManager.getLastFromMap(map, "b"));
		assertEquals("C2", IdentifierMapManager.getLastFromMap(map, "c"));
	}


	@Test
	public void testRemoveFromMap() {

		// 操作対象のマップを用意
		LinkedHashMap<String, LinkedList<String>> map = new LinkedHashMap<String, LinkedList<String>>();

		// 要素を追加
		IdentifierMapManager.putToMap(map, "a", "A0");
		IdentifierMapManager.putToMap(map, "b", "B0");
		IdentifierMapManager.putToMap(map, "b", "B1");
		IdentifierMapManager.putToMap(map, "c", "C0");
		IdentifierMapManager.putToMap(map, "c", "C1");
		IdentifierMapManager.putToMap(map, "c", "C2");

		// 要素を削除（重複キーで複数の要素がある場合、最後の要素が削除されるはず）
		IdentifierMapManager.removeLastFromMap(map, "a");
		IdentifierMapManager.removeLastFromMap(map, "b");
		IdentifierMapManager.removeLastFromMap(map, "c");

		// 各キーでの要素の有無を検査
		assertFalse(map.containsKey("a")); // キー「 a 」の要素は1個だけだったので、削除でマップから消えているはず
		assertTrue(map.containsKey("b")); // キー「 b 」、「 c 」はまだあるはず
		assertTrue(map.containsKey("c"));


		// キー「b」の要素の内容を確認
		assertEquals(1, map.get("b").size());
		assertEquals("B0", map.get("b").get(0));

		// キー「c」の要素の内容を確認
		assertEquals(2, map.get("c").size());
		assertEquals("C0", map.get("c").get(0));
		assertEquals("C1", map.get("c").get(1));
	}

}
