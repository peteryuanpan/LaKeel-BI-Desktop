package com.legendapl.lightning.adhoc.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.xmlAdapter.DataTypeAdapter;

@XmlType(propOrder = {"id", "fieldDBName", "dataType", "tableName", "tableId", "label", "resourceId", "calculatedExpression", "subFieldResourceId"})
public class Field {

	protected String id;
	protected String fieldDBName;
	protected DataType dataType;
	protected String tableName;
	protected String tableId;
	protected String label;
	protected String resourceId;
	protected String calculatedExpression;
	protected List<String> subFieldResourceId;

	public Field() {
		super();
		this.id = new String();
		this.fieldDBName = new String();
		this.dataType = DataType.UNKNOW;
		this.tableName = null;
		this.tableId = null;
		this.label = null;
		this.resourceId = null;
		this.calculatedExpression = null;
		this.subFieldResourceId = null;
	}

	public Field(Field field) {
		super();
		this.setId(field.getId());
		this.setFieldDBName(field.getFieldDBName());
		this.setDataType(field.getDataType());
		this.setTableName(field.getTableName());
		this.setTableId(field.getTableId());
		this.setLabel(field.getLabel());
		this.setResourceId(field.getResourceId());
		this.setCalculatedExpression(field.getCalculatedExpression());
		this.setSubFieldResourceId(field.getSubFieldResourceId());
	}

	@XmlAttribute(name = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name = "fieldDBName")
	public String getFieldDBName() {
		return fieldDBName;
	}

	public void setFieldDBName(String fieldDBName) {
		this.fieldDBName = fieldDBName;
	}

	@XmlJavaTypeAdapter(type = DataType.class, value = DataTypeAdapter.class)
	@XmlAttribute(name = "type")
	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	@XmlAttribute(name = "tableName")
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@XmlAttribute(name = "tableId")
	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	@XmlAttribute(name = "label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@XmlAttribute(name = "resourceId")
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@XmlAttribute(name = "calculatedExpression")
	public String getCalculatedExpression() {
		return calculatedExpression;
	}

	public void setCalculatedExpression(String calculatedExpression) {
		this.calculatedExpression = calculatedExpression;
	}

	@XmlAttribute(name = "subFieldResourceId")
	public List<String> getSubFieldResourceId() {
		return subFieldResourceId;
	}

	public void setSubFieldResourceId(List<String> subFieldResourceId) {
		this.subFieldResourceId = subFieldResourceId;
	}

	// DO NOT OVERRIDE EQUAL && HASHCODE FUNCTION, PLEASE !
}
