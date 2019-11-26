package com.legendapl.lightning.adhoc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.AdhocField;
import com.legendapl.lightning.adhoc.calculate.field.CalculatedFunction;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.dao.util.TableGraph;
import com.legendapl.lightning.adhoc.factory.TableViewFactory;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.model.DataSourceImpl;

/**
 *　クロス集計とテーブルの共通のDAO
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class GenericDAO {
	protected DataSourceImpl dataSource;
	protected String sqlStatement = "";
	protected String domainWhereStatement = "";
	protected String adhocWhereStatement = "";
	protected String fromStatement = "";
	protected String selectStatement = "";
	protected String orderStatement = "";
	protected Adhoc adhoc;
	protected TableGraph tableGraph;
	protected Logger logger = Logger.getLogger(getClass());
	protected static final int sampleData = TableViewFactory.sampleData;
	protected Set<String> resourceIds = new HashSet<>();
	protected List<String> calculatedResourceIds = new ArrayList<>();
	protected List<String> tableList = new ArrayList<>();
	protected Map<String, Comparable> resourceId2Value = new HashMap<>();

	protected JexlContext jexlContext = new MapContext();

	protected JexlEngine jexl = new JexlEngine();

	protected Map<String, Object> jexlFunctions = Collections.unmodifiableMap(new HashMap<String, Object>() {

		/**
		 *
		 */
		private static final long serialVersionUID = 1424683646214532873L;
		{
			put("fun", new CalculatedFunction());
			put("field", resourceId2Value);
		}
	});

	GenericDAO(Adhoc adhoc) {
		this.dataSource = adhoc.getDatabase().getDataSource();
		this.adhoc = adhoc;
		tableGraph = new TableGraph(adhoc.getTopicTree().getJoins().size() * 2);
		//tableGraph.addFilterTables(adhoc.getTree().getFilter());
		tableGraph.addFilterTables(adhoc.getFilters());
		tableGraph.addEdges(adhoc.getTopicTree().getJoins());
		jexl.setFunctions(jexlFunctions);
	}

	public GenericDAO(DataSourceImpl dataSource) {
		this.dataSource = dataSource;
	}

	protected <R> R SQLExecute(String sql, SQLFun<R> sqlFun, R defaultValue) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsermame(), dataSource.getPassword());
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			R r = sqlFun.call(rs);
			rs.close();
			stmt.close();
			conn.close();
			return r;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.SingleAlert.showError(AdhocUtils.getString("SQL_EXECUTE_ERROR"), e.getMessage());
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return defaultValue;
	}

	public interface SQLFun<R> {
		R call(ResultSet rs) throws Exception;
	}

	protected void refreshSQL(List<? extends Field>... args) {
		sqlStatement = "";
		// DO NOT domainWhereStatement = "";
		adhocWhereStatement= "";
		fromStatement = "";
		selectStatement = "";
		orderStatement = "";
		resourceIds.clear();
		calculatedResourceIds.clear();
		tableList.clear();
		for(List<? extends Field> arg: args) {
			arg.forEach(field -> {
				if(field.getCalculatedExpression() != null) {
					parseCalculatedField(field);
				} else {
					resourceIds.add(field.getResourceId());
					tableList.add(field.getTableId());
				}
			});
		}
		if(!tableList.isEmpty() && !adhoc.getTopicTree().getJoins().isEmpty())
			tableGraph.addTableIds(tableList,adhoc.getFilters(), adhoc.getColumnSort());
	}

	protected void parseCalculatedField(Field field) {
		if(!calculatedResourceIds.contains(field.getResourceId())) {
			for(String resourceId: field.getSubFieldResourceId()) {
				if(adhoc.getFieldByResId(resourceId).getCalculatedExpression() != null) {
					parseCalculatedField(adhoc.getFieldByResId(resourceId));
				} else {
					resourceIds.add(resourceId);
					tableList.add(adhoc.getFieldByResId(resourceId).getTableId());
				}
			}
			calculatedResourceIds.add(field.getResourceId());
		}
	}
	
	protected void generateWhereStatement() {
		generateAdhocWhereStatement();
		String whereStatement = "";
		if (domainWhereStatement.isEmpty() && adhocWhereStatement.isEmpty()) {
			// DO NOTHING
		} else if (domainWhereStatement.isEmpty()) {
			whereStatement = "WHERE " + adhocWhereStatement + "\n";
		} else if (adhocWhereStatement.isEmpty()) {
			whereStatement = "WHERE " + domainWhereStatement  + "\n";
		} else {
			whereStatement = "WHERE " + domainWhereStatement + " AND " + adhocWhereStatement + "\n";
		}
		sqlStatement += whereStatement;
	}
	
	abstract protected void generateAdhocWhereStatement();
	
	/**
	 * get stream by List
	 * @param list
	 * @return
	 */
	protected <T extends AdhocField> Stream<T> getStream(List<T> list) {
		return list.stream().filter(t -> t.getCalculatedExpression() == null);
	}
	
	/**
	 * unique list
	 * @param list
	 * @return
	 */
	public static void uniqueList(List<String> list) {
		if (null != list) {
			Set<String> set = new HashSet<>();
			List<String> newList = new ArrayList<>();
			for (String s : list) {
				if (!set.contains(s)) {
					newList.add(s);
					set.add(s);
				}
			}
			list.clear();
			list.addAll(newList);
		}
	}
}
