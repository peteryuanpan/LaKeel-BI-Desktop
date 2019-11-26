package com.legendapl.lightning.adhoc.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTRowTrieTree;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTColumnTrieTree;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.dao.CrossTableGenericDAO;
import com.legendapl.lightning.adhoc.dao.CrossTableMysqlDAOImpl;
import com.legendapl.lightning.adhoc.dao.CrossTableOracleDAOImpl;
import com.legendapl.lightning.adhoc.dao.CrossTableSqlserverDAOImpl;
import com.legendapl.lightning.adhoc.dao.TableGenericDAO;
import com.legendapl.lightning.adhoc.dao.TableMysqlDAOImpl;
import com.legendapl.lightning.adhoc.dao.TableOracleDAOImpl;
import com.legendapl.lightning.adhoc.dao.TableSqlserverDAOImpl;
import com.legendapl.lightning.adhoc.dao.util.CalculateListMap;
import com.legendapl.lightning.adhoc.model.DatabaseInfo;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.model.DataSourceImpl;

import javafx.scene.text.Font;

/**
 * データベースからデータをロードする
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class DatabaseService {

	private TableGenericDAO tableDao;
	private CrossTableGenericDAO crossTableDao;
	protected Logger logger = Logger.getLogger(getClass());

	public DatabaseService(Adhoc adhoc) {
		switch (adhoc.getDatabase().getDataSource().getType()) {
		/* jdbc:mysql://localhost:3306/dbname */
		case Constant.DBInfo.MYSQL:
			tableDao = new TableMysqlDAOImpl(adhoc);
			crossTableDao = new CrossTableMysqlDAOImpl(adhoc);
			break;

		/* jdbc:oracle:thin:@localhost:1521:orcl */
		case Constant.DBInfo.ORACLE:
			tableDao = new TableOracleDAOImpl(adhoc);
			crossTableDao = new CrossTableOracleDAOImpl(adhoc);
			break;

		/*
		 * jdbc:sqlserver://localhost:1433;
		 * databaseName=dbname
		 */
		case Constant.DBInfo.SQLSERVER:
			tableDao = new TableSqlserverDAOImpl(adhoc);
			crossTableDao = new CrossTableSqlserverDAOImpl(adhoc);
			break;

		default:
			logger.warn("unknow type property found: " + adhoc.getDatabase().getDataSource().getType());
			return;
		}
	}

	public DatabaseService(DataSourceImpl dataSource) {
		switch (dataSource.getType()) {
		/* jdbc:mysql://localhost:3306/dbname */
		case Constant.DBInfo.MYSQL:
			tableDao = new TableMysqlDAOImpl(dataSource);
			crossTableDao = new CrossTableMysqlDAOImpl(dataSource);
			break;

		/* jdbc:oracle:thin:@localhost:1521:orcl */
		case Constant.DBInfo.ORACLE:
			tableDao = new TableOracleDAOImpl(dataSource);
			crossTableDao = new CrossTableOracleDAOImpl(dataSource);
			break;

		/*
		 * jdbc:sqlserver://localhost:1433;
		 * databaseName=dbname
		 */
		case Constant.DBInfo.SQLSERVER:
			tableDao = new TableSqlserverDAOImpl(dataSource);
			crossTableDao = new CrossTableSqlserverDAOImpl(dataSource);
			break;

		default:
			logger.warn("unknow type property found: " + dataSource.getType());
			return;
		}
	}

	public static DatabaseInfo getDatabase(String domainUri) {
		return ExecuteAPIService.getDatabase(domainUri);
	}

	public TableGenericDAO getDAO() {
		return tableDao;
	}

	public void setDAO(TableGenericDAO dao) {
		this.tableDao = dao;
	}

	public String generateTableSQL(List<TableField> columns, List<TableField> groups) {
		return tableDao.generateSQL(columns, groups);
	}

	public void fillTableData(List<TableField> columns, List<TableViewData> tempData, boolean fullData) {
		tableDao.execute(tempData, fullData);
		List<TableField> calculatedFields = new ArrayList<>();
		columns.forEach(column -> {
			if (column.isCalculated()) {
				calculatedFields.add(column);
			}
		});
		if (!calculatedFields.isEmpty()) {
			tempData.add(calculateTableCell(calculatedFields, tempData));
		}
	}

	public double getMaxBitWidth(Field column) {
		if(column.getResourceId() == null) {
			return 0;
		}
		String maxFieldValue = tableDao.getMaxLength(column);
		return AdhocUtils.getLabelWidth(new Font("Meiryo Bold", 13), maxFieldValue);
	}

	public double getMaxWidth(Field column) {
		List<String> fieldValues = tableDao.getDistinctList(column);
		double max = 0;
		if (fieldValues != null) {
			for (String fieldValue : fieldValues) {
				max = Math.max(max, AdhocUtils.getLabelWidth(new Font("Meiryo Bold", 13), fieldValue));
			}
		}
		return max;
	}

	public List<String> getDistinctList(Field field) {
		// TODO
		List<String> fieldList = new ArrayList<String>();
		if (field.getCalculatedExpression() == null) {
			fieldList = tableDao.getDistinctList(field);
		} else {
			List<TableField> empty = new ArrayList<>();
			List<TableViewData> finalData = new ArrayList<>();
			//TODO 削除
			TableField test = new TableField(field, null, null);
			generateTableSQL(empty, Arrays.asList(test));
			fillTableData(empty, finalData, true);
			fieldList = finalData.stream().map(data -> data.getGroupKey().get(0).toString()).collect(Collectors.toList());
		}
		return fieldList;
	}

	public CTRowTrieTree initCrossTableData(
			List<CrossTableField> columns, List<CrossTableField> rows, List<CrossTableField> values,
			CTColumnTrieTree columnTree) {

		CTRowTrieTree tree = new CTRowTrieTree();
		tree.setDepth(rows.size());
		tree.setSqlStatement(crossTableDao.generateSQL(columns, rows, values));
		crossTableDao.execute(tree, columnTree);
		tree.generateTreeTotal();
		columnTree.setDepth(columns.size());
		columnTree.generateTreeTotal();
		return tree;
	}

	public TableViewData calculateTableCell(List<TableField> columns, List<TableViewData> tempData) {
		TableViewData data = new TableViewData();
		data.setLastRow(true);
		columns.forEach(field -> {
			data.addValue(field,  CalculateListMap.getCalculatedValue(field, tempData));
		});
		return data;
	}

	public String generateCalculate(List<TableField> columns, List<TableField> groups) {
		return tableDao.generateSQL(columns, groups);
	}
}
