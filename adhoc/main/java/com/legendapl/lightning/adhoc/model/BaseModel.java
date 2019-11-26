package com.legendapl.lightning.adhoc.model;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.common.ModelType;

/**
 * ドメイン、トピック、アドホックデータの共通情報
 * 
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.02.26
 */
public abstract class BaseModel {
	
	protected Logger logger = Logger.getLogger(getClass());

	protected static ModelType modelType;

	public ModelType getModelType() {
		return modelType;
	}
	
	// TODO:
	// 共通のアイテムは何ですか?
	
}
