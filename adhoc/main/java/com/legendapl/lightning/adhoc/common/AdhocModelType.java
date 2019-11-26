package com.legendapl.lightning.adhoc.common;

public enum AdhocModelType {

	ITEMTREE("P121.ModelType.ITEMTREE"),
	LAYOUT("P121.ModelType.LAYOUT"),
	FILTER("P121.ModelType.FILTER"),
	TABLE("P121.ModelType.TABLE"), 
	CROSSTABLE("P121.ModelType.CROSSTABLE"),
	SIMPLEDATA("P121.ModelType.SIMPLEDATA"), 
	FULLDATA("P121.ModelType.FULLDATA"),
	FIELD("P121.ModelType.FIELD"), 
	MEASURE("P121.ModelType.MEASURE"), 
	LAYOUT_POUND("P121.ModelType.LAYOUT_POUND");
	
	private String name;
	
	private AdhocModelType(String name) {
		this.name = name;
	}
	
	@Override public String toString() {
		try {
			return AdhocUtils.getString(name);
		} catch (Exception e) {
			return null;
		}
	}

	public AdhocModelType getAdhocModelType(String name) {
		for(AdhocModelType modelType : AdhocModelType.values()) {
			if(modelType.name.equals(name)) {
				return modelType;
			}
		}
		return null;
	}
}
