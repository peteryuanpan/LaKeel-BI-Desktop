package com.legendapl.lightning.xmlAdapter;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

import com.legendapl.lightning.adhoc.common.ModelType;

public class RecentItemElement {

	private String reportLabel;
	private String reportURI;
	private String user;
	private Date date;
	private ModelType adhocFromType;
	// TODO
	
	public RecentItemElement() {
		// TODO
	}
	
	@XmlElement(name = "reportLabel")
	public String getReportLabel() {
		return reportLabel;
	}
	
	public void setReportLabel(String reportLabel) {
		this.reportLabel = reportLabel;
	}
	
	@XmlElement(name = "reportURI")
	public String getReportURI() {
		return reportURI;
	}
	
	public void setReportURI(String reportURI) {
		this.reportURI = reportURI;
	}
	
	@XmlElement(name = "user")
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	@XmlElement(name = "date")
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	@XmlElement(name = "adhocFromType")
	public ModelType getAdhocFromType() {
		return adhocFromType;
	}
	
	public void setAdhocFromType(ModelType adhocFromType) {
		this.adhocFromType = adhocFromType;
	}
	
}
