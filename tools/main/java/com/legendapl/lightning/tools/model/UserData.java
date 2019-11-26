package com.legendapl.lightning.tools.model;

import java.util.ArrayList;
import java.util.List;

import com.jaspersoft.jasperserver.dto.authority.ClientUser;
import com.jaspersoft.jasperserver.dto.authority.hypermedia.HypermediaAttribute;

/**
 * 
 * @author LAC_潘
 * @since 2017/9/11
 */
public class UserData {
	 private List<ClientUser> clientUserList;
	 private List<List<HypermediaAttribute>> attributeList;
	 
	 public UserData() {
		 clientUserList = new ArrayList<ClientUser>();
		 attributeList = new ArrayList<List<HypermediaAttribute>>();
	 }
	 
	 public UserData(UserData other) {
		 clientUserList = new ArrayList<ClientUser>();
		 attributeList = new ArrayList<List<HypermediaAttribute>>();
		 clientUserList.addAll(other.getClientUserList());
		 attributeList.addAll(other.getAttributeList());
	 }
	 
	 public UserData(List<ClientUser> clientUserList_, List<List<HypermediaAttribute>> attributeList_) {
		 clientUserList = new ArrayList<ClientUser>();
		 attributeList = new ArrayList<List<HypermediaAttribute>>();
		 clientUserList.addAll(clientUserList_);
		 attributeList.addAll(attributeList_);
	 }
	 
	 public UserData add(ClientUser clientUserList_, List<HypermediaAttribute> attributeList_) {
		 clientUserList.add(clientUserList_);
		 attributeList.add(attributeList_);
		 return this;
	 }
	 
	 
	 public List<ClientUser> getClientUserList() {
		 return clientUserList;
	 }
	 public UserData setClientUserList (List<ClientUser> clientUserList_) {
		 clientUserList = clientUserList_;
		 return this;
	 }
	 
	 
	 public List<List<HypermediaAttribute>> getAttributeList() {
		 return attributeList;
	 }
	 public UserData setAttributeList (List<List<HypermediaAttribute>> attributeList_) {
		 attributeList = attributeList_;
		 return this;
	 }
}