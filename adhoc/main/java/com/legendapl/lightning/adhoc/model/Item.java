package com.legendapl.lightning.adhoc.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.legendapl.lightning.adhoc.common.ViewType;

@XmlType(propOrder = {"id", "rank", "label", "label2", "resourceId", "viewType", 
		"defaultViewType", "defaultCalculateType", "defaultDataFormat"})
public class Item extends BaseNode {

	private String id;
	private String label;
	private String label2;
	private String resourceId;
	private ViewType viewType;
	private String treeId;
	private Integer rank;
	
	private String defaultViewType;
	private String defaultCalculateType;
	private String defaultDataFormat;
	
	public Item() {
		super();
		this.id = new String();
		this.label = new String();
		this.label2 = null;
		this.resourceId = new String();
		this.viewType = null;
		this.treeId = new String();
		this.rank = null;
		this.defaultViewType = null;
		this.defaultCalculateType = null;
		this.defaultDataFormat = null;
	}

	public Item(Item item) {
		super();
		this.setId(item.getId());
		this.setLabel(item.getLabel());
		this.setLabel2(item.getLabel2());
		this.setResourceId(item.getResourceId());
		this.setViewType(item.getViewType());
		this.setTreeId(item.getTreeId());
		this.setRank(item.getRank());
		this.setDefaultViewType(item.getDefaultViewType());
		this.setDefaultCalculateType(item.getDefaultCalculateType());
		this.setDefaultDataFormat(item.getDefaultDataFormat());
	}
	
	public Item(Field field) {
		super();
		this.setId(field.getResourceId());
		this.setLabel(field.getLabel());
		this.label2 = null;
		this.setResourceId(field.getResourceId());
		this.viewType = null;
		this.treeId = new String();
		this.rank = null;
		this.defaultViewType = null;
		this.defaultCalculateType = null;
		this.defaultDataFormat = null;
	}

	@XmlAttribute(name = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name = "label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@XmlAttribute(name = "label2")
	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	@XmlAttribute(name = "resourceId")
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@XmlAttribute(name = "viewType")
	public ViewType getViewType() {
		return viewType;
	}

	public void setViewType(ViewType viewType) {
		this.viewType = viewType;
	}
	
	@XmlTransient
	public String getTreeId() {
		return treeId;
	}

	public void setTreeId(String treeId) {
		this.treeId = treeId;
	}

	@XmlAttribute(name = "rank")
	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	@XmlAttribute(name = "dimensionOrMeasure")
	public String getDefaultViewType() {
		return defaultViewType;
	}

	public void setDefaultViewType(String defaultViewType) {
		this.defaultViewType = defaultViewType;
	}

	@XmlAttribute(name = "defaultAgg")
	public String getDefaultCalculateType() {
		return defaultCalculateType;
	}

	public void setDefaultCalculateType(String defaultCalculateType) {
		this.defaultCalculateType = defaultCalculateType;
	}

	@XmlAttribute(name = "defaultMask")
	public String getDefaultDataFormat() {
		return defaultDataFormat;
	}

	public void setDefaultDataFormat(String defaultDataFormat) {
		this.defaultDataFormat = defaultDataFormat;
	}
	
	/*-------------------------------------------------*/
	
	public List<Item> getAllItems() {
		List<Item> items = new ArrayList<>();
		items.add(this);
		return items;
	}
	
	public List<BaseNode> getAllNodes() {
		List<BaseNode> nodes = new ArrayList<BaseNode>();
		nodes.add(this);
		return nodes;
	}

	public List<BaseNode> getNodes() {
		return new ArrayList<>();
	}

}
