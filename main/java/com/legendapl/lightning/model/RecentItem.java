/**
 * 
 */
package com.legendapl.lightning.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

/**
 * 最近表示したアイテムのモデルクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class RecentItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String reportLabel;
	private String reportURI;
	private String user;
	private Date date;

	public RecentItem() {
		this("", "");
	}

	public RecentItem(String reportLabel, String reportURI) {
		this(reportLabel, reportURI, null, new Date());
	}

	public RecentItem(String reportLabel, String reportURI, String user) {
		this(reportLabel, reportURI, user, new Date());
	}

	public RecentItem(String reportLabel, String reportURI, String user, Date date) {
		this.reportLabel = reportLabel;
		this.reportURI = reportURI;
		this.user = user;
		this.date = date;
	}

	public RecentItem(TableRecord record) {
		this(record.getName().getValue(), record.getUri().getValue());
	}

	/**
	 * @return the reportLabel
	 */
	@XmlElement(name = "reportLabel")
	public String getReportLabel() {
		return reportLabel;
	}

	/**
	 * @return the reportURI
	 */
	@XmlElement(name = "reportURI")
	public String getReportURI() {
		return reportURI;
	}

	/**
	 * @return the user
	 */
	@XmlElement(name = "user")
	public String getUser() {
		return user;
	}

	/**
	 * @return the date
	 */
	@XmlElement(name = "date")
	public Date getDate() {
		return date;
	}

	/**
	 * @param reportLabel
	 *            the reportLabel to set
	 */
	public void setReportLabel(String reportLabel) {
		this.reportLabel = reportLabel;
	}

	/**
	 * @param reportURI
	 *            the reportURI to set
	 */
	public void setReportURI(String reportURI) {
		this.reportURI = reportURI;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RecentItemsList [reportLabel=" + reportLabel + ", reportURI=" + reportURI + ", user=" + user + ", date="
				+ date + "]";
	}

}
