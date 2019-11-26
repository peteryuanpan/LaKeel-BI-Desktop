package com.legendapl.lightning.adhoc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.legendapl.lightning.adhoc.common.ModelType;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.xmlAdapter.MapStringToStringAdapter;

/**
 * トピックデータの情報
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.02.26
 */
@XmlRootElement(name = "Topic")
@XmlType(propOrder = {"database", "filters", "dbTableIdToName", "tree", "trees", "nodeIds"})
public class Topic extends BaseModel {

	static {
		modelType = ModelType.TOPIC;
	}

	private DBTree tree;
	private List<DBTree> trees;
	private List<String> nodeIds;
	private Map<String, String> dbTableIdToName;
	private DatabaseInfo database;
	private List<Filter> filters;

	public Topic() {
		super();
		modelType = ModelType.TOPIC;
		tree = null;
		trees = new ArrayList<>();
		nodeIds = new ArrayList<>();
		dbTableIdToName = new HashMap<>();
		database = null;
		filters = new ArrayList<>();
	}

	public Topic(Domain domain) {
		modelType = ModelType.TOPIC;
		this.tree = null;
		this.trees = new ArrayList<>();
		for (DBTree tree : domain.getTrees()) {
			if (null != tree.getRoot() && !tree.getRoot().getNodes().isEmpty()) {
				this.trees.add(tree);
			}
		}
		this.nodeIds = new ArrayList<>();
		for (Item item : domain.getItems()) {
			this.nodeIds.add(item.getId());
		}
		for (DBNode node : domain.getNodes()) {
			this.nodeIds.add(node.getId());
		}
		this.setDbTableIdToName(domain.getDbTableIdToName());
		this.setDatabase(domain.getDatabase());
		this.filters = new ArrayList<>();
	}

	@XmlElement(name = "SelectTree")
	public DBTree getTree() {
		return tree;
	}

	public void setTree(DBTree tree) {
		this.tree = tree;
	}

	@XmlElementWrapper(name = "DBTrees")
	@XmlElement(name = "DBTree")
	public List<DBTree> getTrees() {
		return trees;
	}

	public void setTrees(List<DBTree> trees) {
		this.trees = trees;
	}

	@XmlElementWrapper(name = "NodeIds")
	@XmlElement(name = "NodeId")
	public List<String> getNodeIds() {
		return nodeIds;
	}

	public void setNodeIds(List<String> nodeIds) {
		this.nodeIds = nodeIds;
	}

	@XmlJavaTypeAdapter(type = Map.class, value = MapStringToStringAdapter.class)
	@XmlElement(name = "DbTableIdToName")
	public Map<String, String> getDbTableIdToName() {
		return dbTableIdToName;
	}

	public void setDbTableIdToName(Map<String, String> dbTableIdToName) {
		this.dbTableIdToName = dbTableIdToName;
	}

	@XmlElement(name = "DatabaseInfo")
	public DatabaseInfo getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseInfo database) {
		this.database = database;
	}

	@XmlElementWrapper(name = "Filters")
	@XmlElement(name = "Filter")
	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public Field getFieldByResId(String resId) {
		Field field = tree.getFieldByResId(resId);
		String[] ids = resId.split(Pattern.quote("."));
		field.setTableName(dbTableIdToName.get(ids[ids.length-2]));
		field.setTableId(ids[ids.length-2]);
		return field;
	}
	
	/*-------------------------Set Others-----------------------------*/
	
	public void setOthers() {
		// set trees, fields
		for (DBTree tree : trees) {
			tree.setOthers();
		}
		// set tree, fields
		if (null != tree) {
			tree.setOthers();
			for (Field field : tree.getFields()) {
				field = this.getFieldByResId(field.getResourceId());
			}
		}
	}
	
}
