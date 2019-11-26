package com.legendapl.lightning.adhoc.common;

public enum ViewType {
	
	BOTH("both", null),
	LEFT("left", null),
	RIGHT("right", null),
	TOP("top", "Dimension"),
	BOTTOM("bottom", "Measure");
	
	private String name;
	private String defaultViewType;
	
	private ViewType(String name, String defaultViewType) {
		this.name = name;
		this.defaultViewType = defaultViewType;
	}

	public static ViewType getViewType(String name) {
		for (ViewType type : ViewType.values()) {
			if (null != type.name && type.name.equals(name)) {
				return type;
			}
		}
		return null;
	}
	
	public static ViewType getViewType(Object name, String defaultViewType) {
		for (ViewType type : ViewType.values()) {
			if (null != type.defaultViewType && type.defaultViewType.equals(defaultViewType)) {
				return type;
			}
		}
		return null;
	}
	
}
