
package com.legendapl.lightning.tools.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.jaspersoft.jasperserver.dto.authority.ClientRole;
import com.jaspersoft.jasperserver.dto.authority.ClientTenant;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.common.IgnoreCaseUtil;
import com.legendapl.lightning.tools.common.Utils;
import com.legendapl.lightning.tools.model.CsvRow;
import com.legendapl.lightning.tools.model.ProcessFlag;
import com.legendapl.lightning.tools.model.Role;
import com.legendapl.lightning.tools.service.ExecuteAPIService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

/**
 * ロール画面のコントローラクラス
 *
 * @author LAC_徐
 * @since 2017.09.06
 *
 */
public class P82RoleAnchorPane extends P80BaseToolsAnchorPane {

	@FXML
	private JFXTreeTableView<Role> roleTable;
	@FXML
	private JFXTreeTableColumn<Role, String> roleId;
	@FXML
	private JFXTreeTableColumn<Role, String> newRoleId;
	@FXML
	private JFXTreeTableColumn<Role, String> organizationId;
	@FXML
	private JFXTreeTableColumn<Role, String> status;
	@FXML
	private static final ObservableList<Role> role = FXCollections.observableArrayList();

	private static final ObservableList<Role> serverRole = FXCollections.observableArrayList();
	private static final List<Role> changeRoles = new ArrayList<Role>();

	private final String SIGNAL = Constants.P82_TABLE_COLUMN_SIGNAL;
	private final String ID = Constants.P82_TABLE_COLUMN_ROLEID;
	private final String NEWID = Constants.P82_TABLE_COLUMN_NEWROLEID;
	private final String ORGANIZATION = Constants.P82_TABLE_COLUMN_ORGANIZAITON;
	private static final int MAXCHAR = 100;
	private final Map<Integer, String> ROWNAME = new HashMap<Integer, String>();
	private final List<String> errorMessage = new ArrayList<String>();
	private final List<String> errorMessages = new ArrayList<String>();
	private final List<JFXTreeTableColumn<Role, String>> columns = new ArrayList<JFXTreeTableColumn<Role, String>>();
	private final List<String> functions = new ArrayList<String>();
	private final IgnoreCaseUtil organizationList = new IgnoreCaseUtil();
	private boolean update = false;
	private final List<Role> roleCheckList = new ArrayList<Role>();
	private boolean hasNewRoleIdRow = false;

	@Override
	public void init(URL location, ResourceBundle resources) {
		// preferencesを読み込み
		ROWNAME.put(0, SIGNAL);
		ROWNAME.put(1, ID);
		ROWNAME.put(2, ORGANIZATION);
		ROWNAME.put(3, NEWID);
		applyButton.setDisable(true);
		columns.add(roleId);
		columns.add(newRoleId);
		columns.add(organizationId);
		columns.add(status);
		functions.add("getRoleId");
		functions.add("getNewRoleId");
		functions.add("getOrganization");
		functions.add("getStatus");
		loadPreferences();
		logger.debug("initialization successed.");
		getWithoutNotify();
		logger.debug("initialization successed and data has been loaded.");
	}

	private boolean loadData() {
		clear();
		serverRole.clear();
		try {
			logger.debug("Requesting server for roles.");
			List<ClientRole> roleList = ExecuteAPIService.getRoleUnit();
			logger.debug("Roles has been loaded from server.");
			if (roleList.isEmpty())
				return false;
			for (int i = 0; i < roleList.size(); i++) {
				ClientRole clientRole = roleList.get(i);
				if (clientRole.getTenantId() != null) {
					serverRole.add(new Role(clientRole.getName(), clientRole.getTenantId()));
				} else {
					serverRole.add(new Role(clientRole.getName()));
				}
			}
		} catch (Exception e) {
			setGetFlag(false);
			clear();
			logger.debug("Syncing roles failed.");
			logger.error(e.getMessage(), e);
			return false;
		}
		role.addAll(serverRole);
		showData(role, columns, functions, roleTable);
		setImportFlag(false);
		setGetFlag(true);
		logger.debug("Roles are in sync with server.");
		return true;
	}

	/*
	 * 【CSVインポート】 押下
	 */
	public boolean csvImportWork(List<CsvRow> csvRows) {
		logger.debug("Import button has been pressed.");
		if (!getWork()) {
			logger.error("Import: Failed to import file.");
			return false;
		}
		logger.debug("Data has been synced with server before importing csv.");
		update = false;
		hasNewRoleIdRow = false;
		for(CsvRow readRole : csvRows) {
			if(readRole.get(SIGNAL) == null) {
				hasNewRoleIdRow = false;
				break;
			} else if(readRole.get(SIGNAL).equals("0")) {
				hasNewRoleIdRow = true;
				update = true;
				break;
			} else {
				hasNewRoleIdRow = true;
			}
		}
		if(!validateHead(csvRows.get(0), update)) {
			logger.error("Import: Failed to import file.");
			return false;
		}
		update = false;
		List<String[]> roleT = new ArrayList<String[]>();
		for (CsvRow readRole : csvRows) {
			String[] singleRow = new String[5];
			singleRow[0] = readRole.get(SIGNAL);
			if(singleRow[0].isEmpty()) {
				continue;
			}
			singleRow[1] = readRole.get(ID);
			if(readRole.get(ORGANIZATION) == null)
				singleRow[2] = "";
			else
				singleRow[2] = readRole.get(ORGANIZATION);
			singleRow[3] = readRole.get(NEWID);
			singleRow[4] = "" + readRole.getRowNo();
			roleT.add(singleRow);
		}
		if(roleT.isEmpty()) {
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_FLAGS_ALL_EMPTY));
			showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
			return false;
		}
		logger.debug("Csv data is ready to be validated.");
		roleCheckList.clear();
		for (int i = 0; i < roleT.size(); i++) {
			String[] item = roleT.get(i);
			if (validate(item)) {
				Role toChange = new Role(item[1], item[2]);
				if (item[0].equals(Constants.CSV_FLAG_DELETE)) {
					role.remove(toChange);
					toChange.setFlag(ProcessFlag.DELETE);
					toChange.setStatus(Constants.P81_STATUS_DELETE);
					role.add(toChange);
					changeRoles.add(toChange);
					roleCheckList.add(toChange);
				} else if(item[0].equals(Constants.CSV_FLAG_ADD)) {
					toChange.setFlag(ProcessFlag.ADD);
					toChange.setStatus(Constants.P81_STATUS_ADD);
					role.add(toChange);
					changeRoles.add(toChange);
					roleCheckList.add(toChange);
				} else if(item[0].equals(Constants.CSV_FLAG_UPDATE)) {
					role.remove(toChange);
					toChange.setNewRoleId(item[3]);
					toChange.setStatus(Constants.P81_STATUS_UPDATE);
					toChange.setFlag(ProcessFlag.UPDATE);
					role.add(toChange);
					changeRoles.add(toChange);
					roleCheckList.add(toChange);
					roleCheckList.add(new Role(item[3], toChange.getOrganization().get()));
				}
			} else {
				errorMessages.addAll(errorMessage);
			}
		}
		logger.debug("Validation has already finished.");
		if (errorMessages.isEmpty()) {
			setImportFlag(true);
			showData(role, columns, functions, roleTable);
			logger.info("Import: Successed to import file.");
			return true;
		} else {
			showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
			clear();
			role.addAll(serverRole);
			showData(role, columns, functions, roleTable);
			logger.error("Import: Faild to import file.");
			return false;
		}

	}

	/*
	 * 【CSVエクスポート】 押下
	 */
	public boolean csvExportWork(File csvFileExport) {
		logger.debug("Export button has been pressed.");
		if (csvFileExport == null || !getWork()) {
			return false;
		}
		logger.debug("Data has been synced with server before exporting roles.");
		List<List<String>> saveRoles = getBackup();
		if (!saveCsv(csvFileExport, saveRoles)) {
			return false;
		}
		showInfo(Utils.getString(Constants.DLG_INFO_EXPORT_SUCC));
		logger.info("Export: Successed to export file.");
		return true;
	}

	/*
	 * 【取得】 押下
	 */
	public boolean getWork() {
		try {
			logger.debug("Get button has been pressed.");
			organizationList.clear();
			List<ClientTenant> organizations = ExecuteAPIService.getOrganization();
			if (organizations != null && !organizations.isEmpty()) {
				organizations.forEach(organization -> {
					organizationList.add(organization.getId());
				});
			}
				if (!serverInfo.getOrganizationName().isEmpty())
					organizationList.add(serverInfo.getOrganizationName());
				else
					organizationList.add("");
			logger.debug("Organizations have been synced with server.");
			loadData();
			logger.info("Get: Successed to get data from server.");
			return true;
		} catch(Exception e) {
			logger.error("Get: Failed to get data from server");
			logger.error(e.getMessage(), e);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
			return false;
		}

	}

	/*
	 * 【適用】 押下
	 */
	public boolean applyWork() {
		try {
			saveBackup(getBackup());
			logger.debug("backup has been saved.");
			for (int i = 0; i < changeRoles.size(); i++) {
				Role item = changeRoles.get(i);
				switch (item.getFlag()) {
				case DELETE:
					ExecuteAPIService.deleteRole(item.getRoleId().get(), item.getOrganization().get());
					logger.debug("Role " + item.getRoleId().get() + " has been deleted.");
					break;
				case UPDATE:
					ExecuteAPIService.modifyRole(item.getRoleId().get(), item.getNewRoleId().get(), item.getOrganization().get());
					logger.debug("Role " + item.getRoleId().get() + " has been modified.");
					break;
				case ADD:
					ExecuteAPIService.createRole(item.getRoleId().get(), item.getOrganization().get());
					logger.debug("Role " + item.getRoleId().get() + " has been created.");
					break;
				default:
					break;
				}
			}
			getWork();
			showInfo(Utils.getString(Constants.DLG_INFO_APPLY_SUCC));
			logger.info("Apply: Successed to apply data to server.");
			return true;
		} catch (Exception e) {
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_APPLY), e);
			logger.error("Apply: Failed to apply data to server.");
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private List<List<String>> getBackup() {
		List<List<String>> saveRoles = new ArrayList<List<String>>();
		List<String> header = new ArrayList<String>();
		header.add(SIGNAL);
		header.add(ID);
		header.add(NEWID);
		header.add(ORGANIZATION);
		saveRoles.add(header);
		for (Role iter : serverRole) {
			List<String> singleRow = new ArrayList<String>();
			singleRow.add("");
			singleRow.add(iter.getRoleId().get());
			singleRow.add("");
			singleRow.add(iter.getOrganization().get());
			saveRoles.add(singleRow);
		}
		return saveRoles;
	}

	private void clear() {
		changeRoles.clear();
		errorMessage.clear();
		errorMessages.clear();
		role.clear();
		logger.debug("View has been cleared.");
	}

	private boolean validateHead(CsvRow csvRow, boolean update) {
		String emptyColumns = "";
		if(!hasNewRoleIdRow) {
			emptyColumns += SIGNAL + ", ";
		}
		if (csvRow.get(ID) == null) {
			emptyColumns += ID + ", ";
		}
		// TODO
		if(update &&  csvRow.get(NEWID) == null) {
				emptyColumns += NEWID + ", ";
		}
		if (!emptyColumns.isEmpty()) {
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_NO_COLUMN,
					emptyColumns.substring(0, emptyColumns.length() - 2)));
			showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
			logger.debug("Csv head error.");
			return false;
		}
		logger.debug("Csv head has been validated.");
		return true;
	}

	private boolean validate(String[] item) {
		errorMessage.clear();
		illegal(item, 0);
		if (!errorMessage.isEmpty()) {
			logger.debug("Row "+ item[4] + ROWNAME.get(0) + " is empty.");
			return false;
		}
		switch (ProcessFlag.get(item[0])) {
		case DELETE:
			logger.debug("Flag of current row is 'DELETE'");
			if(hasNewRoleIdRow)
				empty(true, item, 3);
			if (empty(false, item, 1))
				exist(true, item, 1);
			return errorMessage.isEmpty();

		case ADD:
			logger.debug("Flag of current row is 'ADD'");
			if(hasNewRoleIdRow)
				empty(true, item, 3);
			if (empty(false, item, 1)) {
				if (exist(true, item, 2) && exist(false, item, 1)) {

					oversize(item, 1);
					illegal(item, 1);
				}
			}
			return errorMessage.isEmpty();

		case UPDATE:
			logger.debug("Flag of current row is 'UPDATE'");
			if (empty(false, item, 1, 3)) {
				if (exist(true, item, 2) && exist(true, item, 1)) {
					if (item[3].equalsIgnoreCase(item[1])) {
						errorMessage.add(Utils.getString(Constants.P82_DATA_ERROR_SAME, item[4], ID, NEWID));
						logger.debug("Row "+ item[4] + item[1] + " and " + item[3]  + " are same.");
					} else {
						if (exist(false, item, 3)) {
							exist(true, item, 2);
							oversize(item, 3);
							illegal(item, 3);
						}
					}
				}
			}
			return errorMessage.isEmpty();

		default:
			return false;
		}
	}

	private boolean oversize(String[] item, int... columns) {
		for (int i : columns) {
			if (item[i].length() > MAXCHAR) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE, item[4], ROWNAME.get(i), MAXCHAR));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " oversized.");
				return false;
			}
		}
		return true;
	}


	private boolean empty(boolean empty, String[] item, int... columns) {
		for (int i : columns) {
			if (item[i].isEmpty() && !empty) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_EMPTY, item[4], ROWNAME.get(i)));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " is empty.");
				return false;
			} else if (!item[i].isEmpty() && empty) {
				errorMessage.add(Utils.getString(Constants.P82_DATA_ERROR_NOT_EMPTY, item[4], ROWNAME.get(i)));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " is not empty.");
				return false;
			}
		}
		return true;
	}

	private boolean illegal(String[] item, int... columns) {
		for (int i : columns) {
			if (i == 0) {
				if (ProcessFlag.get(item[0]) == null || (!item[0].equals(Constants.CSV_FLAG_DELETE)
						&& !item[0].equals(Constants.CSV_FLAG_UPDATE) && !item[0].equals(Constants.CSV_FLAG_ADD))) {
					errorMessage.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE, item[4], SIGNAL, Constants.CSV_FLAG_DELETE + ", " + Constants.CSV_FLAG_ADD + ", " + Constants.CSV_FLAG_UPDATE));
					logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[0] + " is illegal.");
					return false;
				}
			} else {
				if (Utils.hasIllegalChar(item[i])) {
					errorMessage.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_CHAR, item[4], ROWNAME.get(i)));
					logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " contains illegal char.");
					return false;
				}
			}
		}
		return true;
	}


	private boolean exist(boolean exist, String[] item, int i) {
		Role tempRole = new Role(item[i], item[2]);
		if (i != 2) {
			if (roleCheckList.contains(tempRole)) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE_ELEMENT, item[4], ROWNAME.get(i)));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " duplicates.");
				return false;
			}
			if (!serverRole.contains(tempRole) && exist) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[4], ROWNAME.get(i)));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " does not exsit.");
				return false;
			} else if (role.contains(tempRole) && !exist) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_EXIST, item[4], ROWNAME.get(i)));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " already exists.");
				return false;
			} else if (role.contains(tempRole) && exist) {
				if (item[0].equals("0")
						&& (item[i].equalsIgnoreCase("ROLE_SUPERUSER") || item[i].equalsIgnoreCase("ROLE_USER")
								|| item[i].equalsIgnoreCase("ROLE_ANONYMOUS") || item[i].equalsIgnoreCase("ROLE_ADMINISTRATOR"))
						&& item[2].isEmpty()) {
					errorMessage.add(Utils.getString(Constants.DATA_ERROR_CANT_UPDATE, item[4], ID));
					logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " can't update.");
					return false;
				} else if ((item[i].equalsIgnoreCase("ROLE_SUPERUSER") || item[i].equalsIgnoreCase("ROLE_USER")
						|| item[i].equalsIgnoreCase("ROLE_ANONYMOUS") || item[i].equalsIgnoreCase("ROLE_ADMINISTRATOR"))
						&& item[2].isEmpty()) {
					errorMessage.add(Utils.getString(Constants.DATA_ERROR_CANT_DELETE, item[4], ID));
					logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " can't delete.");
					return false;
				}
			}
		} else {
			if(!organizationList.contains(item[2]) && !item[2].isEmpty()) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[4], ORGANIZATION));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[i] + " does not exist on server.");
				return false;
			} else if(!organizationList.contains(item[2]) && item[2].isEmpty()) {
				errorMessage.add(Utils.getString(Constants.P82_DATA_CAN_NOT_CHANGE, item[4], ORGANIZATION));
				logger.debug("Row "+ item[4] + ROWNAME.get(i) + " : " + item[1] + " can't change.");
				return false;
			}

		}
		return true;
	}

}
