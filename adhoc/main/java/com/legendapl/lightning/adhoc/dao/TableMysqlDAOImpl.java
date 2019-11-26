package com.legendapl.lightning.adhoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.jexl2.Expression;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.JoinDTO;
import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.SortableTrieTree;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.model.DataSourceImpl;

/**
 *　テーブルのMysqlのDAO
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({"rawtypes"})
public class TableMysqlDAOImpl extends TableGenericDAO {

	private static final String maxLengthSqlFormat = "SELECT `FN` FROM `TN` WHERE LENGTH(`FN`) = (SELECT MAX(LENGTH(`FN`)) FROM `TN`) LIMIT 1";

	private static final String distinctSqlFormat = "SELECT DISTINCT `FN` FROM `TN`";

	private static final String distinctNumberSqlFormat = "SELECT `TN`.`FN` FROM `TN` ORDER BY `TN`.`FN` DESC LIMIT 1";

	private List<String> resourceIdList;

	private List<String> fieldIds;

	private List<DataType> dataTypes;

	private SortableTrieTree tree;

	private MysqlUtils mysqlUtils = new MysqlUtils(this);

	public TableMysqlDAOImpl(Adhoc adhoc) {
		super(adhoc);
		mysqlUtils.MysqlFilter();
	}

	public TableMysqlDAOImpl(DataSourceImpl dataSource) {
		super(dataSource);
	}

	@Override
	protected void generateSQL() {
		mysqlUtils.generateSelectStatement();
		generateFromStatement();
		generateWhereStatement();
		generateOrderStatement();
	}

	protected void generateOrderStatement() {
		
		String sortSql = getSortStatement();

		if (getStream(groups).count() == 0) {
			if (!sortSql.isEmpty()) orderStatement = sortSql;
			
		} else {
			orderStatement += getGroupStatement();
			if (!sortSql.isEmpty()) orderStatement += "," + sortSql;
		}
		
		if (!orderStatement.trim().isEmpty()) {
			sqlStatement += "ORDER BY " + orderStatement;
		}
	}
	


	protected void generateWhereStatement() {
		super.generateWhereStatement();
	}
	
	protected void generateAdhocWhereStatement() {
		if (adhoc.getFilterCheckFlg()) {
			adhocWhereStatement = MysqlUtils.getFilterConnect(adhoc.getFilters(), adhoc.getFilterConnect().replaceAll(" +", " ").trim());
		} else {
			adhocWhereStatement = "";
		}
	}

	protected void generateFromStatement() {
		if(adhoc.getTopicTree().getJoins().isEmpty()) {
			fromStatement = "FROM " + SQLUtils.DBsurround(adhoc.getTopicTree().getTableName()) + "\n";
		} else {
			fromStatement = "FROM " + getRealTableStatement(tableGraph.getMainTableId()) + "\n";
			List<JoinDTO> subTables = tableGraph.getSubTables();
			for(JoinDTO join: subTables) {
				switch(join.getJoinType()) {
				case Full:
					fromStatement += "FULL OUTER JOIN ";
					break;
				case Inner:
					fromStatement += "JOIN ";
					break;
				case LeftOuter:
					fromStatement += "LEFT OUTER JOIN ";
					break;
				case RightOuter:
					fromStatement += "RIGHT OUTER JOIN ";
					break;
				default:
					break;
				}
				fromStatement += getRealTableStatement(join.getSubTableId()) + " ON (" + join.getCondition() + ")\n";
			}
		}
		sqlStatement += fromStatement;
	}

	@Override
	public void execute(List<TableViewData> tempData, boolean fullData) {
		resourceIdList = new ArrayList<>(resourceIds);
		fieldIds = resourceIdList.stream().map(resourceId -> SQLUtils.uniqueFieldId(adhoc.getFieldByResId(resourceId))).collect(Collectors.toList());
		dataTypes = resourceIdList.stream().map(resourceId -> adhoc.getFieldByResId(resourceId).getDataType()).collect(Collectors.toList());
		tree = new SortableTrieTree(tempData);
		if(sqlStatement.isEmpty()) {
			try {
				singleRow(null);
				tree.sortTree();
				tree.getDataList();
			} catch (SQLException e) {
				AdhocUtils.logger.error(e.getMessage(), e);
				AlertWindowService.SingleAlert.showError("execute failed.", e.getMessage());
			}
		} else {
			SQLFun<Void> excuteFun = new SQLFun<Void>() {
				@Override
				public Void call(ResultSet rs) throws Exception {
					int rowNum = 0;
					while (rs.next() && (fullData || rowNum++ < sampleData)) {
						singleRow(rs);
					}
					tree.sortTree();
					tree.getDataList();
					return null;
				}
			};
			SQLExecute(sqlStatement, excuteFun, null);
		}

	}

	private void singleRow(ResultSet rs) throws SQLException {
		resourceId2Value.clear();

		for(int i=0;i<resourceIdList.size();i++) {
			Comparable ob = dataTypes.get(i).getResult(rs, fieldIds.get(i));
			resourceId2Value.put(resourceIdList.get(i), ob);
		}
		for(String cal: calculatedResourceIds) {
			Field calField = adhoc.getFieldByResId(cal);
			Expression expression = jexl.createExpression(calField.getCalculatedExpression());
			Object calOb = expression.evaluate(jexlContext);
			resourceId2Value.put(cal, (Comparable)calField.getDataType().convert(calOb));
		}
		TableViewData data = new TableViewData();
		columns.forEach(column -> {
			data.addValue(column, resourceId2Value.get(column.getResourceId()));
		});

		List<Comparable> orders = new ArrayList<>();
		groups.forEach(order -> {
			Comparable ob = resourceId2Value.get(order.getResourceId());
			orders.add(ob);
		});
		if(columns.isEmpty()) {
			tree.insert(orders, null);
		} else {
			tree.insert(orders, data);
		}
	}

	@Override
	public String getMaxLength(Field field) {
		String sql = "";
		switch (field.getDataType().getFilterType()) {
		case NUMBER:
		case BOOLEAN:
			sql = distinctNumberSqlFormat.replaceAll("FN", field.getFieldDBName());
			break;

		default:
			sql = maxLengthSqlFormat.replaceAll("FN", field.getFieldDBName());
			break;
		}

		sql = sql.replaceAll("TN", field.getTableName());
		SQLFun<String> excuteFun = new SQLFun<String>() {
			@Override
			public String call(ResultSet rs) throws Exception {
				if(rs.next()) {
					String maxFieldValue = rs.getString(field.getFieldDBName());
					return maxFieldValue;
				}
				return "";
			}
		};
		return SQLExecute(sql, excuteFun, "");
	}

	@Override
	public List<String> getDistinctList(Field field) {
		String sql = distinctSqlFormat.replaceAll("FN", field.getFieldDBName());
		sql = sql.replaceAll("TN", field.getTableName());
		SQLFun<List<String>> excuteFun = new SQLFun<List<String>>() {

			@Override
			public List<String> call(ResultSet rs) throws Exception {
				List<String> distincts = new ArrayList<String>();
				while (rs.next()) {
					distincts.add(rs.getString(field.getFieldDBName()));
				}
				return distincts;
			}

		};
		return SQLExecute(sql, excuteFun, null);
	}

	public Object Field(String resourceId) {
		return resourceId2Value.get(resourceId);
	}

	public List<String> calculateField(Field field) {
		List<String> distincts = new ArrayList<String>();
		Expression expression = jexl.createExpression(field.getCalculatedExpression());
		Object calOb = expression.evaluate(jexlContext);
		resourceId2Value.put(field.getResourceId(), (Comparable)calOb);
		return distincts;
	}
	
	@Override
	public List<String> getCalculateList(Field field) {
		String sql = distinctSqlFormat.replaceAll("FN", field.getFieldDBName());
		sql = sql.replaceAll("TN", field.getTableName());
		SQLFun<List<String>> excuteFun = new SQLFun<List<String>>() {

			@Override
			public List<String> call(ResultSet rs) throws Exception {
				List<String> distincts = new ArrayList<String>();
				while (rs.next()) {
					distincts.add(rs.getString(field.getFieldDBName()));
				}
				return distincts;
			}

		};
		return SQLExecute(sql, excuteFun, null);
	}
}
