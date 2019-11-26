package com.legendapl.lightning.adhoc.adhocView.model;

import com.legendapl.lightning.adhoc.common.JoinType;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;

/**
 * データベースの中でテーブルの接続関係
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.03.22
 */
public class JoinDTO {
	private String condition;
	private String subTableId;
	private JoinType joinType;

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		if(condition.contains(" and ")) {
			String[] joins = condition.split(" and ");
			String tempCondition = "";
			for(int i=0;i<joins.length;i++) {
				tempCondition = tempCondition + parse(joins[i]) + " and ";
			}
			condition = tempCondition.substring(0, tempCondition.lastIndexOf(" and "));
		} else {
			condition = parse(condition);
		}
		this.condition = condition;
	}

	public String getSubTableId() {
		return subTableId;
	}

	public void setSubTableId(String subTableId) {
		this.subTableId = subTableId;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	public String parse(String join) {
		String[] tableIds = join.split(" == ");
		return SQLUtils.DBsurround(tableIds[0]) + " = " + SQLUtils.DBsurround(tableIds[1]);
	}

}
