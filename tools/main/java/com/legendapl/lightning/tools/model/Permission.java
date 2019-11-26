package com.legendapl.lightning.tools.model;

import javax.xml.bind.annotation.XmlElement;

public class Permission extends BaseModel<Permission> implements Comparable<Permission>, Cloneable {
	private final StringBase roleName = new StringBase("");
	private final StringBase permission = new StringBase("");
	private final StringBase userName = new StringBase("");
	private final StringBase organization = new StringBase("");
	private final StringBase status = new StringBase("");
	private boolean isRoot = true;
	private boolean allCheck = false;
	private boolean inherited = true;

	public Permission() {
		super();
	}

	public Permission(String roleName, String userName, String permission) {
		if (roleName.isEmpty()) {
			this.permission.set(permission);
			this.userName.set(userName);
		} else if (userName.isEmpty()) {
			this.roleName.set(roleName);
			this.permission.set(permission);
		}
	}

	public Permission(String roleName, String userName, String permission, String organizaiton) {
		if (roleName.isEmpty()) {
			this.permission.set(permission);
			this.userName.set(userName);
			this.organization.set(organizaiton);
		} else if (userName.isEmpty()) {
			this.roleName.set(roleName);
			this.permission.set(permission);
			this.organization.set(organizaiton);
		}
		if (!organizaiton.isEmpty()) {
			isRoot = false;
		}
	}

	public Permission(String userOrRole, String name) {
		if (userOrRole.equalsIgnoreCase("user")) {
			this.userName.set(name);
		} else if (userOrRole.equalsIgnoreCase("role")) {
			this.roleName.set(name);
		}
	}

	@XmlElement(name = "roleName")
	public StringBase getRoleName() {
		return roleName;
	}

	@XmlElement(name = "permission")
	public StringBase getPermission() {
		if (inherited) {
			return new StringBase(permission.get() + "*");
		}
		return permission;
	}

	@XmlElement(name = "userName")
	public StringBase getUserName() {
		return userName;
	}

	@XmlElement(name = "organization")
	public StringBase getOrganization() {
		return organization;
	}

	@XmlElement(name = "status")
	public StringBase getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status.set(status);
	}

	public void setRoleName(String roleName) {
		this.roleName.set(roleName);
	}

	public void setPermission(String permission) {
		this.permission.set(permission);
	}

	public void setUserName(String userName) {
		this.userName.set(userName);
	}

	public void setOrganization(String organization) {
		this.organization.set(organization);
	}

	@Override
	public boolean equals(Object obj) {
		Permission cmp = (Permission) obj;
		if (!this.allCheck && !cmp.isAllCheck()) {
			if(isRole() == cmp.isRole() && cmp.getKey().equalsIgnoreCase(this.getKey())) {
				if(!cmp.getKey().equals(this.getKey())) {
					/*
					cmp.setRoleName(this.roleName.get());
					cmp.setUserName(this.userName.get());
					cmp.setOrganization(this.organization.get());
					*/
					setRoleName(cmp.getRoleName().get());
					setUserName(cmp.getUserName().get());
					setOrganization(cmp.getOrganization().get());
				}
				return true;
			}
			return false;
		}

		if(isRole() == cmp.isRole() && cmp.getKey().equalsIgnoreCase(this.getKey())
				&& cmp.getPermissionString().equals(getPermissionString())) {
			if(!cmp.getKey().equals(this.getKey())) {
				setRoleName(cmp.getRoleName().get());
				setUserName(cmp.getUserName().get());
				setOrganization(cmp.getOrganization().get());
			}
			return true;
		} else
			return false;
	}

	public boolean isRole() {
		return userName.get().isEmpty();
	}

	public boolean isRoot() {
		return isRoot;
	}

	public String getKey() {
		if (isRoot) {
			return roleName.get() + userName.get();
		} else {
			return organization.get() + "/" + roleName.get() + userName.get();
		}
	}

	public boolean isInherited() {
		return inherited;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	public boolean isAllCheck() {
		return allCheck;
	}

	public void setAllCheck(boolean allCheck) {
		this.allCheck = allCheck;
	}

	public String getPermissionString() {
		return permission.get();
	}

	@Override
	public int compareTo(Permission o) {
		return isRole() ? this.roleName.get().compareTo(o.getRoleName().get())
				: this.userName.get().compareTo(o.getUserName().get());
	}

	@Override
	public String toString() {
		return "Permission [roleName=" + roleName.get() + ", permission=" + permission.get() + ", userName="
				+ userName.get() + ", organization=" + organization.get() + ", allCheck=" + allCheck + ", flag=" + flag
				+ ", inherited=" + inherited
				+ "]";
	}

	public Permission getCopy() {
		Permission copy = new Permission();
		copy.setInherited(inherited);
		copy.setOrganization(organization.get());
		copy.setRoleName(roleName.get());
		copy.setPermission(permission.get());
		copy.setUserName(userName.get());
		return copy;
	}

	public Object clone() {
		Permission o = new Permission();
		o.setInherited(inherited);
		o.setOrganization(organization.get());
		o.setRoleName(roleName.get());
		o.setPermission(permission.get());
		o.setUserName(userName.get());
		o.setAllCheck(allCheck);
		o.setFlag(flag);
		o.setStatus(status.get());
		return o;
	}

}
