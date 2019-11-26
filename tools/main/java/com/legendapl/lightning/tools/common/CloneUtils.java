package com.legendapl.lightning.tools.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CloneUtils {

	public static <K,V extends Cloneable> HashMap<K, List<V>> clone(HashMap<K, List<V>> obj) {
		HashMap<K, List<V>> clonedObj = new HashMap<K, List<V>>();
		Set<K> keys = obj.keySet();
		for(K key : keys) {
			List<V> list = obj.get(key);
			List<V> clonedList = new ArrayList<V>();
			clonedList.addAll(list);
			clonedObj.put(key, clonedList);
		}
		return clonedObj;
	}
}
