package com.legendapl.lightning.adhoc.common;

/**
 *　データベースのテーブルの接続タイプ
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.03.12
 */
public enum JoinType {

	Unknow(""),
	LeftOuter("left outer join"),
	RightOuter("right outer join"),
	Inner("join"),
	// TODO ?
	Full("full join");

	String name;

	private JoinType(String name) {
		this.name = name;
	}

	public static JoinType getJoinType(String name) {
		for(JoinType joinType : JoinType.values()) {
			if(joinType != null && joinType.name != null &&
					joinType.name.equals(name)) {
				return joinType;
			}
		}
		return null;
	}
}
