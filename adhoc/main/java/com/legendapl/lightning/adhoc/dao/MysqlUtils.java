package com.legendapl.lightning.adhoc.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.filter.FilterStatementUtil;
import com.legendapl.lightning.adhoc.filter.MysqlFilterSQLImpl;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.AlertWindowService;

public class MysqlUtils {

	private GenericDAO dao;

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	static {
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			AdhocUtils.logger.error(e.getMessage(), e);
			AlertWindowService.showError("MysqlUtils failed.", e.getMessage());
		}
	}

	public MysqlUtils(GenericDAO dao) {
		this.dao = dao;
	}

	protected void MysqlFilter() {
		MysqlFilterSQLImpl filterParse = MysqlFilterSQLImpl.getInstance();
		Set<String> domainTableIdSet = new HashSet<>();
		Adhoc adhoc = dao.adhoc;
		String filterString = adhoc.getTopicTree().getFilter();
		if (!filterString.isEmpty()) {
			// 大文字でしないで
			String[] filters = adhoc.getTopicTree().getFilter().split(" and ");
			for (String singleFilter : filters) {
				Filter filter = FilterStatementUtil.parseFilter(singleFilter);
				if(filter.getFieldId().contains(".")) {
					domainTableIdSet.add(adhoc.getTopicTree().getFieldByResId(adhoc.getTopicTree().getId() + "." + filter.getFieldId()).getTableId());
				}
				dao.domainWhereStatement += filterParse.filter2SQL(filter) + " AND ";
			}
			dao.tableGraph.setFilterTableIds(domainTableIdSet);
			if (!dao.domainWhereStatement.isEmpty()) {
				dao.domainWhereStatement = dao.domainWhereStatement.substring(0, dao.domainWhereStatement.lastIndexOf(" AND "));
			}
		} else {
			dao.domainWhereStatement = "";
		}
	}

	protected void generateSelectStatement() {
		Set<String> fieldNames = new HashSet<String>();
		for(String resourceId: dao.resourceIds) {
			Field field = dao.adhoc.getFieldByResId(resourceId);
			String fiedName = SQLUtils.DBsurround(field.getTableId() + "." + field.getFieldDBName()) + " AS " + SQLUtils.selectAsField(field);
			fieldNames.add(fiedName);
		}
		dao.selectStatement += "SELECT\n";
		fieldNames.forEach(fieldName -> {
			dao.selectStatement += fieldName + ",\n";
		});
		dao.selectStatement = dao.selectStatement.substring(0, dao.selectStatement.length()-2) + "\n";
		dao.sqlStatement += dao.selectStatement;
	}

	/**
	 *  SQL作成
	 * @param filters
	 * @param FilterConnect
	 * @return
	 */
	public static String getFilterConnect(List<Filter> filters, String FilterConnect) {
		String whereSql = "";
		if (filters.size() > 0) {
			String adhocWhereStatement = "";
			MysqlFilterSQLImpl filterParse = MysqlFilterSQLImpl.getInstance();
			if (!FilterConnect.isEmpty()) {
				String[] inputItem = FilterConnect.split(" ");
				for (int i = 0; i < inputItem.length; i++) {
					String inputParm = inputItem[i].toString();
					String adhocWhereSql = "";
					if ("and".equals(inputParm) || "or".equals(inputParm) || "not".equals(inputParm)
							|| "(not".equals(inputParm)) {
						adhocWhereSql = inputParm;
					} else {
						// 参数の場合
						Boolean leftbrackets = false;
						Boolean rightbrackets = false;
						if (inputParm.substring(0, 1).equals("(")
								&& inputParm.substring(inputParm.length() - 1, inputParm.length()).equals(")")) {
							inputParm = inputParm.substring(1, inputParm.length() - 1);
						} else if (inputParm.substring(0, 1).equals("(")) {
							inputParm = inputParm.substring(1, inputParm.length());
							leftbrackets = true;
						} else if (inputParm.substring(inputParm.length() - 1, inputParm.length()).equals(")")) {
							inputParm = inputParm.substring(0, inputParm.length() - 1);
							rightbrackets = true;
						}
						int filterNumber = changeOrderNum(inputParm);
						Filter filter = filters.get(filterNumber);
						changeNotValue(filter);
						adhocWhereSql = filterParse.filter2SQL(filter);
						if (leftbrackets) {
							adhocWhereSql = "(" + adhocWhereSql;
						} else if (rightbrackets) {
							adhocWhereSql = adhocWhereSql + ")";
						}
					}
					adhocWhereStatement = adhocWhereStatement + " " + adhocWhereSql;
				}
			} else {
				for (int i = 0; i < filters.size(); i++) {
					String stringSQL = new String();
					Filter filter = filters.get(i);
					stringSQL = filterParse.filter2SQL(filter);
					if (adhocWhereStatement.isEmpty()) {
						adhocWhereStatement = stringSQL;
					} else {
						if (!stringSQL.isEmpty()) {
							adhocWhereStatement = adhocWhereStatement + " AND " + stringSQL;
						}
					}
				}
			}
			whereSql = "(" + adhocWhereStatement.trim() + ")";
		}
		return whereSql;
	}

	/**
	 * char が intに変更
	 * @param firstNum
	 * @return
	 */
	public static int changeOrderNum(String str) {
		int res = 0;
		int t = 1;
		for (int i = str.length() - 1; i >= 0; i--) {
			char c = str.charAt(i);
			res = res + t * (c - 'A' + 1);
			t = t * 26;
		}
		res = res - 1;
		return res;
	}

	public static void changeNotValue (Filter filter) {
		filter.setOp(filter.getOp());
		
	}
}
