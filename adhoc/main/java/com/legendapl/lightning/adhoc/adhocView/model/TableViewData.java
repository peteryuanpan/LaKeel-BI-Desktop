package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.legendapl.lightning.adhoc.model.Field;

/**
 * テーブルの行のデータ
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.03.19
 */
public class TableViewData {
	/**
	 * 普段の値
	 */

	private Map<Field, Object> field2value = new HashMap<Field, Object>();
	/**
	 * グループの値
	 */
	private List<Object> groupKey;
	private boolean groupContent = false;
	private boolean oddStyle = true;
	private boolean lastRow = false;

	public boolean isOddStyle() {
		return oddStyle;
	}

	public void setOddStyle(boolean oddStyle) {
		this.oddStyle = oddStyle;
	}

	public TableViewData(List<Object> groupKey) {
		this.groupKey = groupKey;
		groupContent = true;
	}

	public TableViewData() {

	}

	public boolean isGroupContent() {
		return groupContent;
	}

	public List<Object> getGroupKey() {
		return groupKey;
	}

	public boolean isLastRow() {
		return lastRow;
	}

	public void setLastRow(boolean lastRow) {
		this.lastRow = lastRow;
	}

	public Object getValueByField(Field field) {
		return field2value.get(field);
	}

	public void addValue(Field field, Object data) {
		field2value.put(field, data);
	}

	public Map<Field, Object> getField2value() {
		return field2value;
	}

}
