package org.kvj.quebec4.ui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.os.Build;

public class APIResolver {

	private static Map<String, Map<Integer, Class<?>>> data = new HashMap<String, Map<Integer, Class<?>>>();

	public static void addVersion(int version, Class<?> cl, Class<?> value) {
		Map<Integer, Class<?>> map = data.get(cl.getName());
		if (null == map) {
			map = new LinkedHashMap<Integer, Class<?>>();
			data.put(cl.getName(), map);
		}
		map.put(version, value);
	}

	public Class<?> getClass(Class<?> cl) {
		Map<Integer, Class<?>> map = data.get(cl.getName());
		if (null == map) {
			return cl;
		}
		int api = Build.VERSION.SDK_INT;
		for (Integer v : map.keySet()) {
			if (v >= api) {
				return map.get(v);
			}
		}
		return cl;
	}
}
