package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.CalculateType;
import com.legendapl.lightning.adhoc.common.DataFormat;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;

@XmlType(propOrder = {"modelType", "calculateType", "dataFormat"})
public abstract class AdhocField extends Field {

	protected AdhocModelType modelType;
	protected CalculateType calculateType;
	protected DataFormat dataFormat;
	
	public AdhocField() {
		super();
	}

	public AdhocField(Field field, AdhocModelType modelType, Item item) {
		
		super(field);
		this.modelType = modelType;
		
		if (null != field.getDataType()) {
			if (null != item && null != item.getDefaultCalculateType()) {
				this.calculateType = getCalculateTypeByTag(field.getDataType().getCalculateTypes(), item.getDefaultCalculateType());
			} else {
				this.calculateType = field.getDataType().getDefaultCalculateType();
			}
		}
		
		if (null != field.getDataType()) {
			if (null != item && null != item.getDefaultDataFormat()) {
				this.dataFormat = getDataFormatByTag(field.getDataType().getDataFormats(), item.getDefaultDataFormat());
			} else {
				this.dataFormat = field.getDataType().getDefaultDataFormat();
			}
		}
	}
	
	@XmlAttribute(name = "modelType")
	public AdhocModelType getModelType() {
		return modelType;
	}

	public void setModelType(AdhocModelType modelType) {
		this.modelType = modelType;
	}

	@XmlAttribute(name = "calculateType")
	public CalculateType getCalculateType() {
		return calculateType;
	}

	public void setCalculateType(CalculateType calculateType) {
		this.calculateType = calculateType;
	}

	@XmlAttribute(name = "dataFormat")
	public DataFormat getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}
	
	protected CalculateType getCalculateTypeByTag(List<CalculateType> calculateTypes, String calculateType) {
		if (null != calculateTypes && null != calculateType) {
			for (CalculateType type : calculateTypes) {
				if (null != type.getDefaultCalculateType() && type.getDefaultCalculateType().equals(calculateType)) {
					return type;
				}
			}
		}
		return null;
	}
	
	protected DataFormat getDataFormatByTag(List<DataFormat> dataFormats, String dataFormat) {
		if (null != dataFormats && null != dataFormat) {
			for (DataFormat type : dataFormats) {
				if (null != type.getDefaultDataFormat() && type.getDefaultDataFormat().equals(dataFormat)) {
					return type;
				}
			}
		}
		return null;
	}

	// DO NOT OVERRIDE EQUAL && HASHCODE FUNCTION, PLEASE !
}
