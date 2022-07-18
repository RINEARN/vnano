/*
 * Copyright(C) 2019-2022 RINEARN
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
		new IdentifierMapManager();
	}


	@Test
	public void testPutToMap() {

		// Create an Map instance for testing.
		LinkedHashMap<String, LinkedList<String>> map = new LinkedHashMap<String, LinkedList<String>>();

		// Put values.
		IdentifierMapManager.putToMap(map, "a", "A0");
		IdentifierMapManager.putToMap(map, "b", "B0");
		IdentifierMapManager.putToMap(map, "b", "B1");
		IdentifierMapManager.putToMap(map, "c", "C0");
		IdentifierMapManager.putToMap(map, "c", "C1");
		IdentifierMapManager.putToMap(map, "c", "C2");

		// Test that above keys are registered to the Map.
		assertTrue(map.containsKey("a"));
		assertTrue(map.containsKey("b"));
		assertTrue(map.containsKey("c"));
		assertFalse(map.containsKey("d"));
		assertFalse(map.containsKey("e"));

		// Test the value associated with the key "a".
		assertEquals(1, map.get("a").size());
		assertEquals("A0", map.get("a").get(0));

		// Test the value associated with the key "b".
		assertEquals(2, map.get("b").size());
		assertEquals("B0", map.get("b").get(0));
		assertEquals("B1", map.get("b").get(1));

		// Test the value associated with the key "c".
		assertEquals(3, map.get("c").size());
		assertEquals("C0", map.get("c").get(0));
		assertEquals("C1", map.get("c").get(1));
		assertEquals("C2", map.get("c").get(2));
	}


	@Test
	public void testGetFromMap() {

		// Create an Map instance for testing.
		LinkedHashMap<String, LinkedList<String>> map = new LinkedHashMap<String, LinkedList<String>>();

		// Put values.
		IdentifierMapManager.putToMap(map, "a", "A0");
		IdentifierMapManager.putToMap(map, "b", "B0");
		IdentifierMapManager.putToMap(map, "b", "B1");
		IdentifierMapManager.putToMap(map, "c", "C0");
		IdentifierMapManager.putToMap(map, "c", "C1");
		IdentifierMapManager.putToMap(map, "c", "C2");

		// Test the values associated with keys.
		assertEquals("A0", IdentifierMapManager.getLastFromMap(map, "a"));
		assertEquals("B1", IdentifierMapManager.getLastFromMap(map, "b"));
		assertEquals("C2", IdentifierMapManager.getLastFromMap(map, "c"));
	}


	@Test
	public void testRemoveFromMap() {

		// Create an Map instance for testing.
		LinkedHashMap<String, LinkedList<String>> map = new LinkedHashMap<String, LinkedList<String>>();

		// Put values.
		IdentifierMapManager.putToMap(map, "a", "A0");
		IdentifierMapManager.putToMap(map, "b", "B0");
		IdentifierMapManager.putToMap(map, "b", "B1");
		IdentifierMapManager.putToMap(map, "c", "C0");
		IdentifierMapManager.putToMap(map, "c", "C1");
		IdentifierMapManager.putToMap(map, "c", "C2");

		// Remove values.
		IdentifierMapManager.removeLastFromMap(map, "a");
		IdentifierMapManager.removeLastFromMap(map, "b");
		IdentifierMapManager.removeLastFromMap(map, "c");

		// Test the values associated with keys.
		assertFalse(map.containsKey("a")); // Only one value had put with the key "a", and we have removed it, so "containsKey" should return false.
		assertTrue(map.containsKey("b")); // Multiple values had put with the key "b", and we have removed the last one, so "containsKey" should return true.
		assertTrue(map.containsKey("c")); // As the same as the key "b", "containsKey" should return true.

		// Test values associated with the key "b".
		assertEquals(1, map.get("b").size());
		assertEquals("B0", map.get("b").get(0));

		// Test values associated with the key "c".
		assertEquals(2, map.get("c").size());
		assertEquals("C0", map.get("c").get(0));
		assertEquals("C1", map.get("c").get(1));
	}

}
