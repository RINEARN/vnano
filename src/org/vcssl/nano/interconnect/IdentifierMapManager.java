/*
 * Copyright(C) 2019-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class manages I/O to/from a Map, for enabling to store multiple values for each key. 
 */
public class IdentifierMapManager {


	/**
	 * Put a value to the Map.
	 *
	 * @param map The Map to which you want to put a value.
	 * @param key The key, which will be required when you want to get the value.
	 * @param element The value to be put.
	 */
	public static <KeyType, ValueType> void putToMap(
			Map<KeyType, LinkedList<ValueType>> map, KeyType key, ValueType element) {

		if (!map.containsKey(key)) {
			map.put(key, new LinkedList<ValueType>());
		}
		map.get(key).add(element);
	}


	/**
	 * Gets the last-put value with the specified key, from the Map.
	 * 
	 * @param map The map storing the specified key/value.
	 * @param key The key specified when you had put the value.
	 * @return The last-put value with the specified key.
	 */
	public static <KeyType, ValueType> ValueType getLastFromMap(
			Map<KeyType, LinkedList<ValueType>> map, KeyType key) {

		return map.get(key).getLast();
	}


	/**
	 * Get all values put with the specified key, from the Map.
	 * 
	 * @param map The Map storing values to be gotten.
	 * @param key The key specified when you had put the values to be gotten.
	 * @return All values put with the specified key.
	 */
	public static <KeyType, ValueType> List<ValueType> getAllFromMap(
			Map<KeyType, LinkedList<ValueType>> map, KeyType key) {

		return map.get(key);
	}

	/**
	 * Removes the last-put value with the specified key, from the Map.
	 *
	 * @param map The map from which you want to remove the value.
	 * @param key The key specified when you had put the value.
	 */
	public static <KeyType, ValueType> void removeLastFromMap(
			Map<KeyType, LinkedList<ValueType>> map, KeyType key) {

		map.get(key).removeLast();
		if (map.get(key).size() == 0) {
			map.remove(key);
		}
	}

}
