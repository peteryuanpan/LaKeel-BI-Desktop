package com.legendapl.lightning.adhoc.adhocView.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "resourceId", "sortFlg", "tableId", "columnId" })
public class SortData {

	private String resourceId;
	private Boolean sortFlg;
	protected String tableId;
	protected String columnId;

	public SortData() {
		super();
		this.resourceId = null;
		this.sortFlg = true;
		this.tableId = null;
		this.columnId = null;
	}

	@XmlAttribute(name = "resourceId")
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@XmlAttribute(name = "SortFlg")
	public Boolean getSortFlg() {
		return sortFlg;
	}

	public void setSortFlg(Boolean sortFlg) {
		this.sortFlg = sortFlg;
	}

	@XmlAttribute(name = "tableId")
	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	@XmlAttribute(name = "columnId")
	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SortData other = (SortData) obj;
		if (this.resourceId != other.getResourceId()) 
			return false;
		if (this.sortFlg != other.getSortFlg()) 
			return false;
		return true;
	}

}
