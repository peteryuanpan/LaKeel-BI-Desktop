package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.FilterType;
import com.legendapl.lightning.adhoc.common.ModelType;
import com.legendapl.lightning.adhoc.common.ViewType;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.BaseModel;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBTree;
import com.legendapl.lightning.adhoc.model.DatabaseInfo;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.xmlAdapter.MapStringToStringAdapter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * アドホックデータの情報
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.03.21
 */
@XmlRootElement(name = "Adhoc")
@XmlType(propOrder = {"topicName", "viewModelType", "dataModelType", "database",
		"layoutData", "filterCheckFlg", "filterConnect", "filters", "columnSort", "topicTree", "dbTableIdToName"})
public class Adhoc extends BaseModel {

	static {
		modelType = ModelType.ADHOC;
	}

	private DBTree topicTree;
	private Map<String, String> dbTableIdToName;
	private DatabaseInfo database;
	
	private LayoutData layoutData;
	
	private List<Filter> filters;
	private Boolean filterCheckFlg;
	private String filterConnect;
	private ObservableList<SortData> columnSort; // DO NOT MOVE TO LIST

	private String topicName;
	private AdhocModelType viewModelType;
	private AdhocModelType dataModelType;

	public Adhoc() {
		super();
		modelType = ModelType.ADHOC;
		this.topicTree = null;
		this.dbTableIdToName = new HashMap<>();
		this.database = null;
		this.layoutData = null;
		this.filters = new ArrayList<>();
		this.filterCheckFlg = false;
		this.filterConnect = new String();
		this.topicName = new String();
		this.viewModelType = null;
		this.dataModelType = null;
		this.columnSort = FXCollections.observableArrayList();
	}

	public Adhoc(Topic topic) {
		super();
		modelType = ModelType.ADHOC;
		this.setTopicTree(topic.getTree());
		if (null != this.topicTree) {
			this.topicTree.setOthers();
		}
		this.setDbTableIdToName(topic.getDbTableIdToName());
		this.setDatabase(topic.getDatabase());
		this.layoutData = null;
		this.setFilters(topic.getFilters());
		this.filterCheckFlg = false;
		this.filterConnect = new String();
		this.topicName = new String();
		this.viewModelType = null;
		this.dataModelType = null;
		this.columnSort = FXCollections.observableArrayList();
	}

	@XmlElement(name = "topicTree")
	public DBTree getTopicTree() {
		return topicTree;
	}

	public void setTopicTree(DBTree topicTree) {
		this.topicTree = topicTree;
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
	
	@XmlElement(name = "LayoutData")
	public LayoutData getLayoutData() {
		return layoutData;
	}

	public void setLayoutData(LayoutData layoutData) {
		this.layoutData = layoutData;
	}

	@XmlElementWrapper(name = "Filters")
	@XmlElement(name = "Filter")
	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	@XmlElement(name = "FilterCheckFlg")
	public Boolean getFilterCheckFlg() {
		return filterCheckFlg;
	}

	public void setFilterCheckFlg(Boolean filterCheckFlg) {
		this.filterCheckFlg = filterCheckFlg;
	}
	
	@XmlElement(name = "filterConnect")
	public String getFilterConnect() {
		return filterConnect;
	}

	public void setFilterConnect(String filterConnect) {
		this.filterConnect = filterConnect;
	}

	@XmlAttribute(name = "topicName")
	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	@XmlAttribute(name = "viewModelType")
	public AdhocModelType getViewModelType() {
		return viewModelType;
	}

	public void setViewModelType(AdhocModelType viewModelType) {
		this.viewModelType = viewModelType;
	}

	@XmlAttribute(name = "dataModelType")
	public AdhocModelType getDataModelType() {
		return dataModelType;
	}

	public void setDataModelType(AdhocModelType dataModelType) {
		this.dataModelType = dataModelType;
	}

	@XmlElementWrapper(name = "columnSorts")
	@XmlElement(name = "columnSort")
	public ObservableList<SortData> getColumnSort() {
		return columnSort;
	}

	public void setColumnSort(ObservableList<SortData> columnSort) {
		this.columnSort = columnSort;
	}

	/*-------------------------Set Others-----------------------------*/

	/**
	 * topicTree.setOthers();
	 */
	public void setOthers() {
		if (null != topicTree) {
			topicTree.setOthers();
			// label2 -> label
			topicTree.getAllNodes().forEach(node -> {
				if (null != node.getLabel2()) {
					node.setLabel(node.getLabel2());
					node.setLabel2(null);
				}
			});
			// field:label
			topicTree.getAllItems().forEach(item -> {
				topicTree.getFields().forEach(field -> {
					if (item.getResourceId().equals(topicTree.getId()+"."+field.getId())) {
						field.setLabel(item.getLabel());
					}
				});
			});
			// defaultViewType
			topicTree.getAllItems().forEach(item -> {
				if (null != item.getDefaultViewType()) {
					ViewType defaultViewType = ViewType.getViewType(null, item.getDefaultViewType());
					if (null != defaultViewType) {
						item.setViewType(defaultViewType);
						item.setDefaultViewType(null);
					}
				}
			});
			// viewType
			topicTree.getAllItems().forEach(node -> {
				ViewType type = node.getViewType();
				if (null == type ||
						(!type.equals(ViewType.TOP) && !type.equals(ViewType.BOTTOM))) {
					Field field = topicTree.getFieldByResId(node.getResourceId());
					if(null == field || !FilterType.NUMBER.equals(field.getDataType().getFilterType())) {
						node.setViewType(ViewType.TOP);
					} else {
						node.setViewType(ViewType.BOTTOM);
					}
				}
			});
			// rank
			topicTree.getAllNodes().forEach(node -> {
				Collections.sort(node.getNodes(), new Comparator<BaseNode>() {
					@Override public int compare(BaseNode node1, BaseNode node2) {
						Integer rank1 = node1.getRank();
						Integer rank2 = node2.getRank();
						if (null == rank1) rank1 = 0;
						if (null == rank2) rank2 = 0;
						return rank1 - rank2;
					}
				});
			});
		}
	}

	public Field getFieldByResId(String resId) {
		Field field = topicTree.getFieldByResId(resId);
		//TODO
		if(field.getCalculatedExpression() == null) {
			String[] ids = resId.split(Pattern.quote("."));
			field.setTableName(dbTableIdToName.get(ids[ids.length-2]));
			field.setTableId(ids[ids.length-2]);
		}
		return field;
	}

}
