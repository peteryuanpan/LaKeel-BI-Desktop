package com.legendapl.lightning.adhoc.common;

/**
 * サマリ計算の変更
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.27
 */
public enum CalculateType {

	None("P121.crossTable.None", "None", 0),
	Average("P121.crossTable.Average", "Average", 1),
	CountAll("P121.crossTable.CountAll", "CountAll", 2),
	CountDistinct("P121.crossTable.CountDistinct", "CountDistinct", 3),
	Maximum("P121.crossTable.Maximum", "Maximum", 4),
	Median("P121.crossTable.Median", "Median", 5),
	Minimum("P121.crossTable.Minimum", "Minimum", 6),
	Sum("P121.crossTable.Sum", "Sum", 7);

	private String name;
	private String defaultCalculateType;
	private Integer index;

	CalculateType(String name, String defaultCalculateType, Integer index) {
		this.name = name;
		this.defaultCalculateType = defaultCalculateType;
		this.index = index;
	}

	@Override public String toString() {
		try {
			return AdhocUtils.getString(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDefaultCalculateType() {
		return defaultCalculateType;
	}

	public Integer getIndex() {
		return index;
	}
	
	public String getId() {
		return index.toString();
	}
	
	public static CalculateType getCalculateType(String name) {
		for (CalculateType type : CalculateType.values()) {
			if (null != type.name && type.name.equals(name)) {
				return type;
			}
		}
		return null;
	}

}
