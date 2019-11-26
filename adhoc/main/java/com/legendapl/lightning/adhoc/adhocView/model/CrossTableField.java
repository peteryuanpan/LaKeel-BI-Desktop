package com.legendapl.lightning.adhoc.adhocView.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.common.GroupType;
import com.legendapl.lightning.adhoc.factory.itemTree.CrossTableValueTreeFactory;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;

@XmlType(propOrder = {"groupType"})
public class CrossTableField extends AdhocField {

	private GroupType groupType;

	public CrossTableField() {
		super();
	}

	public CrossTableField(Field field, AdhocModelType modelType, Item item) {

		super(field, modelType, item);

		if (null != field.getDataType()) {
			this.groupType = field.getDataType().getDefaultGroupType();
		}

		DataType dataType = CrossTableValueTreeFactory.getDataType(this);
		if (null != dataType) {
			this.dataFormat = dataType.getDefaultDataFormat();
		}
	}

	@XmlAttribute(name = "groupType")
	public GroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(GroupType groupType) {
		this.groupType = groupType;
	}

	// DO NOT OVERRIDE EQUAL && HASHCODE FUNCTION, PLEASE !
}
