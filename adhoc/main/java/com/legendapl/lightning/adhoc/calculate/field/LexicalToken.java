package com.legendapl.lightning.adhoc.calculate.field;

import java.util.HashMap;

public class LexicalToken {

	private int type;

	private Object value;

	public LexicalToken(int type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "LexicalToken [type=" + type + ", value=" + value + "]";
	}

	public static void main(String[] args) {
		LexicalParser parser = new LexicalParser(new HashMap<>());
		parser.lexicalParse("NOT");
	}

}
