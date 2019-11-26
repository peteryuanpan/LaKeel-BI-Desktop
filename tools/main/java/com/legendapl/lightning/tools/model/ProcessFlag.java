package com.legendapl.lightning.tools.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.legendapl.lightning.tools.common.Constants;

/**
 * 操作フラグ
 *
 * @author LAC_楊
 * @since 2017/9/11
 */
public enum ProcessFlag {

	/**
	 * 未定義
	 */
	NONE(Integer.MIN_VALUE, "None", Constants.CSV_FLAG_NONE),
	
	/**
	 * 追加
	 */
	ADD(1, "Add", Constants.CSV_FLAG_ADD),

	/**
	 * 更新
	 */
	UPDATE(0, "Update", Constants.CSV_FLAG_UPDATE),

	/**
	 * 削除
	 */
	DELETE(-1, "Delete", Constants.CSV_FLAG_DELETE),
	
	/**
	 * 更新または追加
	 */
	UPDATEORADD(2, "UpdateOrAdd", Constants.CSV_FLAG_UPDATE_OR_ADD);
	

	/**
	 * 未定義のvalue
	 */
	public static final int NONE_VALUE = Integer.MIN_VALUE;
	
	/**
	 * 追加のvalue
	 */
	public static final int ADD_VALUE = 1;

	/**
	 * 更新のvalue
	 */
	public static final int UPDATE_VALUE = 0;

	/**
	 * 追加のvalue
	 */
	public static final int DELETE_VALUE = -1;
	
	/**
	 * 更新または追加のvalue
	 */
	public static final int UPDATEORADD_VALUE = 2;

	/**
	 * すべての操作フラグ
	 */
	private static final ProcessFlag[] VALUES_ARRAY =
		new ProcessFlag[] {
			NONE,
			ADD,
			UPDATE,
			DELETE,
			UPDATEORADD,
		};

	/**
	 * すべての操作フラグ
	 */
	public static final List<ProcessFlag> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * 操作フラグ取得(by literal)
	 */
	public static ProcessFlag get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ProcessFlag result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * 操作フラグ取得(by Name)
	 */
	public static ProcessFlag getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ProcessFlag result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * 操作フラグ取得(by value)
	 */
	public static ProcessFlag get(int value) {
		switch (value) {
			case NONE_VALUE: return NONE;
			case ADD_VALUE: return ADD;
			case UPDATE_VALUE: return UPDATE;
			case UPDATEORADD_VALUE: return UPDATEORADD;
			case DELETE_VALUE: return DELETE;
		}
		return null;
	}

	private final int value;

	private final String name;

	private final String literal;

	private ProcessFlag(int value, String name, String literal) {
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
