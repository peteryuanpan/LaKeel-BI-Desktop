package com.legendapl.lightning.adhoc.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"alias", "referenceId"})
public class JoinInfo {
	
	private String alias;
	private String referenceId;
	
	public JoinInfo() {
		super();
		alias = new String();
		referenceId = new String();
	}
	
	public JoinInfo(String alias, String referenceId) {
		super();
		this.alias = alias;
		this.referenceId = referenceId;
	}
	
	public JoinInfo(JoinInfo joinInfo) {
		super();
		this.setAlias(joinInfo.getAlias());
		this.setReferenceId(joinInfo.getReferenceId());
	}

	@XmlAttribute(name = "alias")
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	@XmlAttribute(name = "referenceId")
	public String getReferenceId() {
		return referenceId;
	}
	
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	
}
