package com.legendapl.lightning.adhoc.dao;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTRowTrieTree;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTColumnTrieTree;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;
import com.legendapl.lightning.model.DataSourceImpl;

/**
 *　クロス集計の共通のDAO
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({"unchecked"})
public abstract class CrossTableGenericDAO extends GenericDAO {

	protected List<CrossTableField> crossTableColumns;

	protected List<CrossTableField> crossTableRows;

	protected List<CrossTableField> crossTableValues;

	public CrossTableGenericDAO(Adhoc adhoc) {
		super(adhoc);
	}

	public CrossTableGenericDAO(DataSourceImpl dataSource) {
		super(dataSource);
	}

	public String generateSQL(List<CrossTableField> columns, List<CrossTableField> rows, List<CrossTableField> values) {
		refreshSQL(columns, rows, values);
		this.crossTableColumns = columns;
		this.crossTableRows = rows;
		this.crossTableValues = values;
		if(resourceIds.isEmpty()) {
			return sqlStatement = "";
		}
		generateSQL();
		return sqlStatement;
	}

	abstract protected void generateSQL();
	
	/**
	 * get unique order statement
	 * @return
	 */
	protected String getOrderStatement() {
		
		List<String> list = new ArrayList<String>();
		getStream(crossTableRows).forEach(field -> {
			list.add(SQLUtils.selectAsField(field));
		});
		getStream(crossTableColumns).forEach(field -> {
			list.add(SQLUtils.selectAsField(field));
		});
		
		uniqueList(list);
		
		return list.toString().substring(1, list.toString().length() - 1);
	}

	/**
	 * テーブルの名前を変更するため
	 *　@param tableId テーブルのID
	 * @return tableName( tableId)
	 */
	public String getRealTableStatement(String tableId) {
		String tableName = adhoc.getDbTableIdToName().get(tableId);
		if(tableName.equals(tableId))
			return SQLUtils.DBsurround(tableId);
		return SQLUtils.DBsurround(tableName) + " " + SQLUtils.DBsurround(tableId);
	}

	abstract public void execute(CTRowTrieTree tree, CTColumnTrieTree columnTree);

}
