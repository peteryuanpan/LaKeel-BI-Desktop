package com.legendapl.lightning.adhoc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"sql"})
public class Join {

	private String sql;
	
	public Join() {
		super();
		sql = new String();
	}

	public Join(String sql) {
		super();
		this.sql = sql;
	}
	
	public Join(Join join) {
		this.setSql(join.getSql());
	}

	@XmlElement(name = "joinString")
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
	
}
