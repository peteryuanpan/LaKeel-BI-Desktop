package com.legendapl.lightning.adhoc.adhocView.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;

@XmlType(propOrder = {"calculated", "interval"})
public class TableField extends AdhocField {

	private Boolean calculated = false;
	private Boolean interval = false;

	public TableField() {
		super();
	}

	public TableField(Field field, AdhocModelType modelType, Item item) {
		super(field, modelType, item);
	}
	
	public TableField(TableField field, AdhocModelType modelType, Item item) {
		super(field, modelType, item);
		this.setCalculated(field.isCalculated());
		this.setInterval(field.isInterval());
	}

	@XmlAttribute(name = "calculated")
	public Boolean isCalculated() {
		return calculated;
	}

	public void setCalculated(Boolean calculated) {
		this.calculated = calculated;
	}

	@XmlAttribute(name = "isInterval")
	public Boolean isInterval() {
		return interval;
	}

	public void setInterval(Boolean interval) {
		this.interval = interval;
	}
	
	// DO NOT OVERRIDE EQUAL && HASHCODE FUNCTION, PLEASE !
}
