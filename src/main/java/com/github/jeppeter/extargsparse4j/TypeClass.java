package com.github.jeppeter.extargsparse4j;

import com.github.jeppeter.extargsparse4j.KeyException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class TypeClass {
	private String m_typename;
	public TypeClass(Object o) throws KeyException {
		if ( o instanceof String) {
			this.m_typename = "string";
		} else if (o instanceof Long) {
			this.m_typename = "long";
		} else if (o instanceof JSONObject) {
			this.m_typename = "dict";
		} else if (o instanceof JSONArray) {
			this.m_typename = "list";
		} else if (o instanceof Boolean) {
			this.m_typename = "bool";
		} else if (o instanceof Double ) {
			this.m_typename = "float";
		}  else if (o == null) {
			this.m_typename = "string";
		} else {
			throw new KeyException(String.format("unknown type %s", o.getClass().getName()));
		}
	}

	public String get_type() {
		return this.m_typename;
	}
}
