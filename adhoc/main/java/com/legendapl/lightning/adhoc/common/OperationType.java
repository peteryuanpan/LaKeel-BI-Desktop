package com.legendapl.lightning.adhoc.common;

/**
 *　フィルタの操作タイプ
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public enum OperationType {
	
	isOneOf("P112.filter.isOneOf", 0), isNotOneOf("P112.filter.isNotOneOf", 1),
    equals("P112.filter.equals", 2), isNotEqualTo("P112.filter.doesNotEqualTo", 3),
    contains("P112.filter.contains", 4), doesNotContain("P112.filter.doesNotContain", 5),
    startsWith("P112.filter.startsWith", 6), doesNotStartWith("P112.filter.doesNotStartWith", 7),
    endsWith("P112.filter.endsWith", 8), doesNotEndWith("P112.filter.doesNotEndWith", 9),
    isGreaterThan("P112.filter.isGreaterThan", 10), lessThan("P112.filter.lessThan", 11),
    isGreaterThanOrEqualTo("P112.filter.isGreaterThanOrEqualTo", 12),
    isLessThanOrEqualTo("P112.filter.isLessThanOrEqualTo", 13),
    isBetween("P112.filter.isBetween", 14), isNotBetween("P112.filter.isNotBetween", 15),
    isAfter("P112.filter.isAfter", 16), isBefore("P112.filter.isBefore", 17),
    isOnOrAfter("P112.filter.isOnOrAfter", 18), isOnOrBefore("P112.filter.isOnOrBefore", 19);

	private final String name;
	private int index;

	OperationType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	@Override
	public String toString() {
		return AdhocUtils.getString(name);
	}

	public int getIndex() {
		return index;
	}
}
