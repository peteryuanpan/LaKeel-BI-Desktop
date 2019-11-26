package com.legendapl.lightning.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.legendapl.lightning.xmlAdapter.RecentItemAdpater;

/**
 * 単一のサーバ情報を格納するクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 */

public class ServerInformation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	@XmlElement(name = "serverName")
	private String serverName;

	@XmlElement(name = "address")
	private String address;

	@XmlElement(name = "port")
	private int port;

	@XmlElement(name = "BIServerName")
	private String BIServerName;

	private String organizationName;

	private String userName;

	private String password;

	private String status;
	
	private boolean useHttps = false;

	private List<RecentItem> recentItems = new ArrayList<RecentItem>();

	public ServerInformation() {
		this("", "", 8080, "lakeelbi", "", "", "", "", false);
	}

	public ServerInformation(String serverName, String address, int port, String BIServerName, String organizationName,
			String userName, String password, String status, boolean useHttps) {

		this.serverName = serverName;
		this.address = address;
		this.port = port;
		this.BIServerName = BIServerName;
		this.organizationName = organizationName;
		this.userName = userName;
		this.password = password;
		this.status = status;
		this.useHttps = useHttps;
	}

	/**
	 * @return the id
	 */
	@XmlAttribute(name = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the serverName
	 */
	public String getName() {
		return serverName;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the BIServerName
	 */
	public String getBIName() {
		return BIServerName;
	}

	/**
	 * @return the organizationName
	 */
	@XmlElement(name = "organizationName")
	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	/**
	 * @return the userName
	 */
	@XmlElement(name = "userName")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	@XmlElement(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlElement(name = "status")
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the items
	 */
	@XmlElementWrapper(name = "recentItems")
	@XmlElement(name = "recentItem")
	@XmlJavaTypeAdapter(RecentItemAdpater.class)
	public List<RecentItem> getRecentItems() {
		return recentItems;
	}

	/**
	 * add a RecentItem
	 */
	public void addRecentItem(TableRecord record) {
		this.recentItems.add(0, new RecentItem(record));
	}

	/**
	 * clear RecentItem
	 */
	public void clearRecentItem() {
		this.recentItems.clear();
	}
	
	/**
	 * @return useHttps
	 */
	@XmlElement(name = "useHttps")
	public boolean getUseHttps() {
		return useHttps;
	}
	
	public void setUseHttps(boolean useHttps) {
		this.useHttps = useHttps;
	}
	
	/**
	 * @return [http|https]://[address]:[port]/[BIServerName]
	 * @author panyuan
	 */
	@XmlTransient
	public String getUrl() {
		return (useHttps ? "https" : "http") + "://" + 
			   address + ":" + port + "/" + BIServerName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerInformation [id=" + id + ", serverName=" + serverName + ", address=" + address + ", port=" + port
				+ ", BIServerName=" + BIServerName + ", organizationName=" + organizationName + ", userName=" + userName
				+ ", password=**" + ", status=" + status + ", useHttps=" + useHttps +"]";
	}

}
