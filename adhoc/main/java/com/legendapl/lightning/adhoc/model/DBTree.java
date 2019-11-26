package com.legendapl.lightning.adhoc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"id", "tableName", "root", "fields", "filter", "joinInfo", "joins"})
public class DBTree {

	private String id;
	private String tableName;
	private DBNode root;
	private JoinInfo joinInfo;
	private String filter;
	private List<Join> joins;
	private List<Field> fields;
	private Map<String, Field> resIdToField;

	public DBTree() {
		super();
		id = new String();
		tableName = new String();
		root = null;
		joinInfo = new JoinInfo();
		filter = new String();
		joins = new ArrayList<>();
		fields = new ArrayList<>();
		resIdToField = new HashMap<>();
	}

	public DBTree(String id, String tableName, DBNode root, JoinInfo joinInfo, String filter, List<Join> joins,
			List<Field> fields, Map<String, Field> resIdToField) {
		super();
		this.id = id;
		this.tableName = tableName;
		this.root = root;
		this.joinInfo = joinInfo;
		this.filter = filter;
		this.joins = joins;
		this.fields = fields;
		this.resIdToField = resIdToField;
	}

	public DBTree(DBTree tree) {
		super();
		this.setId(tree.getId());
		this.setTableName(tree.getTableName());
		this.setRoot(tree.getRoot());
		this.setJoinInfo(tree.getJoinInfo());
		this.setFilter(tree.getFilter());
		this.setJoins(tree.getJoins());
		this.setFields(tree.getFields());
		this.setResIdToField(tree.getResIdToField());
	}

	@XmlAttribute(name = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name = "tableName")
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@XmlElement(name = "itemGroup")
	public DBNode getRoot() {
		return root;
	}

	public void setRoot(DBNode root) {
		this.root = root;
	}

	@XmlElement(name = "joinInfo")
	public JoinInfo getJoinInfo() {
		return joinInfo;
	}

	public void setJoinInfo(JoinInfo joinInfo) {
		this.joinInfo = joinInfo;
	}

	@XmlElement(name = "filterString")
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@XmlElementWrapper(name = "joinedDataSetList")
	@XmlElement(name = "joinedDataSetRef")
	public List<Join> getJoins() {
		return joins;
	}

	public void setJoins(List<Join> joins) {
		this.joins = joins;
	}

	@XmlElementWrapper(name = "fieldList")
	@XmlElement(name = "field")
	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	@XmlTransient
	public Map<String, Field> getResIdToField() {
		return resIdToField;
	}

	public void setResIdToField(Map<String, Field> resIdToField) {
		this.resIdToField = resIdToField;
	}

	public Field getFieldByResId(String resId) {
		return resIdToField.get(resId);
	}
	
	/**
	 * return all items of tree
	 */
	public List<Item> getAllItems() {
		if (null == root) {
			return new ArrayList<>();
		}
		return root.getAllItems();
	}
	
	public List<BaseNode> getAllNodes() {
		if (null == root) {
			return new ArrayList<>();
		}
		return root.getAllNodes();
	}

	/*-------------------------Set Others-----------------------------*/
	
	/**
	 * set treeId, fields, resIdToField by root
	 */
	public void setOthers() {
		if (null != root) {
			resIdToField = new HashMap<>();
			setOthers(root);
		}
	}

	private void setOthers(DBNode root) {
		root.setTreeId(id);
		for (int i = 0; i < root.getChildren().size(); i ++) {
			DBNode child = root.getChildren().get(i);
			child.setTreeId(id);
			setOthers(child);
		}
		for (int i = 0; i < root.getItems().size(); i ++) {
			Item item = root.getItems().get(i);
			item.setTreeId(id);
			for (Field field : fields) {
				if (item.getResourceId().equals(id+"."+field.getId())) {
					String[] ids = item.getResourceId().split(Pattern.quote("."));
					field.setTableId(ids[ids.length-2]);
					//field.setTableName() ..
					field.setLabel(item.getLabel());
					field.setResourceId(item.getResourceId());
					resIdToField.put(item.getResourceId(), field);
				}
			}
		}
	}

}
