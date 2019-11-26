package com.legendapl.lightning.adhoc.dao;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.SortData;
import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.model.DataSourceImpl;

/**
 *　テーブルの共通のDAO
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({"unchecked"})
public abstract class TableGenericDAO extends GenericDAO{

	protected List<TableField> columns;
	protected List<TableField> groups;

	public TableGenericDAO(Adhoc adhoc) {
		super(adhoc);
	}

	public TableGenericDAO(DataSourceImpl dataSource) {
		super(dataSource);
	}

	public String generateSQL(List<TableField> columns, List<TableField> groups) {
		refreshSQL(columns, groups);
		this.columns = columns;
		this.groups = groups;
		if(resourceIds.isEmpty()) {
			return sqlStatement = "";
		}
		generateSQL();
		return sqlStatement;
	}

	abstract protected void generateSQL();

	public String getRealTableStatement(String tableId) {
		String tableName = adhoc.getDbTableIdToName().get(tableId);
		if(tableName.equals(tableId))
			return SQLUtils.DBsurround(tableId);
		return SQLUtils.DBsurround(tableName) + " " + SQLUtils.DBsurround(tableId);
	}

	abstract public void execute(List<TableViewData> tempData, boolean fullData);

	abstract public String getMaxLength(Field field);

	abstract public List<String> getDistinctList(Field field);
	
	abstract public List<String> getCalculateList(Field field);
	
	/**
	 * グループＳＱＬ文を生成
	 */
	protected String getGroupStatement() {
		
		List<String> list = new ArrayList<String>();
		getStream(groups).forEach(group -> {
			list.add(SQLUtils.selectAsField(group));
		});
		
		uniqueList(list);
		
		return list.toString().substring(1, list.toString().length() - 1);
	}
	
	/**
	 * ソートＳＱＬ文を生成
	 */
	protected String getSortStatement() {

		String sortParam = "";
		
		if (null != adhoc.getColumnSort()) {
			for (SortData sortData : adhoc.getColumnSort()) {
				
				if (inGroup(sortData)) continue;
				
				String ascOrDesc = sortData.getSortFlg() ? "ASC" : "DESC";
				sortParam += ", `" + sortData.getTableId() + "` .`" + sortData.getColumnId() + "` " + ascOrDesc;
			}
			
			if (!sortParam.isEmpty()) sortParam = sortParam.substring(1);
		}
		
		return sortParam;
	}
	
	boolean inGroup(SortData sortData) {
		for (TableField group : groups) {
			if (sortData.getResourceId().equals(group.getResourceId())) return true;
		}
		return false;
	}

}
