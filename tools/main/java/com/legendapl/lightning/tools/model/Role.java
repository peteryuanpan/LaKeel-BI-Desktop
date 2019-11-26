package com.legendapl.lightning.tools.model;

import javax.xml.bind.annotation.XmlElement;

public class Role extends BaseModel<Role> implements Comparable<Role> {

	private final StringBase roleId = new StringBase("");
	private final StringBase organization = new StringBase("");
	private final StringBase status = new StringBase("");
	private final StringBase newRoleId = new StringBase("");
	private boolean delete = false;
	private String uri = "";

	public Role() {
		super();
	}

	public Role(String roleId, String organization) {
		setRoleId(roleId);
		setOrganization(organization);
	}

	public Role(String roleId, String organization, String uri) {
		setRoleId(roleId);
		setOrganization(organization);
		this.uri = uri;
	}


	public void setNewRoleId(String newRoleId) {
		this.newRoleId.set(newRoleId);
	}

	@XmlElement(name = "newRoleId")
	public StringBase getNewRoleId() {
		return newRoleId;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Role(String roleId) {
		setRoleId(roleId);
	}

	public Role(String roleId, String organization, ProcessFlag flag) {
		setRoleId(roleId);
		setOrganization(organization);
		this.flag = flag;
	}

	private void setOrganization(String organization) {
		this.organization.set(organization);

	}

	@XmlElement(name = "organization")
	public StringBase getOrganization() {
		return this.organization;
	}

	public void setStatus(String status) {
		this.status.set(status);
	}

	@XmlElement(name = "status")
	public StringBase getStatus() {
		return this.status;
	}

	public void setRoleId(String roleId) {
		this.roleId.set(roleId);
	}

	@XmlElement(name = "roleId")
	public StringBase getRoleId() {
		return this.roleId;
	}

	@Override
	public boolean equals(Object roleCmp) {
		Role cmp = (Role) roleCmp;
		boolean temp;
		if (!this.roleId.get().isEmpty() && this.roleId.get().equalsIgnoreCase(cmp.getRoleId().get())
				&& this.organization.get().equalsIgnoreCase(cmp.getOrganization().get())) {
			if(!this.roleId.get().equals(cmp.getRoleId().get()))
				setRoleId(cmp.getRoleId().get());
			if(!this.organization.get().equals(cmp.getOrganization().get()))
				setOrganization(cmp.getOrganization().get());
			temp =  true;
		}

		else if (this.roleId.get().isEmpty()) {
			temp =  this.organization.get().equalsIgnoreCase(cmp.getOrganization().get());
			if(temp && !this.organization.get().equals(cmp.getOrganization().get())) {
				setOrganization(cmp.getOrganization().get());
			}
		} else
			temp =  false;
		if(uri.isEmpty() || cmp.getUri().isEmpty()) {
			return temp;
		} else {
			return temp && uri.equals(cmp.getUri());
		}
	}

	@Override
	public int compareTo(Role o) {
		int org = this.getOrganization().get().compareTo(o.getOrganization().get());
		if (org == 0) {
			return this.getRoleId().get().compareTo(o.getRoleId().get());
		} else
			return org;
	}

	@Override
	public String toString() {
		return "Role [roleId=" + roleId + ", organization=" + organization + "]";
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}


}
