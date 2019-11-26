package com.legendapl.lightning.adhoc.common;

/**
 *　フィルタ　タイプ
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.03.07
 */
public enum FilterType {
	NUMBER(0), STRING(1), DATE(2), BOOLEAN(3), TIME(4), TIMESTAMP(5), UNKNOW(6);

	private int index;

	FilterType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
