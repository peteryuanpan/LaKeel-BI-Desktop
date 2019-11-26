package com.legendapl.lightning.tools.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.legendapl.lightning.tools.common.Constants;

public enum PermissionEnum {

	/**
	 * 未定義
	 */
	NOTACCESS(0, "Not Access", Constants.P84_NOT_ACCESS),

	ADMINISTER(1, "Administer", Constants.P84_ADMISITER),

	READONLY(2, "Read Only", Constants.P84_READ_ONLY),

	READWRITE(6, "Read and Write", Constants.P84_READ_WRITE),

	READDELETE(18, "Read and Delete", Constants.P84_READ_DELETE),

	READWRITEDELETE(30, "Read, Write and Delete", Constants.P84_READ_WRITE_DELETE),

	EXECUTEONLY(32, "Execute Only", Constants.P84_EXECUTE_ONLY);


	/**
	 * 未定義のvalue
	 */
	public static final int NOTACCESS_VALUE = 0;

	public static final int ADMINISTER_VALUE = 1;

	public static final int READONLY_VALUE = 2;

	public static final int READWRITE_VALUE = 6;

	public static final int READDELETE_VALUE = 18;

	public static final int READWRITEDELETE_VALUE = 30;

	public static final int EXECUTEONLY_VALUE = 32;


	/**
	 * すべての操作フラグ
	 */
	private static final PermissionEnum[] VALUES_ARRAY =
		new PermissionEnum[] {
			NOTACCESS,
			ADMINISTER,
			READONLY,
			READWRITE,
			READDELETE,
			READWRITEDELETE,
			EXECUTEONLY,
		};

	/**
	 * すべての操作フラグ
	 */
	public static final List<PermissionEnum> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * 操作フラグ取得(by literal)
	 */
	public static PermissionEnum get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			PermissionEnum result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * 操作フラグ取得(by Name)
	 */
	public static PermissionEnum getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			PermissionEnum result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * 操作フラグ取得(by value)
	 */
	public static PermissionEnum get(int value) {
		switch (value) {
			case NOTACCESS_VALUE: return NOTACCESS;
			case ADMINISTER_VALUE: return ADMINISTER;
			case READONLY_VALUE: return READONLY;
			case READWRITEDELETE_VALUE: return READWRITEDELETE;
			case READWRITE_VALUE: return READWRITE;
			case EXECUTEONLY_VALUE: return EXECUTEONLY;
			case READDELETE_VALUE: return READDELETE;
		}
		return null;
	}

	private final int value;

	private final String name;

	private final String literal;

	private PermissionEnum(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	public int getValue() {
	  return value;
	}

	public String getName() {
	  return name;
	}

	public String getLiteral() {
	  return literal;
	}

	@Override
	public String toString() {
		return literal;
	}
}