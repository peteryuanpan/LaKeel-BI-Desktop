package com.legendapl.lightning.adhoc.model;

import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.legendapl.lightning.adhoc.common.ViewType;

public abstract class BaseNode {
	
	@XmlTransient
	public abstract String getId();

	public abstract void setId(String id);

	@XmlTransient
	public abstract String getLabel();

	public abstract void setLabel(String label);
	
	@XmlTransient
	public abstract String getLabel2();

	public abstract void setLabel2(String label2);

	@XmlTransient
	public abstract String getResourceId();

	public abstract void setResourceId(String resourceId);

	@XmlTransient
	public abstract ViewType getViewType();
	
	public abstract void setViewType(ViewType viewType);
	
	@XmlTransient
	public abstract String getTreeId();
	
	public abstract void setTreeId(String treeId);
	
	@XmlTransient
	public abstract Integer getRank();

	public abstract void setRank(Integer rank);
	
	/**
	 * return all Items (Only Item)
	 */
	public abstract List<Item> getAllItems();
	
	/**
	 * return all Nodes (DBNode and Item)
	 */
	public abstract List<BaseNode> getAllNodes();
	
	/**
	 * return Children (DBNode) and Items
	 */
	public abstract List<BaseNode> getNodes();
	
	@Override
	public String toString() {
		return getLabel();
	}
	
}
