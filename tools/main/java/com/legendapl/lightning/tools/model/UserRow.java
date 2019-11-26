package com.legendapl.lightning.tools.model;

import com.legendapl.lightning.tools.common.Constants;

/**
 * 
 * @author LAC_潘
 * @since 2017/9/7
 *
 */
public class UserRow extends BaseModel<UserRow> implements Comparable<UserRow> {
	private StringBase username = new StringBase("");
	private StringBase organization = new StringBase("");
	private StringBase password = new StringBase("");
	private StringBase name = new StringBase("");
	private StringBase email = new StringBase("");
	private StringBase role = new StringBase("");
	private StringBase enable = new StringBase("");
	private StringBase property = new StringBase("");
	private StringBase status = new StringBase("");

	public UserRow() {
		super();
	}

	public UserRow(String username, String organization, String password, String name, String email, String role, String enable, String property) {
		setUsername(username);
		setPassword(password);
		setName(name);
		setOrganization(organization);
		setEmail(email);
		setRole(role);
		setEnable(enable);
		setProperty(property);
		setStatus(Constants.P81_STATUS_NONE);
	}
	
	@Override
	public boolean equals(Object user_) {
		UserRow user = (UserRow) user_;
		return username.get().equals( user.getUsername().get() ) && 
			   organization.get().equals( user.getOrganization().get() );
	}
	
	@Override
	public int compareTo(UserRow user) {
		int res = username.get().compareTo( user.getUsername().get() );
		if (res == 0) {
			return organization.get().compareTo( user.getOrganization().get() );
		}
		else {
			return res;
		}
	}

	
	public StringBase getUsername() {
		return this.username;
	}
	public UserRow setUsername(String usernameStr) {
		username.set(usernameStr);
		return this;
	}


	public StringBase getPassword() {
		return this.password;
	}
	public UserRow setPassword(String passwordStr) {
		password.set(passwordStr);
		return this;
	}
	

	public StringBase getName() {
		return name;
	}
	public UserRow setName(String nameStr) {
		name.set(nameStr);
		return this;
	}


	public StringBase getOrganization() {
		return this.organization;
	}
	public UserRow setOrganization(String organizationStr) {
		organization.set(organizationStr);
		return this;
	}


	public StringBase getEmail() {
		return this.email;
	}
	public UserRow setEmail(String emailStr) {
		email.set(emailStr);
		return this;
	}


	public StringBase getRole() {
		return this.role;
	}
	public UserRow setRole(String roleStr) {
		role.set(roleStr);
		return this;
	}


	public StringBase getEnable() {
		return enable;
	}
	public UserRow setEnable(String enableStr) {
		enable.set(enableStr);
		return this;
	}
	

	public StringBase getProperty() {
		return property;
	}
	public UserRow setProperty(String propertyStr) {
		property.set(propertyStr);
		return this;
	}
	
	
	public StringBase getStatus() {
		return status;
	}
	public UserRow setStatus(String statusStr) {
		status.set(statusStr);
		return this;
	}
}
