package com.legendapl.lightning.adhoc.dao.util;

import com.legendapl.lightning.adhoc.adhocView.model.CalculatedNumber;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.common.DataFormat;
import com.legendapl.lightning.adhoc.common.GroupType;
import com.legendapl.lightning.adhoc.model.Field;

public class SQLUtils {

	/**
	 * tableId.fieldId --> `tableId`.`fieldId`
	 * fieldId --> `fieldId`
	 */
	public static final String DBsurround(String value) {
		if(value.contains(".")) {
			String[] temp = value.split("\\.");
			return "`" + temp[0] + "`.`" + temp[1] + "`";
		}
		return "`" + value + "`";
	}

	/**
	 * クロス集計のメジャーのIDを設定する
	 */
	public static final CrossTableField uniqueMeasure(CrossTableField field) {
		return field;
	}

	/**
	 * クロス集計のフィールドのIDを設定する
	 */
	public static final String uniqueFieldId(Field field) {
		return field.getId().replace(".", "_");
	}

	public static final String jexlResourceId(String resourceId) {
		return "field:get(\"" + resourceId + "\")";
	}

	public static String getFormatedString(Object result, DataFormat dataFormat) {
		return result == null ? null : dataFormat.parse(result);
	}

	public static String getFormatedString(Object result, GroupType groupType) {
		return result == null ? null : groupType == null ? result.toString() : groupType.parse(result);
	}

	public static CalculatedNumber getNumberFormat(Object ob, CrossTableField field) {
		if (ob == null)
			return null;
		return new CalculatedNumber(ob, field);
	}

	public static String selectAsField (Field field) {
		return DBsurround(uniqueFieldId(field));
	}
}
