package com.legendapl.lightning.adhoc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.legendapl.lightning.adhoc.common.ModelType;

@XmlRootElement(name = "schema")
@XmlType(propOrder = {"items", "nodes", "trees"})
public class Domain extends BaseModel {
	
	static {
		modelType = ModelType.DOMAIN;
	}
	
	private List<Item> items;
	private List<DBNode> nodes;
	private List<DBTree> trees;
	private Map<String, String> dbTableIdToName;
	private DatabaseInfo database;
	
	public Domain() {
		super();
		modelType = ModelType.DOMAIN;
		this.items = new ArrayList<>();
		this.nodes = new ArrayList<>();
		this.trees = new ArrayList<>();
		this.dbTableIdToName = new HashMap<>();
		this.database = null;
	}
	
	@XmlElementWrapper(name = "items")
	@XmlElement(name = "item")
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	@XmlElementWrapper(name = "itemGroups")
	@XmlElement(name = "itemGroup")
	public List<DBNode> getNodes() {
		return nodes;
	}
	
	public void setNodes(List<DBNode> nodes) {
		this.nodes = nodes;
	}
	
	@XmlElementWrapper(name = "resources")
	@XmlElement(name = "jdbcTable")
	public List<DBTree> getTrees() {
		return trees;
	}
	
	public void setTrees(List<DBTree> trees) {
		this.trees = trees;
	}
	
	@XmlTransient
	public Map<String, String> getDbTableIdToName() {
		return dbTableIdToName;
	}

	public void setDbTableIdToName(Map<String, String> dbTableIdToName) {
		this.dbTableIdToName = dbTableIdToName;
	}

	@XmlTransient
	public DatabaseInfo getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseInfo database) {
		this.database = database;
	}
	
	public void setOthers() {
		// set dbTableIdToName
		for (DBTree tree : trees) {
			dbTableIdToName.put(tree.getId(), tree.getTableName());
		}
		// set trees' root
		for (DBTree tree : trees) {
			DBNode root = new DBNode();
			root.setId("");
			root.setResourceId(tree.getId());
			root.setTreeId(tree.getId());
			tree.setRoot(root);
			for (Item item : items) {
				String resId[] = item.getResourceId().split(Pattern.quote("."));
				if (resId[0].equals(tree.getId())) {
					tree.getRoot().getItems().add(item);
				}
			}
			for (DBNode node : nodes) {
				if (node.getResourceId().equals(tree.getId())) {
					tree.getRoot().getChildren().add(node);
				}
			}
		}
		// set trees
		for (DBTree tree : trees) {
			tree.setOthers();
		}
	}
	
}
