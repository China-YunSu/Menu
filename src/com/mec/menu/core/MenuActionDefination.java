package com.mec.menu.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class MenuActionDefination {
	private Map<String,Method> actionPool;
	private Object object;
	
	MenuActionDefination() {
		actionPool = new HashMap<String,Method>();
	}

	Object getObject() {
		return object;
	}

	void setObject(Object object) {
		this.object = object;
	}
	
	void addMethod(String item, Method method) {
		if (!actionPool.containsKey(item)) {
			actionPool.put(item, method);
		}
	}
	
	Method getMethod(String item) {
		if (actionPool.containsKey(item)) {
			return actionPool.get(item);
		}
		return null;
 	}

}
