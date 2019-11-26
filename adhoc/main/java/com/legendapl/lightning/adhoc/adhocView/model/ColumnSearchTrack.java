package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.ArrayList;
import java.util.List;

/**
 * CrossTableCustomColumn　のトラックで、行のデータから値を取得するためのキーです
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.08
 */
public class ColumnSearchTrack {
	private List<Object> fieldNames = new ArrayList<>();
	private CrossTableField measure = null;

	public void addAll(ColumnSearchTrack track) {
		fieldNames.addAll(track.fieldNames);
		measure = track.getMeasure();
	}

	public void add(Object fieldName) {
		fieldNames.add(fieldName);
	}

	public List<Object> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(List<Object> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public CrossTableField getMeasure() {
		return measure;
	}

	public void setMeasure(CrossTableField measure) {
		this.measure = measure;
	}

	@Override
	public String toString() {
		return "ColumnSearchTrack [fieldNames=" + fieldNames + ", measure=" + measure + "]";
	}

}
