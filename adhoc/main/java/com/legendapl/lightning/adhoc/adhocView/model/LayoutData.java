package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"tableColumns", "tableRows", "valueIndex", "row", "crossTableColumns", "crossTableRows", "crossTableValues"})
public class LayoutData {

	private List<TableField> tableColumns = new ArrayList<>();
	private List<TableField> tableRows = new ArrayList<>();
	private List<CrossTableField> crossTableColumns = new ArrayList<>();
	private List<CrossTableField> crossTableRows = new ArrayList<>();
	private List<CrossTableField> crossTableValues = new ArrayList<>();
	private Integer valueIndex;
	private Boolean row;
	
	@XmlElementWrapper(name = "tableColumns")
	@XmlElement(name = "tableColumn")
	public List<TableField> getTableColumns() {
		return tableColumns;
	}
	
	public void setTableColumns(List<TableField> tableColumns) {
		this.tableColumns = tableColumns;
	}
	
	@XmlElementWrapper(name = "tableRows")
	@XmlElement(name = "tableRow")
	public List<TableField> getTableRows() {
		return tableRows;
	}
	
	public void setTableRows(List<TableField> tableRows) {
		this.tableRows = tableRows;
	}
	
	@XmlElementWrapper(name = "crossTableColumns")
	@XmlElement(name = "crossTableColumn")
	public List<CrossTableField> getCrossTableColumns() {
		return crossTableColumns;
	}
	
	public void setCrossTableColumns(List<CrossTableField> crossTableColumns) {
		this.crossTableColumns = crossTableColumns;
	}
	
	@XmlElementWrapper(name = "crossTableRows")
	@XmlElement(name = "crossTableRow")
	public List<CrossTableField> getCrossTableRows() {
		return crossTableRows;
	}
	
	public void setCrossTableRows(List<CrossTableField> crossTableRows) {
		this.crossTableRows = crossTableRows;
	}
	
	@XmlElementWrapper(name = "crossTableValues")
	@XmlElement(name = "crossTableValue")
	public List<CrossTableField> getCrossTableValues() {
		return crossTableValues;
	}
	
	public void setCrossTableValues(List<CrossTableField> crossTableValues) {
		this.crossTableValues = crossTableValues;
	}
	
	@XmlElement(name = "valueIndex")
	public Integer getValueIndex() {
		return valueIndex;
	}
	
	public void setValueIndex(Integer valueIndex) {
		this.valueIndex = valueIndex;
	}
	
	@XmlElement(name = "row")
	public Boolean isRow() {
		return row;
	}
	
	public void setRow(Boolean row) {
		this.row = row;
	}
	
}
