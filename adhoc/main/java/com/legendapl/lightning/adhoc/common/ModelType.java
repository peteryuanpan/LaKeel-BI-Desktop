package com.legendapl.lightning.adhoc.common;

/**
 * モデルの種類
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public enum ModelType {
	ADHOC("adhoc"), TOPIC("topic"), DOMAIN("domain");

	private String name;

	private ModelType(String name) {
		this.name = name;
	}


	public ModelType getModelType(String name) {
		for(ModelType modelType : ModelType.values()) {
			if(modelType.name.equals(name)) {
				return modelType;
			}
		}
		return null;
	}
}
