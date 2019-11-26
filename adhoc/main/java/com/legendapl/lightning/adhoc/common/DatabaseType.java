package com.legendapl.lightning.adhoc.common;

/**
 * データベースの種類
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public enum DatabaseType {
	MYSQL("mysql", 0), SQLSERVER("sqlserver", 1), ORACLE("oracle", 2);

	private String name;
	private int index;

	private DatabaseType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public static DatabaseType getDatabaseType(String name) {
		for(DatabaseType databaseType : DatabaseType.values()) {
			if(databaseType != null && databaseType.name != null &&
					databaseType.name.equals(name)) {
				return databaseType;
			}
		}
		return null;
	}

	public int getIndex() {
		return index;
	}
}
