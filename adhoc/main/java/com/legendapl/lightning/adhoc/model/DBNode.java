package com.legendapl.lightning.adhoc.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.legendapl.lightning.adhoc.common.ViewType;

@XmlType(propOrder = {"id", "rank", "label", "label2", "resourceId", "viewType", "items", "children"})
public class DBNode extends BaseNode {

	private String id;
	private String label;
	private String label2;
	private String resourceId;
	private ViewType viewType;
	private String treeId;
	private Integer rank;
	private List<DBNode> children;
	private List<Item> items;

	public DBNode() {
		super();
		id = new String();
		label = new String();
		label2 = null;
		resourceId = new String();
		viewType = null;
		treeId = new String();
		rank = null;
		children = new ArrayList<>();
		items = new ArrayList<>();
	}

	public DBNode(DBNode node) {
		super();
		this.setId(node.getId());
		this.setLabel(node.getLabel());
		this.setLabel2(node.getLabel2());
		this.setResourceId(node.getResourceId());
		this.setViewType(node.getViewType());
		this.setTreeId(node.getTreeId());
		this.setRank(node.getRank());
		this.setChildren(node.getChildren());
		this.setItems(node.getItems());
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
	
	@XmlElementWrapper(name = "itemGroups")
	@XmlElement(name = "itemGroup")
	public List<DBNode> getChildren() {
		if (null == children) {
			children = new ArrayList<>();
		}
		return children;
	}

	public void setChildren(List<DBNode> children) {
		this.children = children;
	}

	@XmlElementWrapper(name="items")
	@XmlElement(name = "item")
	public List<Item> getItems() {
		if (null == items) {
			items = new ArrayList<>();
		}
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	/*-------------------------------------------------*/

	public List<Item> getAllItems() {
		List<Item> items = new ArrayList<>();
		for (BaseNode child : getNodes()) {
			items.addAll(child.getAllItems());
		}
		return items;
	}
	
	public List<BaseNode> getAllNodes() {
		List<BaseNode> nodes = new ArrayList<>();
		nodes.add(this);
		for (BaseNode child : getNodes()) {
			nodes.addAll(child.getAllNodes());
		}
		return nodes;
	}

	public List<BaseNode> getNodes() {
		List<BaseNode> nodes = new ArrayList<>();
		nodes.addAll(this.items);
		nodes.addAll(this.children);
		return nodes;
	}

}
