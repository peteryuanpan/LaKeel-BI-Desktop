package com.legendapl.lightning.adhoc.custom;

import com.legendapl.lightning.adhoc.common.AdhocUtils;

import javafx.scene.control.Tooltip;

public class CustomTooltip extends Tooltip {
	
	private String text;
	private Property property;

	public CustomTooltip() {
		super();
		this.property = new Property();
	}
	
	public CustomTooltip(String text) {
		super(text);
		this.text = text;
		this.property = new Property();
	}
	
	private void setPropertyText() {
		String context = new String();
		if (null != getProperty().label) {
			if (!context.isEmpty()) context += "\n";
			context += AdhocUtils.getString("P121.tooltip.context.label") + ": " + getProperty().label;
		}
		if (null != getProperty().resourceId) {
			if (!context.isEmpty()) context += "\n";
			context += AdhocUtils.getString("P121.tooltip.context.resouceId") + ": " + getProperty().resourceId;
		}
		if (null != getProperty().modelType) {
			if (!context.isEmpty()) context += "\n";
			context += AdhocUtils.getString("P121.tooltip.context.modelType") + ": " + getProperty().modelType;
		}
		if (null != getProperty().calculateType) {
			if (!context.isEmpty()) context += "\n";
			context += AdhocUtils.getString("P121.tooltip.context.calculateType") + ": " + getProperty().calculateType;
		}
		if (null != getProperty().dataFormat) {
			if (!context.isEmpty()) context += "\n";
			context += AdhocUtils.getString("P121.tooltip.context.dataFormat") + ": " + getProperty().dataFormat;
		}
		if (null != getProperty().groupType) {
			if (!context.isEmpty()) context += "\n";
			context += AdhocUtils.getString("P121.tooltip.context.groupType") + ": " + getProperty().groupType;
		}
		this.setText(context);
	}
	
	public class Property {
		String label;
		String resourceId;
		String modelType;
		String calculateType;
		String dataFormat;
		String groupType;
		public Property() {
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String text) {
			this.label = text;
			setPropertyText();
		}
		public String getResourceId() {
			return resourceId;
		}
		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
			setPropertyText();
		}
		public String getModelType() {
			return modelType;
		}
		public void setModelType(String modelType) {
			this.modelType = modelType;
			setPropertyText();
		}
		public String getCalculateType() {
			return calculateType;
		}
		public void setCalculateType(String calculateType) {
			this.calculateType = calculateType;
			setPropertyText();
		}
		public String getDataFormat() {
			return dataFormat;
		}
		public void setDataFormat(String dataFormat) {
			this.dataFormat = dataFormat;
			setPropertyText();
		}
		public String getGroupType() {
			return groupType;
		}
		public void setGroupType(String groupType) {
			this.groupType = groupType;
			setPropertyText();
		}
	}
	
	public Property getProperty() {
		if (null == property) {
			property = new Property();
		}
		return property;
	}
	
	public void removeProperty() {
		property = new Property();
		this.setText(text);
	}
	
}
