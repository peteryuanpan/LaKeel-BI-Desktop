package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.dao.util.CalculateListMap;

/**
 * クロス集計の数字の値を格納して、フォーマットする
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.05.08
 */
public class CalculatedNumber implements Cloneable {

	private CrossTableField field;

	private List<Object> numbers = new ArrayList<>();

	public CalculatedNumber(Object value, CrossTableField field) {
		this.field = field;
		numbers.add(value);
	}

	public CalculatedNumber() {

	}

	public void add(CalculatedNumber other) {
		numbers.addAll(other.getNumbers());
	}

	public String format() {
		return CalculateListMap.getCalculatedCTValue(field, numbers);
	}

	public List<Object> getNumbers() {
		return numbers;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		CalculatedNumber cloneFormat = new CalculatedNumber();
		cloneFormat.numbers = new ArrayList<>(this.numbers);
		cloneFormat.field = this.field;
		return cloneFormat;
	}
}
