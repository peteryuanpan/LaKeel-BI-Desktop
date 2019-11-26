package com.legendapl.lightning.tools.controller;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import com.jaspersoft.jasperserver.dto.authority.ClientRole;
import com.jaspersoft.jasperserver.dto.authority.ClientTenant;
import com.jaspersoft.jasperserver.dto.authority.ClientUser;
import com.jaspersoft.jasperserver.dto.authority.hypermedia.HypermediaAttribute;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.legendapl.lightning.tools.model.UserData;
import com.legendapl.lightning.tools.model.CsvRow;
import com.legendapl.lightning.tools.model.ProcessFlag;
import com.legendapl.lightning.tools.model.UserRow;
import com.legendapl.lightning.tools.service.EMailFormatCheckService;
import com.legendapl.lightning.tools.service.ExecuteAPIService;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.common.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

/**
 * ユーザ画面のコントローラクラス
 *
 * @author LAC_潘
 * @since 2017.09.07
 *
 */
public class P81UserAnchorPane extends P80BaseToolsAnchorPane {

	@FXML
	private JFXTreeTableView<UserRow> repositoryTable;
	@FXML
	private JFXTreeTableColumn<UserRow, String> username;
	@FXML
	private JFXTreeTableColumn<UserRow, String> password;
	@FXML
	private JFXTreeTableColumn<UserRow, String> name;
	@FXML
	private JFXTreeTableColumn<UserRow, String> organization;
	@FXML
	private JFXTreeTableColumn<UserRow, String> email;
	@FXML
	private JFXTreeTableColumn<UserRow, String> role;
	@FXML
	private JFXTreeTableColumn<UserRow, String> enable;
	@FXML
	private JFXTreeTableColumn<UserRow, String> property;
	@FXML
	private JFXTreeTableColumn<UserRow, String> status;

	private ObservableList<UserRow> userRowList = FXCollections.observableArrayList();
	private ObservableList<UserRow> userRowListBackup = FXCollections.observableArrayList();
	private UserData dataFromServer = new UserData();
	private UserData dataUpdated = new UserData();
	private List<CsvRow> csvRowList = new ArrayList<CsvRow>();
	private List<String> errorMessages = new ArrayList<String>();

	private List<JFXTreeTableColumn<UserRow, String>> columnsName = new ArrayList<JFXTreeTableColumn<UserRow, String>>();
	private List<String> functionsName = new ArrayList<String>();

	@Override
	public void init(URL location, ResourceBundle resources) {
		columnsName = new ArrayList<JFXTreeTableColumn<UserRow, String>>();
		columnsName.add(username);
		columnsName.add(password);
		columnsName.add(name);
		columnsName.add(organization);
		columnsName.add(email);
		columnsName.add(role);
		columnsName.add(enable);
		columnsName.add(property);
		columnsName.add(status);

		functionsName = new ArrayList<String>();
		functionsName.add(new String("getUsername"));
		functionsName.add(new String("getPassword"));
		functionsName.add(new String("getName"));
		functionsName.add(new String("getOrganization"));
		functionsName.add(new String("getEmail"));
		functionsName.add(new String("getRole"));
		functionsName.add(new String("getEnable"));
		functionsName.add(new String("getProperty"));
		functionsName.add(new String("getStatus"));

		getWithoutNotify();
	}
	
	@Override
	protected List<String> getHelpContent() {
		List<String> list = new ArrayList<String>();
		list.addAll(super.getHelpContent());
		list.add(Constants.P80_HELP_CONTENT_CSVFLAG_ADD_OR_UPDATE);
		return list;
	}

	/*
	 * 【CSVインポート】　押下
	 */
	protected boolean csvImportWork(List<CsvRow> csvRowListTmp) {
		if (csvRowListTmp == null) {
			logger.error("Import: Failed to import file.");
			return false;
		}
		logger.debug("Import: Target file is chosen.");
		
		if (!getWork()) {
			return false;
		}

		csvRowList = csvRowListTmp;
		if (!csvCheck()) {
			logger.error("Import: Failed to import file.");
			showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
			return false;
		}
		
		logger.debug("Import: userRowList to userRowListBackup");
		userRowListBackup.clear();
		userRowListBackup.addAll(userRowList);


		logger.debug("Import: Converting data from csv file to software.");
		dataUpdated = new UserData(dataFromServer);
		
		for (CsvRow csvRow : csvRowList) {
			String type = csvRow.get(Constants.P81_TYPE);
			if (type.equals(Constants.CSV_FLAG_NONE)) {
				continue;
			}
			
			String username = csvRow.get(Constants.P81_USERNAME);
			String organization = getStringNullToEmpty(csvRow.get(Constants.P81_ORGANIZATION));
			int id = findUserId(dataFromServer.getClientUserList(), username, organization);

			if (type.equals(Constants.CSV_FLAG_UPDATE_OR_ADD)) {
				type = (id < 0) ? Constants.CSV_FLAG_ADD : Constants.CSV_FLAG_UPDATE;
			}

			if (type.equals(Constants.CSV_FLAG_ADD)) {
				addDataSource(csvRow);
			}
			else if (type.equals(Constants.CSV_FLAG_DELETE)) {
				id = findUserId(dataUpdated.getClientUserList(), username, organization);
				dataUpdated.getClientUserList().remove(id);
				dataUpdated.getAttributeList().remove(id);
			}
			else if (type.equals(Constants.CSV_FLAG_UPDATE)) {
				updateDataSource(csvRow);
			}
		}
		
		for (CsvRow csvRow : csvRowList) {
			String type = csvRow.get(Constants.P81_TYPE);
			if (type.equals(Constants.CSV_FLAG_NONE)) {
				continue;
			}
			
			String username = csvRow.get(Constants.P81_USERNAME);
			String organization = getStringNullToEmpty(csvRow.get(Constants.P81_ORGANIZATION));
			int id = findUserId(dataFromServer.getClientUserList(), username, organization);
			if (type.equals(Constants.CSV_FLAG_UPDATE_OR_ADD)) {
				type = (id < 0) ? Constants.CSV_FLAG_ADD : Constants.CSV_FLAG_UPDATE;
			}

			if (type.equals(Constants.CSV_FLAG_ADD)) {
				id = findUserId(dataUpdated.getClientUserList(), username, organization);
				addUserRow(id);
				id = findUserId(userRowList, username, organization);
				userRowList.get(id).setFlag(ProcessFlag.ADD);
				userRowList.get(id).setStatus(Constants.P81_STATUS_ADD);
			}
			else if (type.equals(Constants.CSV_FLAG_DELETE)) {
				id = findUserId(userRowList, username, organization);
				userRowList.get(id).setFlag(ProcessFlag.DELETE);
				userRowList.get(id).setStatus(Constants.P81_STATUS_DELETE);
			}
			else if (type.equals(Constants.CSV_FLAG_UPDATE)) {
				id = findUserId(userRowList, username, organization);
				userRowList.remove(id);
				id = findUserId(dataUpdated.getClientUserList(), username, organization);
				addUserRow(id);
				id = findUserId(userRowList, username, organization);
				userRowList.get(id).setFlag(ProcessFlag.UPDATE);
				userRowList.get(id).setStatus(Constants.P81_STATUS_UPDATE);
			}
		}
		
		Collections.sort(userRowList);

		logger.debug("Import: Showing data to table.");
		showDataOnPlatform();
		
		logger.info("Import: Successed to import file.");
		setImportFlag(true);
		return true;
	}

	private void addDataSource (CsvRow csvRow) {
		ClientUser user = new ClientUser();
		String str = new String("");

		str = csvRow.get(Constants.P81_USERNAME);
		user.setUsername(str);

		str = csvRow.get(Constants.P81_ORGANIZATION);
		if (str.isEmpty()) {
			str = null;
		}
		user.setTenantId(str);

		str = csvRow.get(Constants.P81_PASSWORD);
		user.setPassword(str);

		str = csvRow.get(Constants.P81_NAME);
		if (str == null) {
			str = new String("");
		}
		user.setFullName(str);

		str = csvRow.get(Constants.P81_EMAIL);
		if (str == null) {
			str = new String("");
		}
		user.setEmailAddress(str);

		str = csvRow.get(Constants.P81_ROLE);
		if (str != null) {
			// 追加する時、[ロールID]に「ROLE_USER」が記載されていなくても含めます
			if (str.isEmpty()) {
				str = str.concat("ROLE_USER");
			}
			else if (!str.toLowerCase().contains("ROLE_USER".toLowerCase())){
				str = str.concat(";ROLE_USER");
			}
			
			String roleStrs[] = str.split(Pattern.quote(";"));
			Set<ClientRole> roleSet = new HashSet<ClientRole>();
			for(String roleStr : roleStrs) {
				ClientRole role = new ClientRole();
				String format[] = roleStr.split(Pattern.quote("|"));
				role.setName(format[0]);
				if (format.length == 1) {
					role.setTenantId(null);
				}
				else { // length == 2
					role.setTenantId(format[1]);
				}
				roleSet.add(role);
			}
			user.setRoleSet(roleSet);
		}

		str = csvRow.get(Constants.P81_ENABLE);
		if (str == null) {
			user.setEnabled(true);
		}
		else {
			user.setEnabled( str.equals(Constants.P81_ENABLE_FALSE) ? false : true );
		}

		List<HypermediaAttribute> attr = new ArrayList<HypermediaAttribute>();
		str = csvRow.get(Constants.P81_PROPERTY);
		if (str != null) {
			if (!str.isEmpty()) {
				String attrStrs[] = str.split(Pattern.quote(";"));
				for (String attrStr : attrStrs) {
					if (attrStr != null && !attrStr.isEmpty()) {
						HypermediaAttribute hyattr = new HypermediaAttribute();
						String name = attrStr.split(Pattern.quote("="))[0];
						String value = attrStr.split(Pattern.quote("="))[1];
						hyattr.setName(name);
						hyattr.setValue(value);
						attr.add(hyattr);
					}
				}
			}
			else { // to set empty
				attr = new ArrayList<HypermediaAttribute>();
			}
		}

		user.setExternallyDefined(false);

		dataUpdated.add(user, attr);
	}

	private void updateDataSource (CsvRow csvRow) {
		String username = csvRow.get(Constants.P81_USERNAME);
		String organization = getStringNullToEmpty(csvRow.get(Constants.P81_ORGANIZATION));
		int id = findUserId(dataUpdated.getClientUserList(), username, organization);

		ClientUser user = dataUpdated.getClientUserList().get(id);
		String str = new String("");

		str = csvRow.get(Constants.P81_PASSWORD);
		if (str != null && !str.isEmpty()) {
			user.setPassword(str);
		}

		str = csvRow.get(Constants.P81_NAME);
		if (str != null) {
			user.setFullName(str);
		}

		str = csvRow.get(Constants.P81_EMAIL);
		if (str != null) {
			user.setEmailAddress(str);
		}

		str = csvRow.get(Constants.P81_ROLE);
		if (str != null) {
			if (!str.isEmpty()) {
				String roleStrs[] = str.split(Pattern.quote(";"));
				Set<ClientRole> roleSet = new HashSet<ClientRole>();
				for(String roleStr : roleStrs) {
					ClientRole role = new ClientRole();
					String format[] = roleStr.split(Pattern.quote("|"));
					role.setName(format[0]);
					if (format.length == 1) {
						role.setTenantId(null);
					}
					else { // length == 2
						role.setTenantId(format[1]);
					}
					roleSet.add(role);
				}
				user.setRoleSet(roleSet);
			}
			else { // to set empty
				user.setRoleSet(new HashSet<ClientRole>());
			}
		}

		str = csvRow.get(Constants.P81_ENABLE);
		if (str != null) {
			user.setEnabled(str.equals(Constants.P81_ENABLE_FALSE) ? false : true);
		}

		dataUpdated.getClientUserList().set(id, user);

		List<HypermediaAttribute> attr = new ArrayList<HypermediaAttribute>();
		str = csvRow.get(Constants.P81_PROPERTY);
		if (str != null) {
			if (!str.isEmpty()) {
				String attrStrs[] = str.split(Pattern.quote(";"));
				for (String attrStr : attrStrs) {
					if (attrStr != null && !attrStr.isEmpty()) {
						HypermediaAttribute hyattr = new HypermediaAttribute();
						String name = attrStr.split(Pattern.quote("="))[0];
						String value = attrStr.split(Pattern.quote("="))[1];
						hyattr.setName(name);
						hyattr.setValue(value);
						attr.add(hyattr);
					}
				}
			}
			else { // to set empty
				attr = new ArrayList<HypermediaAttribute>();
			}
		}

		user.setExternallyDefined(false);

		dataUpdated.getAttributeList().set(id, attr);
	}

	private void addUserRow(int id) {
		userRowList.add( new UserRow(
				getStringNullToEmpty(dataUpdated.getClientUserList().get(id).getUsername()),
				getStringNullToEmpty(dataUpdated.getClientUserList().get(id).getTenantId()),
				getPasswordStringForTable(dataUpdated.getClientUserList().get(id)),
				getStringNullToEmpty(dataUpdated.getClientUserList().get(id).getFullName()),
				getStringNullToEmpty(dataUpdated.getClientUserList().get(id).getEmailAddress()),
				getRoleStringForTable(dataUpdated.getClientUserList().get(id)),
				dataUpdated.getClientUserList().get(id).isEnabled() ? Constants.P81_ENABLE_TRUE : Constants.P81_ENABLE_FALSE,
				getAttributeStringForTable(dataUpdated.getAttributeList().get(id))
				)
		);
	}

	private int findUserId(List<ClientUser> list, String username, String organization) {
		int id = -1;
		for (int i = 0; i < list.size(); i++) { // A = a
			if (list.get(i).getUsername().toLowerCase().equals(username.toLowerCase())) {
				if (organization.isEmpty() && list.get(i).getTenantId() == null) {
					id = i;
					break;
				}
				else if (!organization.isEmpty() && list.get(i).getTenantId() != null) {
					if (list.get(i).getTenantId().toLowerCase().equals(organization.toLowerCase())) {
						id = i;
						break;
					}
				}
			}
		}
		return id;
	}

	private int findUserId(ObservableList<UserRow> list, String username, String organization) {
		int id = -1;
		for (int i = 0; i < list.size(); i++) { // A = a
			if (list.get(i).getUsername().get().toLowerCase().equals(username.toLowerCase())) {
				if (list.get(i).getOrganization().get().toLowerCase().equals(organization.toLowerCase())) {
					id = i;
					break;
				}
			}
		}
		return id;
	}

	// CSV チェック
	private static final List<String> typeFormat = Arrays.asList(
			Constants.CSV_FLAG_NONE,
			Constants.CSV_FLAG_DELETE,
			Constants.CSV_FLAG_ADD,
			Constants.CSV_FLAG_UPDATE,
			Constants.CSV_FLAG_UPDATE_OR_ADD
	);
	private static final char usernameIllegalFormat[] = {' ', '\\',  '|',  '`',  '"',  '\'',  '~',  '!',  '#',  '$',  '%',  '^',  '&',  '*',  '+',  '=',  ';',  ':',  '?',  '<',  '>',  '}',  '{',  ')',  '(',  ']',  '[',  '/'};
	private static final char passwordIllegalFormat[] = {' '};
	private static final char propertyNameIllegalFormat[] = {'=', ';', '/', '\\'};
	private static final char propertyValueIllegalFormat[] = {'=', ';'};

	private boolean csvCheck() {
		errorMessages.clear();
		List<String> usernameList = new ArrayList<String>();
		List<String> organizationList = new ArrayList<String>();
		boolean flag = false;
		String type = new String("");
		String username = new String("");
		String organization = new String("");
		String password = new String("");
		String name = new String("");
		String email = new String("");
		String role = new String("");
		String enable = new String("");
		String property = new String("");

		// 処理フラグ  必須
		type = csvRowList.get(0).get(Constants.P81_TYPE);
		if (type == null) {
			// CSVのデータにカラム[0]が見つかりませんでした。
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_NO_COLUMN, Constants.P81_TYPE));
			flag = true;
		}

		// ユーザID  必須
		username = csvRowList.get(0).get(Constants.P81_USERNAME);
		if (username == null) {
			// CSVのデータにカラム[0]が見つかりませんでした。
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_NO_COLUMN, Constants.P81_USERNAME));
			flag = true;
		}

		if (flag) {
			return false;
		}
		
		// 処理フラグ  フォーマット
		boolean allIsCSVNoneFlag = true;
		for (int i = 0; i < csvRowList.size(); i++) {
			CsvRow csvRow = csvRowList.get(i);
			type = csvRow.get(Constants.P81_TYPE);
			if (!type.equals(Constants.CSV_FLAG_NONE)) {
				allIsCSVNoneFlag = false;
				break;
			}
		}
		if (allIsCSVNoneFlag) {
			// CSVのデータに処理すべきデータがありません。
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_FLAGS_ALL_EMPTY));
			flag = true;
		}
		
		if (flag) {
			return false;
		}

		for (int i = 0; i < csvRowList.size(); i++) {
			CsvRow csvRow = csvRowList.get(i);
			flag = false;

			type = csvRow.get(Constants.P81_TYPE);
			if (type.equals(Constants.CSV_FLAG_NONE)) {
				continue;
			}
			
			// 処理フラグ  フォーマット
			if (!inList(type, typeFormat)) {
				// {0}行：{1}の入力は{2}の中にいずれの値ではありません。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE, csvRow.getRowNo(), Constants.P81_TYPE, typeFormat));
				flag = true;
			}

			// ユーザID  入力必須
			username = csvRow.get(Constants.P81_USERNAME);
			if (username.isEmpty()) {
				// {0}行：{1}は必須入力です。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY, csvRow.getRowNo(), Constants.P81_USERNAME));
				flag = true;
			}

			if (flag) {
				continue;
			}

			organization = getStringNullToEmpty( csvRow.get(Constants.P81_ORGANIZATION) );

			// ユーザID  桁数
			if (username.length() > Integer.parseInt(Constants.P81_MAXLENGTH_USERNAME)) {
				// {0}行：{1}が{2}文字を超えています。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE, csvRow.getRowNo(), Constants.P81_USERNAME, Constants.P81_MAXLENGTH_USERNAME));
				flag = true;
			}

			// ユーザID  フォーマット
			if (outOfList(username, usernameIllegalFormat)) {
				// {0}行：{1}の入力に無効の文字を含めています。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_CHAR, csvRow.getRowNo(), Constants.P81_USERNAME));
				flag = true;
			}

			// ユーザID  重複チェック
			if (!csvCheckUsernameOrganizationDuplicated(username, usernameList, organization, organizationList)) {
				// {0}行：[1]の入力はほかの行に重複しています。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE, csvRow.getRowNo(), Constants.P81_USERNAME));
				flag = true;
			}
			usernameList.add(username);
			organizationList.add(organization);

			if (flag) {
				continue;
			}

			if (type.equals(Constants.CSV_FLAG_UPDATE_OR_ADD)) {
				if (!inClientUserList(username, organization, dataFromServer.getClientUserList())) { // add
					type = Constants.CSV_FLAG_ADD;
				}
				else { // update
					type = Constants.CSV_FLAG_UPDATE;
				}
			}

			// ユーザID 存在チェック      「処理フラグ」が「削除」または「更新」の場合
			if (type.equals(Constants.CSV_FLAG_DELETE) || type.equals(Constants.CSV_FLAG_UPDATE)) {
				if (!inClientUserList(username, organization, dataFromServer.getClientUserList())) {
					// {0}行：「{1}」の入力がサーバーに存在していません。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, csvRow.getRowNo(),
									  Constants.P81_USERNAME + "、" + Constants.P81_ORGANIZATION));
					flag = true;
				}
			}

			// ユーザID 存在チェック　    「処理フラグ」が「追加」の場合
			if (type.equals(Constants.CSV_FLAG_ADD)) {
				if (!inClientOrganizationList(organization)) {
					// {0}行：「{1}」の入力がサーバーに存在していません。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, csvRow.getRowNo(), Constants.P81_ORGANIZATION));
					flag = true;
				}
				else if (inClientUserList(username, organization, dataFromServer.getClientUserList())) {
					// {0}行：「{1}」の入力がサーバーにすでに存在しています。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_EXIST, csvRow.getRowNo(),
									  Constants.P81_USERNAME + "、" + Constants.P81_ORGANIZATION));
					flag = true;
				}
			}
			
			if (flag) {
				continue;
			}
			
			// ロールID  スーパーユーザチェック
			if (type.equals(Constants.CSV_FLAG_DELETE) || type.equals(Constants.CSV_FLAG_UPDATE)) {
				ClientUser clientUser = getClientUserFromList(username, organization, dataFromServer.getClientUserList());
				Set<ClientRole> clientRoleSet = clientUser.getRoleSet();
				for (ClientRole clientRole : clientRoleSet) {
					if (!csvCheckRoleContainSuperuser(clientRole.getName())) {
						// {0}行：「ROLE_SUPERUSER」のロールを持つユーザを変更できません。
						errorMessages.add(Utils.getString(Constants.P81_ERROR_USER_ASSIGNED_SUPERUSER, csvRow.getRowNo()));
						flag = true;
						break;
					}
				}
			}
			
			if (flag) {
				continue;
			}
			
			if (type.equals(Constants.CSV_FLAG_DELETE)) {
				continue;
			}
			
			password = csvRow.get(Constants.P81_PASSWORD);
			if (type.equals(Constants.CSV_FLAG_ADD)) {
				// パスワード  必須
				if (password == null) {
					// CSVのデータにカラム[0]が見つかりませんでした。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_NO_COLUMN, Constants.P81_PASSWORD));
				}
				// パスワード  入力必須
				else if (password.isEmpty()) {
					// {0}行：{1}は必須入力です。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY, csvRow.getRowNo(), Constants.P81_PASSWORD));
				}
			}

			if (password != null && !password.isEmpty()) {
				// パスワード  桁数
				if (password.length() > Integer.parseInt(Constants.P81_MAXLENGTH_PASSWORD)) {
					// {0}行：{1}が{2}文字を超えています。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE, csvRow.getRowNo(), Constants.P81_PASSWORD, Constants.P81_MAXLENGTH_PASSWORD));
				}

				// パスワード  フォーマット
				if (outOfList(password, passwordIllegalFormat)) {
					// {0}行：{1}の入力に無効の文字を含めています。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_CHAR, csvRow.getRowNo(), Constants.P81_PASSWORD));
				}
			}

			name = csvRow.get(Constants.P81_NAME);
			if (name != null && !name.isEmpty()) {
				// ユーザ名  桁数
				if (name.length() > Integer.parseInt(Constants.P81_MAXLENGTH_NAME)) {
					// {0}行：{1}が{2}文字を超えています。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE, csvRow.getRowNo(), Constants.P81_NAME, Constants.P81_MAXLENGTH_NAME));
				}
			}

			email = csvRow.get(Constants.P81_EMAIL);
			if (email != null && !email.isEmpty()) {
				// メール  桁数
				if (email.length() > Integer.parseInt(Constants.P81_MAXLENGTH_EMAIL)) {
					// {0}行：{1}が{2}文字を超えています。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE, csvRow.getRowNo(), Constants.P81_EMAIL, Constants.P81_MAXLENGTH_EMAIL));
				}

				// メール  フォーマット  A@B.C
				if (!csvCheckEmailFormat(email)) {
					// {0}行：{1}の形式に誤りがあります。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT, csvRow.getRowNo(), Constants.P81_EMAIL));
				}
			}

			role = csvRow.get(Constants.P81_ROLE);
			if (role != null && !role.isEmpty()) {
				// ロールID  フォーマット  
				if (!csvCheckRoleFormat(role)) {
					// {0}行：{1}の形式に誤りがあります。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT, csvRow.getRowNo(), Constants.P81_ROLE));
				}
				else {
					// ロールID  存在チェック
					if (!csvCheckRoleExsited(role)) {
						// {0}行：{1}の入力がサーバーに存在していません。
						errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, csvRow.getRowNo(), Constants.P81_ROLE));
					}
					
					// ロールID  フォーマット
					if (!csvCheckRoleDuplicated(role)){
						// {0}行：{1}は重複のデータを含めています。
						errorMessages.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE_ELEMENT, csvRow.getRowNo(), Constants.P81_ROLE));
					}
					
					// ロールID  スーパーユーザチェック
					if (!csvCheckRoleContainSuperuser(role)) {
						// {0}行：「ROLE_SUPERUSER」のロールを含めることはできません。
						errorMessages.add(Utils.getString(Constants.P81_ERROR_CSV_ROLEID_CONTAINS_SUPERUSER, csvRow.getRowNo()));
					}
				}
			}

			enable = csvRow.get(Constants.P81_ENABLE);
			if (enable != null) {
				// 有効フラグ  フォーマット
				if (!enable.isEmpty() && !enable.equals(Constants.P81_ENABLE_TRUE) && !enable.equals(Constants.P81_ENABLE_FALSE)) {
					// {0}行：{1}の入力は{2}の中にいずれの値ではありません。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE, csvRow.getRowNo(), Constants.P81_ENABLE,
													  Arrays.asList(Constants.P81_ENABLE_TRUE, Constants.P81_ENABLE_FALSE)));
				}
			}

			property = csvRow.get(Constants.P81_PROPERTY);
			if (property != null && !property.isEmpty()) {
				// 属性  フォーマット
				if (!csvCheckPropertyFormat(property)) {
					// {0}行：{1}の形式に誤りがあります。
					errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT, csvRow.getRowNo(), Constants.P81_PROPERTY));
				}
				else {
					// 属性  桁数
					if (!csvCheckPropertyNameLength(property)) {
						// {0}行：{1}が{2}文字を超えています。
						errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE, csvRow.getRowNo(), Constants.P81_PROPERTY+"名", Constants.P81_MAXLENGTH_PROPERTY_NAME));
					}
					
					// 属性    フォーマット
					if(!csvCheckPropertyDuplicated(property)){
						// {0}行：[1]の入力は重複のデータを含めています。
						errorMessages.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE_ELEMENT, csvRow.getRowNo(), Constants.P81_PROPERTY));
					}
				}
			}
		}

		return errorMessages.isEmpty() ? true : false;
	}
	
	private boolean csvCheckUsernameOrganizationDuplicated(String username, List<String>usernameList, String organization, List<String>organizationList) {
		for (int i = 0; i < usernameList.size(); i++) { // A = a
			if (usernameList.get(i).toLowerCase().equals(username.toLowerCase()) 
					&& organizationList.get(i).toLowerCase().equals(organization.toLowerCase())) {
				return false;
			}
		}
		return true;
	}

	private boolean csvCheckEmailFormat(String email) {
		EMailFormatCheckService service = new EMailFormatCheckService(email);
		return service.forUser();
	}
	
	private boolean csvCheckRoleFormat(String rolesName) {
		String roles[] = rolesName.split(Pattern.quote(";"));
		for (String role : roles) {
			int num = csvCheckFileSplitNumber(role);
			if (num == 0) {
				continue;
			}
			else if (num == 1) {
				String format[] = role.split(Pattern.quote("|"));
				if (format.length != 2 || format[0].isEmpty() || format[1].isEmpty()) {
					return false;
				}
			}
			else { // num > 1
				return false;
			}
		}
		return true;
	}
	
	private int csvCheckFileSplitNumber(String role) {
		int num = 0;
		for (int i = 0; i < role.length(); i++) {
			if (role.charAt(i) == '|') {
				num = num + 1;
			}
		}
		return num;
	}
	
	private boolean csvCheckPropertyFormat(String property) {
		String model_1[] = property.split(Pattern.quote(";"));
		for (int i = 0; i < model_1.length; i++) {
			String model_2[] = model_1[i].split(Pattern.quote("="));
			if (model_2.length != 2) {
				return false;
			}
			String name = model_2[0];
			if (name.isEmpty()) {
				return false;
			}
			for (int j = 0; j < name.length(); j++) {
				if (inList(name.charAt(j), propertyNameIllegalFormat)) {
					return false;
				}
			}

			String value = model_2[1];
			if (value.isEmpty()) {
				return false;
			}
			for (int j = 0; j < value.length(); j++) {
				if (inList(value.charAt(j), propertyValueIllegalFormat)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean csvCheckPropertyNameLength(String property) {
		String model_1[] = property.split(Pattern.quote(";"));
		for (int i = 0; i < model_1.length; i++) {
			String model_2[] = model_1[i].split(Pattern.quote("="));
			String name = model_2[0];
			if (name.length() > Integer.parseInt(Constants.P81_MAXLENGTH_PROPERTY_NAME)) {
				return false;
			}
		}
		return true;
	}

	private boolean csvCheckPropertyDuplicated(String property) {
		String model_1[] = property.split(Pattern.quote(";"));
		List<String> nameList = new ArrayList<String>();
		for (int i = 0; i < model_1.length; i++) {
			String name = model_1[i].split(Pattern.quote("="))[0];
			if (inList(name, nameList)) {
				return false;
			}
			nameList.add(name);
		}
		return true;
	}
	
	private boolean csvCheckRoleExsited(String rolesName) {
		List<ClientRole> list = ExecuteAPIService.getRoleList(null);
		String roles[] = rolesName.split(Pattern.quote(";"));
		for (String role : roles) {
			String format[] = role.split(Pattern.quote("|"));
			if (format.length == 1 && !format[0].isEmpty()) {
				if (!inClientRoleList(format[0], null, list)) {
					return false;
				}
			}
			else if (format.length == 2 && !format[0].isEmpty() && !format[1].isEmpty()) {
				if (!inClientRoleList(format[0], format[1], list)) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		return true;
	}

	private boolean csvCheckRoleDuplicated(String rolesName) {
		List<String> list = new ArrayList<String>();
		String roles[] = rolesName.split(Pattern.quote(";"));
		for (String role : roles) {
			role = role.toLowerCase(); // A = a
			if (inList(role, list)) {
				return false;
			}
			list.add(role);
		}
		return true;
	}
	
	private boolean csvCheckRoleContainSuperuser(String rolesName) {
		String roles[] = rolesName.split(Pattern.quote(";"));
		for (String role : roles) {
			String format[] = role.split(Pattern.quote("|"));
			if (format.length > 0 && format[0].toLowerCase().equals(Constants.P81_ROLE_SUPERUSER.toLowerCase())) {
				return false;
			}
		}
		return true;
	}
	
	private ClientUser getClientUserFromList(String name, String organization, List<ClientUser>list) {
		for (ClientUser user : list) { // A = a
			if (user.getUsername().toLowerCase().equals(name.toLowerCase())) {
				if (organization == null || organization.isEmpty()) {
					if (user.getTenantId() == null) {
						return user;
					}
				}
				else if (user.getTenantId() != null && user.getTenantId().toLowerCase().equals(organization.toLowerCase())){
					return user;
				}
			}
		}
		return null;
	}
	
	private boolean inClientUserList(String name, String organization, List<ClientUser>list) {
		for (ClientUser user : list) { // A = a
			if (user.getUsername().toLowerCase().equals(name.toLowerCase())) {
				if (organization == null || organization.isEmpty()) {
					if (user.getTenantId() == null) {
						return true;
					}
				}
				else if (user.getTenantId() != null && user.getTenantId().toLowerCase().equals(organization.toLowerCase())){
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean inClientOrganizationList(String organization) {
		List<ClientTenant> list = ExecuteAPIService.getOrganizationList();
		for (ClientTenant org : list) { // A = a
			if (org.getId() != null && org.getId().toLowerCase().equals(organization.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private boolean inClientRoleList(String roleName, String organization, List<ClientRole>list) {
		for (ClientRole role : list) { // A = a
			if (role.getName().toLowerCase().equals(roleName.toLowerCase())) {
				if (organization == null || organization.isEmpty()) {
					if (role.getTenantId() == null) {
						return true;
					}
				}
				else if (role.getTenantId() != null && role.getTenantId().toLowerCase().equals(organization.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean inList(String str, List<String>list) {
		for (String s : list) {
			if (s != null && s.equals(str)) {
				return true;
			}
		}
		return false;
	}

	private boolean inList(char c, char[] list) {
		for (int i = 0; i < list.length; i++) {
			if (c == list[i]) {
				return true;
			}
		}
		return false;
	}

	private boolean outOfList(String str, char[] list) {
		for (int i = 0; i < str.length(); i++) {
			for (int j = 0; j < list.length; j++) {
				if (str.charAt(i) == list[j]) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * 【CSVエクスポート】　押下
	 */
	protected boolean csvExportWork(File filename) {
		if (filename == null) {
			logger.debug("Export: Chosen file is null.");
			return false;
		}
		logger.debug("Export: Target file is chosen.");

		if (!getWork()) {
			return false;
		}

		logger.debug("Export: Converting data from software to csv file.");
		List<List<String>> saveFile = getSaveFileByRowList(userRowList);

		logger.debug("Export: Saving csv file.");
		if (!saveCsv(filename, saveFile)) {
			logger.debug("Export: Failed to save csv file.");
			return false;
		}
		
		showInfo(Utils.getString(Constants.DLG_INFO_EXPORT_SUCC));
		logger.info("Export: Successed to export file.");
		return true;
	}
	
	private String getPassWordStringForOutput(String pwStr) {
		if (pwStr.equals(Constants.P81_PASSWORD_SHOW)) {
			return new String("");
		}
		else {
			return pwStr;
		}
	}
	
	private List<List<String>> getSaveFileByRowList(ObservableList<UserRow> rowList) {
		List<List<String>> file = new ArrayList<List<String>>();
		List<String> listStr = new ArrayList<String>();

		listStr = new ArrayList<String>();
		listStr.add(Constants.P81_TYPE);
		listStr.add(Constants.P81_USERNAME);
		listStr.add(Constants.P81_PASSWORD);
		listStr.add(Constants.P81_NAME);
		listStr.add(Constants.P81_ORGANIZATION);
		listStr.add(Constants.P81_EMAIL);
		listStr.add(Constants.P81_ROLE);
		listStr.add(Constants.P81_ENABLE);
		listStr.add(Constants.P81_PROPERTY);
		file.add(listStr);

		for (int i = 0; i < rowList.size(); i++) {
			listStr = new ArrayList<String>();
			listStr.add(new String(""));
			listStr.add(getStringNullToEmpty(rowList.get(i).getUsername().get()));
			listStr.add(getPassWordStringForOutput(rowList.get(i).getPassword().get()));
			listStr.add(getStringNullToEmpty(rowList.get(i).getName().get()));
			listStr.add(getStringNullToEmpty(rowList.get(i).getOrganization().get()));
			listStr.add(getStringNullToEmpty(rowList.get(i).getEmail().get()));
			listStr.add(getStringNullToEmpty(rowList.get(i).getRole().get()));
			listStr.add(getStringNullToEmpty(rowList.get(i).getEnable().get()));
			listStr.add(getStringNullToEmpty(rowList.get(i).getProperty().get()));
			file.add(listStr);
		}
		
		return file;
	}

	/*
	 * 【取得】　押下
	 */
	protected boolean getWork() {
		try {
			logger.debug("Get: Getting data from server.");
			getDataFromServer();
		}
		catch (Exception e) {
			logger.error("Get: Failed to get data from server");
			logger.error(e.getMessage(), e);
			setGetFlag(false);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
			return false;
		}
		
		logger.debug("Get: Showing data to table.");
		showDataOnPlatform();
		
		setImportFlag(false);
		setGetFlag(true);
		logger.info("Get: Successed to get data from server.");
		return true;
	}

	private void showDataOnPlatform() {
		showData(userRowList, columnsName, functionsName, repositoryTable);
	}

	private void getDataFromServer() {
		userRowList = FXCollections.observableArrayList();
		dataFromServer = new UserData();
		dataFromServer.setClientUserList(ExecuteAPIService.getUserList());
		dataFromServer.setAttributeList( new ArrayList<List<HypermediaAttribute>>() );

		for ( int i = 0; i < dataFromServer.getClientUserList().size(); i++ ) {
			ClientUser user = ExecuteAPIService.getUserUnit( dataFromServer.getClientUserList().get(i).getTenantId(), dataFromServer.getClientUserList().get(i).getUsername() );
					dataFromServer.getClientUserList().get(i).setPassword(user.getPassword());
					dataFromServer.getClientUserList().get(i).setEmailAddress(user.getEmailAddress());
					dataFromServer.getClientUserList().get(i).setRoleSet(user.getRoleSet());
					dataFromServer.getClientUserList().get(i).setEnabled(user.isEnabled());
					dataFromServer.getAttributeList().add( ExecuteAPIService.getAttributeList (
							dataFromServer.getClientUserList().get(i).getTenantId(),
							dataFromServer.getClientUserList().get(i).getUsername()
					)
			);
		}

		for ( int i = 0; i < dataFromServer.getClientUserList().size(); i++ ) {
			userRowList.add( new UserRow(
					getStringNullToEmpty(dataFromServer.getClientUserList().get(i).getUsername()),
					getStringNullToEmpty(dataFromServer.getClientUserList().get(i).getTenantId()),
					getPasswordStringForTable(dataFromServer.getClientUserList().get(i)),
					getStringNullToEmpty(dataFromServer.getClientUserList().get(i).getFullName()),
					getStringNullToEmpty(dataFromServer.getClientUserList().get(i).getEmailAddress()),
					getRoleStringForTable(dataFromServer.getClientUserList().get(i)),
					dataFromServer.getClientUserList().get(i).isEnabled() ? Constants.P81_ENABLE_TRUE : Constants.P81_ENABLE_FALSE,
					getAttributeStringForTable(dataFromServer.getAttributeList().get(i))
					)
			);
		}

		Collections.sort(userRowList);
	}

	private String getStringNullToEmpty(String s) {
		if ( s == null ) {
			return new String("");
		}
		return s;
	}
	
	private String getPasswordStringForTable(ClientUser user) {
		String pwString = new String("");
		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			pwString = Constants.P81_PASSWORD_SHOW;
		}
		else {
			pwString = user.getPassword();
		}
		return pwString;
	}

	private String getRoleStringForTable(ClientUser user) {
		String roleString = new String("");
		Set<ClientRole> roleSet = user.getRoleSet();
		if (roleSet != null) {
			Iterator<ClientRole> it = roleSet.iterator();
			while (it.hasNext()) {
				ClientRole role = it.next();
				roleString = roleString.concat(role.getName());
				if (role.getTenantId() != null) {
					roleString = roleString.concat("|"+role.getTenantId());
				}
				if (it.hasNext()) {
					roleString = roleString.concat(";");
				}
			}
		}
		return roleString;
	}

	private String getAttributeStringForTable(List<HypermediaAttribute> list) {
		String attrString = new String("");
		for ( int i = 0; i < list.size(); i++ ) {
			attrString = attrString.concat(list.get(i).getName());
			attrString = attrString.concat("=");
			attrString = attrString.concat(list.get(i).getValue());
			if (i + 1 != list.size()) {
				attrString = attrString.concat(";");
			}
		}
		return attrString;
	}

	/*
	 * 【適用】　押下
	 */
	protected boolean applyWork() {
		logger.debug("Apply: Saving backup file.");
		List<List<String>> saveFile = getSaveFileByRowList(userRowListBackup);
		if (!saveBackup(saveFile)) {
			return false;
		}
		
		try {
			logger.debug("Apply: Applying data to server.");
			applyDataToServer();
		}
		catch (Exception e) {
			logger.error("Apply: Failed to apply data to server.");
			logger.error(e.getMessage(), e);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_APPLY), e);
			return false;
		}
		
		if (!getWork()) {
			return false;
		}
		
		showInfo(Utils.getString(Constants.DLG_INFO_APPLY_SUCC));
		logger.info("Apply: Successed to apply data to server.");
		return true;
	}

	private void applyDataToServer() {
		for (CsvRow csvRow : csvRowList) {
			String type = csvRow.get(Constants.P81_TYPE);
			if (type.equals(Constants.CSV_FLAG_NONE)) {
				continue;
			}
			
			String username = csvRow.get(Constants.P81_USERNAME);
			String organization = getStringNullToEmpty(csvRow.get(Constants.P81_ORGANIZATION));
			int id = findUserId(dataFromServer.getClientUserList(), username, organization);

			if (type.equals(Constants.CSV_FLAG_UPDATE_OR_ADD)) {
				type = (id < 0) ? Constants.CSV_FLAG_ADD : Constants.CSV_FLAG_UPDATE;
			}
			
			if (type.equals(Constants.CSV_FLAG_UPDATE) || type.equals(Constants.CSV_FLAG_DELETE)) {
				ClientUser clientUser = ExecuteAPIService.getUserUnit(organization, username);
				Set<ClientRole> clientRoleSet = clientUser.getRoleSet();
				for (ClientRole clientRole : clientRoleSet) {
					if (!csvCheckRoleContainSuperuser(clientRole.getName())) {
						throw new ApplyMeetSuperuserException(
								Utils.getString(Constants.P81_ERROR_USER_ASSIGNED_SUPERUSER, csvRow.getRowNo()));
					}
				}
			}

			if (type.equals(Constants.CSV_FLAG_ADD) || type.equals(Constants.CSV_FLAG_UPDATE)) {
				id = findUserId(dataUpdated.getClientUserList(), username, organization);
				ClientUser user = dataUpdated.getClientUserList().get(id);
				List<HypermediaAttribute> list = dataUpdated.getAttributeList().get(id);
				ExecuteAPIService.createOrUpdateUser(user);
				ExecuteAPIService.createOrUpdateAttribute(user, list);
			}
			else if (type.equals(Constants.CSV_FLAG_DELETE)) {
				ClientUser user = dataFromServer.getClientUserList().get(id);
				ExecuteAPIService.deleteUserAttribute(user);
			}
		}
	}
	
	public static class ApplyMeetSuperuserException extends RuntimeException {
		private static final long serialVersionUID = -1476776283838696235L;
		public ApplyMeetSuperuserException() {
			super();
		}
		public ApplyMeetSuperuserException(String s) {
			super(s);
		}
	}
}
