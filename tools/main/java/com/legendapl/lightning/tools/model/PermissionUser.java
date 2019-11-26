package com.legendapl.lightning.tools.model;

public class PermissionUser {
	private String userName = "";
	private String organization = "";
	private boolean delete = false;
	private String uri = "";

	public PermissionUser(String userName, String organization) {
		this.userName = userName;
		this.organization = organization;
	}

	public PermissionUser(String userName, String organization, String uri) {
		this.userName = userName;
		this.organization = organization;
		this.uri = uri;
	}



	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public boolean equals(Object obj) {
		PermissionUser cmp = (PermissionUser) obj;
		boolean temp = false;
		if(!this.userName.isEmpty()) {
			if(this.userName.equalsIgnoreCase(cmp.getUserName()) && this.organization.equalsIgnoreCase(cmp.getOrganization())) {
				temp = true;
				if(!this.userName.equals(cmp.getUserName()))
					setUserName(cmp.getUserName());
				if(!this.organization.equals(cmp.getOrganization()))
					setOrganization(cmp.getOrganization());
			}
		}
		else {
			temp = this.organization.equalsIgnoreCase(cmp.getOrganization());
			if(temp) {
				if(!this.organization.equals(cmp.getOrganization()))
					setOrganization(cmp.getOrganization());
			}
		}

		if(cmp.getUri().isEmpty() || uri.isEmpty()) {
			return temp;
		} else {
			return temp && uri.equals(cmp.getUri());
		}
	}

	@Override
	public String toString() {
		return "PermissionUser [userName=" + userName + ", organization=" + organization + "]";
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}



}
