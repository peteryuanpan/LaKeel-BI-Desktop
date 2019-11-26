package com.legendapl.lightning.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleStringProperty;

public class ParameterTableRecord extends RecursiveTreeObject<ParameterTableRecord> {

	/** パラメーター名 */
	private SimpleStringProperty paramName = new SimpleStringProperty("");
	/** パラメーターの値 */
	private SimpleStringProperty paramValue = new SimpleStringProperty("");

	public SimpleStringProperty getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName.set(paramName);
	}

	public SimpleStringProperty getParamValue() {
		return paramValue;
	}

	public void setParamValue(Object paramValue) {
		this.paramValue.set(String.valueOf(paramValue));
	}

}
