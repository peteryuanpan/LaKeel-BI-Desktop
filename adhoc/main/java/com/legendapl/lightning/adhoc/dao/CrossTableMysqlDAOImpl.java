package com.legendapl.lightning.adhoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.jexl2.Expression;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.CalculatedNumber;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.JoinDTO;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTRowTrieTree;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTColumnTrieTree;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.model.DataSourceImpl;

/**
 *　クロス集計のMysqlのDAO
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({"rawtypes"})
public class CrossTableMysqlDAOImpl extends CrossTableGenericDAO {

	private MysqlUtils mysqlUtils = new MysqlUtils(this);

	List<String> resourceIdList;

	List<String> fieldIds;

	List<DataType> dataTypes;

	public CrossTableMysqlDAOImpl(Adhoc adhoc) {
		super(adhoc);
		mysqlUtils.MysqlFilter();
	}

	public CrossTableMysqlDAOImpl(DataSourceImpl  dataSource) {
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

		if (getStream(crossTableRows).count() == 0 && getStream(crossTableColumns).count() == 0) return;
		
		orderStatement += getOrderStatement();

		if (!orderStatement.isEmpty()) {
			sqlStatement += "order by " + orderStatement + "\n";
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
		if (adhoc.getTopicTree().getJoins().isEmpty()) {
			sqlStatement += "from " + SQLUtils.DBsurround(adhoc.getTopicTree().getTableName()) + "\n";
		} else {
			sqlStatement += "from " + getRealTableStatement(tableGraph.getMainTableId()) + "\n";
			List<JoinDTO> subTables = tableGraph.getSubTables();
			for (JoinDTO join : subTables) {
				switch (join.getJoinType()) {
				case Full:
					sqlStatement += "full outer join ";
					break;
				case Inner:
					sqlStatement += "join ";
					break;
				case LeftOuter:
					sqlStatement += "left outer join ";
					break;
				case RightOuter:
					sqlStatement += "right outer join ";
					break;
				default:
					break;
				}
				sqlStatement += getRealTableStatement(join.getSubTableId()) + " ON (" + join.getCondition() + ")\n";
			}
		}
	}

	@Override
	public void execute(CTRowTrieTree tree, CTColumnTrieTree columnTree) {
		resourceIdList = new ArrayList<>(resourceIds);
		fieldIds = resourceIdList.stream().map(resourceId -> SQLUtils.uniqueFieldId(adhoc.getFieldByResId(resourceId))).collect(Collectors.toList());
		dataTypes = resourceIdList.stream().map(resourceId -> adhoc.getFieldByResId(resourceId).getDataType()).collect(Collectors.toList());
		if(sqlStatement.isEmpty()) {
			try {
				singleRow(null, tree, columnTree);
			} catch (SQLException e) {
				AdhocUtils.logger.error(e.getMessage(), e);
				AlertWindowService.SingleAlert.showError("execute failed.", e.getMessage());
			}
		} else {
			SQLFun<Void> excuteFun = new SQLFun<Void>() {
				@Override
				public Void call(ResultSet rs) throws Exception {
					while (rs.next()) {
						singleRow(rs, tree, columnTree);
					}
					return null;
				}

			};
			SQLExecute(sqlStatement, excuteFun, null);
		}
	}

	private void singleRow(ResultSet rs, CTRowTrieTree tree, CTColumnTrieTree columnTree) throws SQLException {
		List<Comparable> rows = new ArrayList<>();
		List<Comparable> columns = new ArrayList<>();
		Map<CrossTableField, CalculatedNumber> map = new HashMap<CrossTableField, CalculatedNumber>();
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
		crossTableRows.forEach(row -> {
			rows.add(resourceId2Value.get(row.getResourceId()));
		});
		crossTableColumns.forEach(column -> {
			columns.add(resourceId2Value.get(column.getResourceId()));
		});

		crossTableValues.forEach(value -> {
			map.put(SQLUtils.uniqueMeasure(value), SQLUtils.getNumberFormat(resourceId2Value.get(value.getResourceId()), value));
		});
		tree.insert(rows, columns, map);
		columnTree.insert(columns, null, null);
	}

}
