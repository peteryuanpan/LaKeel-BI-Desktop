
package com.legendapl.lightning.tools.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jaspersoft.jasperserver.dto.authority.ClientRole;
import com.jaspersoft.jasperserver.dto.authority.ClientTenant;
import com.jaspersoft.jasperserver.dto.authority.ClientUser;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermission;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AccessDeniedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.InternalServerErrorException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jfoenix.controls.JFXSpinner;
import com.legendapl.lightning.model.FolderResource;
import com.legendapl.lightning.tools.common.CloneUtils;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.common.IgnoreCaseUtil;
import com.legendapl.lightning.tools.common.Utils;
import com.legendapl.lightning.tools.model.CsvRow;
import com.legendapl.lightning.tools.model.Permission;
import com.legendapl.lightning.tools.model.PermissionEnum;
import com.legendapl.lightning.tools.model.PermissionUser;
import com.legendapl.lightning.tools.model.ProcessFlag;
import com.legendapl.lightning.tools.model.Role;
import com.legendapl.lightning.tools.service.ExecuteAPIService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * 権限画面のコントローラクラス
 *
 * @author LAC_徐
 * @since 2017/09/06
 *
 */

public class P84PermissionAnchorPaneAPI extends P84PermissionAnchorPane {



	/** バックグラウンドスレッドから設定されるAPIの実行結果 */
	public static List<ClientResourceLookup> directoryList = new ArrayList<ClientResourceLookup>();

	private Queue<TreeItem<FolderResource>> treeItems = new LinkedList<TreeItem<FolderResource>>();
	private Map<TreeItem<FolderResource>, String> item2Uri = new HashMap<TreeItem<FolderResource>, String>();

	private final Map<String, List<Permission>> csvPermissionAddOrUpdate = new HashMap<String, List<Permission>>();
	private final Map<String, List<Permission>> csvPermissionDelete = new HashMap<String, List<Permission>>();
	private final List<String> errorMessage = new ArrayList<String>();
	private final List<String> errorMessages = new ArrayList<String>();
	private final List<Role> roleList = new ArrayList<Role>();
	private final List<PermissionUser> userList = new ArrayList<PermissionUser>();
	private final List<Role> roleCheckList = new ArrayList<Role>();
	private final List<PermissionUser> userCheckList = new ArrayList<PermissionUser>();
	private final List<Role> fullRoles = new ArrayList<Role>();
	private final List<PermissionUser> fullUsers = new ArrayList<PermissionUser>();

	private final Role ROLE_SUPERUSER = new Role("ROLE_SUPERUSER", "");

	private final List<PermissionUser> users = new ArrayList<PermissionUser>();
	private final List<Role> roles = new ArrayList<Role>();

	private final List<String> resourceList = new ArrayList<String>();
	private final HashMap<String, List<Permission>> entireDataNew = new HashMap<String, List<Permission>>();
	private HashMap<String, List<Permission>> entireDataNewShow = new HashMap<String, List<Permission>>();
	private final List<Role> firstFloorDelete = new ArrayList<Role>();
	private final List<Role> otherFloorDelete = new ArrayList<Role>();
	private final List<PermissionUser> firstFloorDeleteU = new ArrayList<PermissionUser>();
	private final List<PermissionUser> otherFloorDeleteU = new ArrayList<PermissionUser>();
	private Queue<String> uriQueue = new LinkedList<String>();
	public Set<String> specialUris = new HashSet<String>();
	public Set<String> alreadyAccess = new HashSet<String>();
	private String currentPath = "";
	private Map<String, List<Role>> org2Role = new HashMap<String, List<Role>>();
	private Map<String, List<PermissionUser>> org2User = new HashMap<String, List<PermissionUser>>();

	private List<String> organizationList = new IgnoreCaseUtil();
	private Map<String, String> organizationUriMap = new HashMap<String, String>();
	private final static String rex = "(?<=/organizations/)(.+?)(?=/|$)";
	private P84PermissionAnchorPane mainPage;
	private boolean RoleView = true;
	private boolean showInherit = true;

	public P84PermissionAnchorPaneAPI(P84PermissionAnchorPane p84PermissionAnchorPane) {
		mainPage = p84PermissionAnchorPane;
		rolePermissions = mainPage.rolePermissions;
		roleColumns = mainPage.roleColumns;
		permissionTable = mainPage.permissionTable;
		userPermissions = mainPage.userPermissions;
		userColumns = mainPage.userColumns;
		permissionTable1 = mainPage.permissionTable1;
		rolePermissionsNotInherit = mainPage.rolePermissionsNotInherit;
		userPermissionsNotInherit = mainPage.userPermissionsNotInherit;
		refresh = mainPage.refresh;
		folderTree = mainPage.folderTree;
		spinnerPane = mainPage.spinnerPane;
		dirPath = mainPage.dirPath;
		toggle = mainPage.toggle;
		toggleInherit = mainPage.toggleInherit;
	}

	@Override
	public void initReport() {
		new Thread(() -> {
			Platform.runLater(() -> {
				spinnerPane.setVisible(true);
			});
			loadTree();
		}).start();
		new Thread(() -> {
			if (loadData(false) && showData(currentPath)) {
				logger.info("Get: Successed to get data from server.");
				mainPage.setImportFlag(false);
				mainPage.setGetFlag(true);
			} else {
				mainPage.setGetFlag(false);
				logger.error("Get: Failed to get data from server");
			}
			Platform.runLater(() -> {
				spinnerPane.setVisible(false);
			});
			resourceList.removeAll(specialUris);
		}).start();
		logger.debug("initialization successed and data has been loaded.");
	}

	/*
	 * フォルダの最新の情報に更新
	 */
	public void refresh() {
		if (updateServer(false)) {
			backW.run(() -> {
				loadTree();
				showData("/");
				resourceList.removeAll(specialUris);
			});
		}
	}

	/*
	 * 【CSVインポート】 押下
	 */
	@Override
	public boolean csvImportWork(List<CsvRow> csvRows) {

		if (csvRows == null || !loadData(true) || !validateHead(csvRows.get(0))) {
			return false;
		}
		roleCheckList.clear();
		userCheckList.clear();

		List<String[]> permissionsT = new ArrayList<String[]>();
		for (CsvRow readPermission : csvRows) {
			String[] singleRow = new String[6];
			singleRow[0] = readPermission.get(uriColumn);
			if (readPermission.get(roleColumn) == null)
				singleRow[1] = "";
			else
				singleRow[1] = readPermission.get(roleColumn);
			if (readPermission.get(userColumn) == null)
				singleRow[2] = "";
			else
				singleRow[2] = readPermission.get(userColumn);
			singleRow[3] = readPermission.get(permissionColumn);
			if (readPermission.get(organizationColumn) == null)
				singleRow[4] = "";
			else
				singleRow[4] = readPermission.get(organizationColumn);
			singleRow[5] = "" + readPermission.getRowNo();
			permissionsT.add(singleRow);
		}
		if (permissionsT.isEmpty()) {
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_FLAGS_ALL_EMPTY));
			showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
			return false;
		}
		for (int i = 0; i < permissionsT.size(); i++) {
			String[] item = permissionsT.get(i);
			if (validate(item)) {
				String uri = item[0];
				Permission csvPermissionItem = new Permission(item[1], item[2],
						maskEnum2String.get(PermissionEnum.get(item[3])), item[4]);

				if (csvPermissionItem.isRole())
					roleCheckList.add(new Role(item[1], item[4], item[0]));
				else
					userCheckList.add(new PermissionUser(item[2], item[4], item[0]));

				List<Permission> uri2Permission = entireDataNewShow.get(uri);
				if (uri2Permission != null && item[1].equalsIgnoreCase("ROLE_SUPERUSER")) {
					csvPermissionItem.setAllCheck(true);
					if (!uri2Permission.contains(csvPermissionItem)) {
						errorMessages.add(Utils.getString(Constants.DATA_ERROR_CANT_UPDATE, item[5], "ROLE_SUPERUSER"));
						logger.debug("Row " + item[5] + roleColumn + " : " + item[1] + " can not update.");
						continue;
					}
					csvPermissionItem.setAllCheck(false);
				}
				if (item[3].isEmpty()) {
					uri2Permission = csvPermissionDelete.get(uri);
					if (uri2Permission != null) {
						uri2Permission.add(csvPermissionItem);
					} else {
						uri2Permission = new ArrayList<Permission>();
						uri2Permission.add(csvPermissionItem);
						csvPermissionDelete.put(uri, uri2Permission);
					}
					uri2Permission = entireDataNewShow.get(uri);
					uri2Permission.remove(csvPermissionItem);
					csvPermissionItem.setFlag(ProcessFlag.DELETE);
					csvPermissionItem.setStatus(Constants.P81_STATUS_DELETE);
					uri2Permission.add(csvPermissionItem);
				} else {

					uri2Permission = entireDataNewShow.get(uri);
					if (uri2Permission != null) {
						if (uri2Permission.contains(csvPermissionItem)) {
							csvPermissionItem.setAllCheck(true);
							if (uri2Permission.contains(csvPermissionItem)) {
								continue;
							}
							csvPermissionItem.setAllCheck(false);
							uri2Permission.remove(csvPermissionItem);
							csvPermissionItem.setFlag(ProcessFlag.UPDATE);
							csvPermissionItem.setStatus(Constants.P81_STATUS_UPDATE);
							uri2Permission.add(csvPermissionItem);

						} else {

							csvPermissionItem.setFlag(ProcessFlag.ADD);
							csvPermissionItem.setStatus(Constants.P81_STATUS_ADD);
							uri2Permission.add(csvPermissionItem);
						}
						uri2Permission = csvPermissionAddOrUpdate.get(uri);
						if (uri2Permission != null) {
							uri2Permission.add(csvPermissionItem);
						} else {
							uri2Permission = new ArrayList<Permission>();
							uri2Permission.add(csvPermissionItem);
							csvPermissionAddOrUpdate.put(uri, uri2Permission);
						}

					} else {
						uri2Permission = new ArrayList<Permission>();
						csvPermissionItem.setFlag(ProcessFlag.ADD);
						csvPermissionItem.setStatus(Constants.P81_STATUS_ADD);
						uri2Permission.add(csvPermissionItem);
						entireDataNewShow.put(uri, uri2Permission);
						uri2Permission = csvPermissionAddOrUpdate.get(uri);
						if (uri2Permission != null) {
							uri2Permission.add(csvPermissionItem);
						} else {
							uri2Permission = new ArrayList<Permission>();
							uri2Permission.add(csvPermissionItem);
							csvPermissionAddOrUpdate.put(uri, uri2Permission);
						}
					}

				}
			} else {
				errorMessages.addAll(errorMessage);
			}
		}
		if (errorMessages.isEmpty()) {
			if (!csvPermissionDelete.isEmpty() || !csvPermissionAddOrUpdate.isEmpty()) {
				mainPage.setImportFlag(true);
				logger.info("Import: Successed to import file.");
			}
			// Collections.sort(role);
			showData(dirPath.getText());
			return true;
		} else {
			showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
			clear();
			entireDataNewShow = CloneUtils.clone(entireDataNew);
			showData(dirPath.getText());
			logger.error("Import: Failed to import file.");
			return false;
		}

	}

	private boolean validateHead(CsvRow csvRow) {
		String emptyColumns = "";
		if (csvRow.get(uriColumn) == null) {
			emptyColumns += uriColumn + ", ";
		}
		if (csvRow.get(roleColumn) == null && csvRow.get(userColumn) == null) {
			emptyColumns += roleColumn + "/" + userColumn + ",";
		}
		if (csvRow.get(permissionColumn) == null) {
			emptyColumns += permissionColumn + ", ";
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
		illegal(item);
		roleAndUser(item);
		if (!errorMessage.isEmpty())
			return false;

		if (item[1].isEmpty() && !userCheck(item)) {
			return false;
		} else if (!item[1].isEmpty() && !roleCheck(item)) {
			return false;
		}
		List<Permission> permissions = entireDataNew.get(item[0]);
		if (item[3].isEmpty()) {
			Permission permission = new Permission(item[1], item[2], item[3], item[4]);
			if (permissions == null || !permissions.contains(permission)) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[5], permissionColumn));
				logger.debug("Row " + item[5] + permissionColumn + " : " + item[3] + " does not exsit.");
			}
		}
		return errorMessage.isEmpty();

	}

	private boolean roleCheck(String[] item) {
		if (!organizationList.contains(item[4])) {
			errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[5], organizationColumn));
			logger.debug("Row " + item[5] + organizationColumn + " : " + item[4] + " does not exsit.");
			return false;
		} else {
			Role role = new Role(item[1], item[4], item[0]);
			if (!fullRoles.contains(role)) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[5], roleColumn));
				logger.debug("Row " + item[5] + roleColumn + " : " + item[1] + " does not exsit.");
				return false;
			} else if (!roleList.contains(role) && !item[0].startsWith("/public")
					&& !item[0].startsWith("/organizations")) {
				errorMessage.add(Utils.getString(Constants.P84_ORGANIZATION_CAN_NOT_ACCESS, item[5], uriColumn,
						organizationColumn));
				logger.debug("Row " + item[5] + organizationColumn + " : " + item[4] + " can not access.");
				return false;
			} else if (!roleList.contains(role) && item[0].startsWith("/organizations")) {
				Pattern pattern = Pattern.compile(rex);
				Matcher matcher = pattern.matcher(item[0]);
				String organizationUri = "";
				IgnoreCaseUtil orgs = new IgnoreCaseUtil();
				while (matcher.find()) {
					String org = matcher.group();
					organizationUri += "/organizations/" + org;
					if (ExecuteAPIService.getSourceUnit(organizationUri + "/organizations") != null) {
						orgs.add(org);
					}
				}
				if (!orgs.contains(role.getOrganization().get())) {
					errorMessage.add(Utils.getString(Constants.P84_ORGANIZATION_CAN_NOT_ACCESS, item[5], uriColumn,
							organizationColumn));
					logger.debug("Row " + item[5] + organizationColumn + " : " + item[4] + " can not access.");
					return false;
				}
			}
			if (roleCheckList.contains(role)) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE_ELEMENT, item[5], roleColumn));
				logger.debug("Row " + item[5] + roleColumn + " : " + item[1] + " already exsit on csv.");
				return false;
			}
		}
		return true;
	}

	private boolean userCheck(String[] item) {
		if (!organizationList.contains(item[4])) {
			errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[5], organizationColumn));
			logger.debug("Row " + item[5] + organizationColumn + " : " + item[4] + " does not exsit.");
			return false;
		} else {
			PermissionUser user = new PermissionUser(item[2], item[4], item[0]);
			if (!fullUsers.contains(user)) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[5], userColumn));
				logger.debug("Row " + item[5] + userColumn + " : " + item[2] + " does not exsit.");
				return false;
			} else if (!userList.contains(user) && !item[0].startsWith("/public")
					&& !item[0].startsWith("/organizations")) {
				errorMessage.add(Utils.getString(Constants.P84_ORGANIZATION_CAN_NOT_ACCESS, item[5], uriColumn,
						organizationColumn));
				logger.debug("Row " + item[5] + organizationColumn + " : " + item[4] + " can not access.");
				return false;
			} else if (!userList.contains(user) && item[0].startsWith("/organizations")) {
				Pattern pattern = Pattern.compile(rex);
				Matcher matcher = pattern.matcher(item[0]);
				String organizationUri = "";
				IgnoreCaseUtil orgs = new IgnoreCaseUtil();
				while (matcher.find()) {
					String org = matcher.group();
					organizationUri += "/organizations/" + org;
					if (ExecuteAPIService.getSourceUnit(organizationUri + "/organizations") != null) {
						orgs.add(org);
					}
				}
				if (!orgs.contains(user.getOrganization())) {
					errorMessage.add(Utils.getString(Constants.P84_ORGANIZATION_CAN_NOT_ACCESS, item[5], uriColumn,
							organizationColumn));
					logger.debug("Row " + item[5] + organizationColumn + " : " + item[4] + " can not access.");
					return false;
				}
			}
			if (userCheckList.contains(user)) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE_ELEMENT, item[5], userColumn));
				logger.debug("Row " + item[5] + userColumn + " : " + item[2] + " already exsit on csv.");
				return false;
			}
			String organizationName = serverInfo.getOrganizationName().isEmpty() ? ""
					: serverInfo.getOrganizationName();
			if (item[2].equalsIgnoreCase(serverInfo.getUserName()) && item[4].equalsIgnoreCase(organizationName)) {
				errorMessage.add(Utils.getString(Constants.DATA_ERROR_CANT_UPDATE, item[5], userColumn));
				logger.debug("Row " + item[5] + userColumn + " : " + item[2] + " can not update.");
				return false;
			}
		}
		return true;
	}

	private boolean illegal(String[] item) {
		if (item[0].isEmpty()) {
			errorMessage.add(Utils.getString(Constants.DATA_ERROR_EMPTY, item[5], uriColumn));
			return false;
		}
		if (specialUris.contains(item[0])) {
			errorMessage.add(Utils.getString(Constants.P84_CAN_NOT_ACCESS, item[5], "\"" + item[0] + "\""));
			return false;
		} else if (!resourceList.contains(item[0])) {
			errorMessage.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, item[5], uriColumn));
			return false;
		}
		if (item[3].isEmpty()) {
			return true;
		}

		if (item[3].equals(Constants.P84_NOT_ACCESS) || item[3].equals(Constants.P84_ADMISITER)
				|| item[3].equals(Constants.P84_READ_ONLY) || item[3].equals(Constants.P84_READ_WRITE)
				|| item[3].equals(Constants.P84_READ_DELETE) || item[3].equals(Constants.P84_READ_WRITE_DELETE)
				|| item[3].equals(Constants.P84_EXECUTE_ONLY)) {
			return true;
		}

		errorMessage.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE, item[5], permissionColumn,
				Constants.P84_NOT_ACCESS + ", " + Constants.P84_ADMISITER + ", " + Constants.P84_READ_ONLY + ", "
						+ Constants.P84_READ_WRITE + ", " + Constants.P84_READ_DELETE + ", "
						+ Constants.P84_READ_WRITE_DELETE + ", " + Constants.P84_EXECUTE_ONLY + ", " + "空白"));
		logger.debug("Row " + item[5] + permissionColumn + " : " + item[3] + " is illegal.");
		return false;
	}

	private boolean roleAndUser(String[] item) {
		if (!item[1].isEmpty() && !item[2].isEmpty()) {
			errorMessage.add(Utils.getString(Constants.P84_BOTH_NOT_EMPTY, item[5], roleColumn, userColumn));
			logger.debug("Row " + item[5] + roleColumn + " and " + userColumn + " can't be empty at same time.");
			return false;
		} else if (item[1].isEmpty() && item[2].isEmpty()) {
			errorMessage.add(Utils.getString(Constants.P84_BOTH_EMPTY, item[5], roleColumn, userColumn));
			logger.debug("Row " + item[5] + roleColumn + " and " + userColumn + " can't exist at same time.");
			return false;
		}
		return true;
	}

	/*
	 * 【CSVエクスポート】 押下
	 */
	@Override
	public boolean csvExportWork(File csvFileExport) {

		if (csvFileExport == null || !loadData(true) || !showData(dirPath.getText())) {
			return false;
		}
		List<List<String>> exportData = getBackup();
		if (!saveCsv(csvFileExport, exportData)) {
			return false;
		}
		logger.info("Export: Successed to export file.");
		showInfo(Utils.getString(Constants.DLG_INFO_EXPORT_SUCC));
		return true;
	}

	/*
	 * 【取得】 押下
	 */
	@Override
	public boolean getWork() {
		if (loadData(true) && showData(currentPath)) {
			logger.info("Get: Successed to get data from server.");
			mainPage.setImportFlag(false);
			mainPage.setGetFlag(true);
			return true;
		}
		mainPage.setGetFlag(false);
		logger.error("Get: Failed to get data from server");
		return false;
	}

	/*
	 * サーバから最新のデータを取得
	 */
	private boolean updateServer(boolean flag) {
		fullUsers.clear();
		fullRoles.clear();
		roleList.clear();
		userList.clear();
		org2Role.clear();
		org2User.clear();
		organizationList.clear();
		organizationUriMap.clear();
		alreadyAccess.clear();
		specialUris.clear();
		try {
			List<ClientRole> clientRoles = ExecuteAPIService.getRoleUnit();
			if (clientRoles != null) {
				clientRoles.forEach(clientRole -> {
					String orId = clientRole.getTenantId();
					if (orId == null || orId.equalsIgnoreCase(serverInfo.getOrganizationName())) {
						if (orId == null)
							roleList.add(new Role(clientRole.getName(), ""));
						else
							roleList.add(new Role(clientRole.getName(), orId));
					} else {
						List<Role> orgRole = org2Role.get(orId.toLowerCase());
						if (orgRole == null) {
							orgRole = new ArrayList<Role>();
							orgRole.add(new Role(clientRole.getName(), orId));
							org2Role.put(orId.toLowerCase(), orgRole);
						} else {
							orgRole.add(new Role(clientRole.getName(), orId));
						}
						fullRoles.add(new Role(clientRole.getName(), clientRole.getTenantId()));
					}

				});
			}
			List<ClientUser> clientUsers = ExecuteAPIService.getUserList();
			if (clientUsers != null) {
				clientUsers.forEach(clientUser -> {
					String orId = clientUser.getTenantId();
					if (orId == null || orId.equalsIgnoreCase(serverInfo.getOrganizationName())) {
						if (orId == null)
							userList.add(new PermissionUser(clientUser.getUsername(), ""));
						else
							userList.add(new PermissionUser(clientUser.getUsername(), orId));
					} else {
						List<PermissionUser> orgUser = org2User.get(orId.toLowerCase());
						if (orgUser == null) {
							orgUser = new ArrayList<PermissionUser>();
							orgUser.add(new PermissionUser(clientUser.getUsername(), orId));
							org2User.put(orId.toLowerCase(), orgUser);
						} else {
							orgUser.add(new PermissionUser(clientUser.getUsername(), orId));
						}
						fullUsers.add(new PermissionUser(clientUser.getUsername(), orId));
					}
				});
			}

			List<ClientTenant> organizations = ExecuteAPIService.getOrganization();

			if (organizations != null && !organizations.isEmpty()) {
				organizations.forEach(organization -> {
					organizationList.add(organization.getId());
					organizationUriMap.put(organization.getId(), organization.getTenantFolderUri());
				});
			}
			if (!serverInfo.getOrganizationName().isEmpty()) {
				organizationList.add(serverInfo.getOrganizationName());
				organizationList.add("");
			} else
				organizationList.add("");
			fullRoles.addAll(roleList);
			fullUsers.addAll(userList);
		} catch (Exception e) {
			if(!flag) {
				mainPage.setGetFlag(false);
				showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
				clear();
				logger.error(e.getMessage(), e);
			}
			return false;
		}
		return true;
	}

	/*
	 * 【適用】 押下
	 */
	@Override
	public boolean applyWork() {
		try {
			saveBackup(getBackup());
			List<RepositoryPermission> permissionList = new ArrayList<RepositoryPermission>();
			Set<String> uris = csvPermissionAddOrUpdate.keySet();
			if (!uris.isEmpty()) {
				uris.forEach(uri -> {
					List<Permission> permissions = csvPermissionAddOrUpdate.get(uri);
					List<Permission> existPermissions = entireDataNew.get(uri);
					for (Permission permission : permissions) {
						if (existPermissions != null && existPermissions.contains(permission)) {
							ExecuteAPIService.deletePermission(uri, permission);
						}
						String permissionName = permission.getPermissionString();
						if (permission.isRole()) {

							permissionList.add(new RepositoryPermission(uri, "role:/" + permission.getKey(),
									maskString2Int.get(permissionName)));
						} else {
							permissionList.add(new RepositoryPermission(uri, "user:/" + permission.getKey(),
									maskString2Int.get(permissionName)));
						}
					}
				});
			}

			uris = csvPermissionDelete.keySet();
			if (!uris.isEmpty()) {
				uris.forEach(uri -> {
					List<Permission> permissions = csvPermissionDelete.get(uri);
					permissions.forEach(permission -> {
						permission.setInherited(true);
						ExecuteAPIService.deletePermission(uri, permission);
					});
				});
			}

			ExecuteAPIService.createPermission(permissionList);
			if (loadData(true) && showData(currentPath)) {
				showInfo(Utils.getString(Constants.DLG_INFO_APPLY_SUCC));
				logger.info("Apply: Successed to apply data to server.");
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_APPLY), e);
			showData(currentPath);
			logger.error("Apply: Failed to apply data to server.");
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	/*
	 * ビューを変更
	 */
	@Override
	public void changeView() {
		RoleView = mainPage.RoleView;
		showData();
	}

	@Override
	public void changeInherit() {
		showInherit = mainPage.showInherit;
		showData();
	}

	private void clear() {
		errorMessage.clear();
		errorMessages.clear();
		rolePermissions = FXCollections.observableArrayList();
		userPermissions = FXCollections.observableArrayList();
		rolePermissionsNotInherit = FXCollections.observableArrayList();
		userPermissionsNotInherit = FXCollections.observableArrayList();
		csvPermissionAddOrUpdate.clear();
		csvPermissionDelete.clear();
		entireDataNewShow.clear();
	}

	private void showData() {
		if (showInherit) {
			if (RoleView) {
				// Collections.sort(rolePermissions);
				permissionTable.setVisible(true);
				permissionTable1.setVisible(false);
				showData(rolePermissions, roleColumns, roleFunctions, permissionTable);
			} else {
				// Collections.sort(userPermissions);
				permissionTable.setVisible(false);
				permissionTable1.setVisible(true);
				showData(userPermissions, userColumns, userFunctions, permissionTable1);
			}
		} else {
			if (RoleView) {
				// Collections.sort(rolePermissions);
				permissionTable.setVisible(true);
				permissionTable1.setVisible(false);
				showData(rolePermissionsNotInherit, roleColumns, roleFunctions, permissionTable);
			} else {
				// Collections.sort(userPermissions);
				permissionTable.setVisible(false);
				permissionTable1.setVisible(true);
				showData(userPermissionsNotInherit, userColumns, userFunctions, permissionTable1);
			}
		}

	}

	private void loadTree() {

		TreeItem<FolderResource> root = new TreeItem<FolderResource>(new FolderResource("root"),
				new MaterialDesignIconView(folderIcon));
		root.setExpanded(true);
		dirPath.clear();
		dirPath.setText("/");
		currentPath = "/";
		if (serverInfo.getOrganizationName().isEmpty()) {
			try {
				ExecuteAPIService.getTargetSourceUnit("/");
				// 組織名がない場合はルートフォルダを展開
				item2Uri.put(root, "/");
				loadResource(root);
				root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
			} catch (AccessDeniedException e) {

			}
		} else {
			// rootフォルダを展開アイコンに変更
			root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
			// ▶トグルにより展開／折り畳みの際のイベントを追加
			root.expandedProperty().addListener((e) -> {
				if (root.isExpanded()) {
					root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
					logger.debug(root.getValue().getLabel() + "is expanded");
				} else {
					MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
					graphic.setCursor(Cursor.HAND);
					root.setGraphic(graphic);
					logger.debug(root.getValue().getLabel() + "is collapsed");
				}
			});
			FolderResource publicFoloder = new FolderResource("Public");
			publicFoloder.setUri("/public");
			MaterialDesignIconView public_graphic = new MaterialDesignIconView(folderIcon);
			public_graphic.setCursor(Cursor.HAND);
			TreeItem<FolderResource> publicNode = new TreeItem<FolderResource>(publicFoloder, public_graphic);
			String rootName = "";
			if (!serverInfo.getOrganizationName().equalsIgnoreCase("organization_1"))
				rootName = serverInfo.getOrganizationName();
			else
				rootName = "Organization";
			FolderResource organizationFoloder = new FolderResource(rootName);
			organizationFoloder.setUri("/");
			MaterialDesignIconView org_graphic = new MaterialDesignIconView(folderIcon);
			org_graphic.setCursor(Cursor.HAND);
			TreeItem<FolderResource> organizationNode = new TreeItem<FolderResource>(organizationFoloder, org_graphic);
			boolean organizationLoad = true;
			boolean publicLoad = true;
			try {
				ExecuteAPIService.getTargetSourceUnit("/");

				root.getChildren().add(organizationNode);
				item2Uri.put(organizationNode, "/");
				organizationNode.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
			} catch (AccessDeniedException e) {
				organizationLoad = false;
			}
			// 組織名がある場合は組織名のrootフォルダとPublicフォルダを追加
			try {
				ExecuteAPIService.getTargetSourceUnit("/public");
				root.getChildren().add(publicNode);
				item2Uri.put(publicNode, "/public");
				publicNode.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
			} catch (AccessDeniedException e) {
				publicLoad = false;
			}

			if(organizationLoad && publicLoad) {
				loadResource(publicNode, organizationNode);
			}
			else if(organizationLoad) {
				loadResource(organizationNode);
			} else if(publicLoad) {
				loadResource(publicNode);
			}
			folderTree.setShowRoot(false);
		}

		dirPath.clear();
		dirPath.setText("/");
		currentPath = "/";

		Platform.runLater(() -> {
			// フォルダ表示用TreeViewに取得したツリーを設定
			folderTree.setRoot(root);

			// フォルダ選択時のイベントハンドラを追加
			folderTree.setOnMouseClicked((e) -> handleMouseClicked(e));

			// ディレクトリパスを編集不可能に設定
			dirPath.setEditable(true);
		});
	}

	private boolean addTreeItems(TreeItem<FolderResource> root, String path, boolean start) {

		if (root.getGraphic() == null) {
			return false;
		}
		// アイテムをクリアする
		root.getChildren().clear();

		// ロード中のアイコンを設定
		JFXSpinner loadingFolderIcon = new JFXSpinner();
		loadingFolderIcon.setRadius(5);
		Platform.runLater(() -> {
			root.setGraphic(loadingFolderIcon);
		});

		try {

			ClientResourceListWrapper sourceListWrapper = ExecuteAPIService.getTargetSourceUnit(path);
			directoryList.clear();
			if (null != sourceListWrapper && null != sourceListWrapper.getResourceLookups()) {
				directoryList.addAll(sourceListWrapper.getResourceLookups());
			}
			if (!directoryList.isEmpty()) {
				// 既に中身が入っていたり、他のスレッドにより追加結果が重複する場合の対策
				if (!root.getChildren().isEmpty()) {
					return false;
				}
				List<TreeItem<FolderResource>> notFolderFile = new ArrayList<TreeItem<FolderResource>>();
				// 実行結果を1件ずつroot直下に追加
				for (ClientResourceLookup resource : directoryList) {
					if (resource.getResourceType().equalsIgnoreCase(Constants.P84_FOLDER)) {
						MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
						// graphic.setCursor(Cursor.HAND);
						TreeItem<FolderResource> child = new TreeItem<FolderResource>(new FolderResource(resource),
								graphic);
						root.getChildren().add(child);
						item2Uri.put(child, resource.getUri());
					} else {
						TreeItem<FolderResource> child = new TreeItem<FolderResource>(new FolderResource(resource));
						notFolderFile.add(child);
						item2Uri.put(child, resource.getUri());
					}
				}
				if (!notFolderFile.isEmpty()) {
					notFolderFile.forEach(node -> {
						root.getChildren().add(node);
					});
				}
				// 展開後にフォルダアイコンを変更
				if (start) {
					Platform.runLater(() -> {
						MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
						// graphic.setCursor(Cursor.HAND);
						root.setGraphic(graphic);
					});
				} else {
					Platform.runLater(() -> {
						root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
					});
				}

				// ▶トグルにより展開／折り畳みの際のイベントを追加
				root.expandedProperty().addListener((e) -> {
					if (root.isExpanded()) {
						root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
						logger.debug(root.getValue().getLabel() + "is expanded");
					} else {
						MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
						// graphic.setCursor(Cursor.HAND);
						root.setGraphic(graphic);
						logger.debug(root.getValue().getLabel() + "is collapsed");
					}
				});
				return true;
			}

			else {
				// フォルダの中にフォルダがない場合は、展開されたアイコンを設定
				if (start) {
					Platform.runLater(() -> {
						MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
						// graphic.setCursor(Cursor.HAND);
						root.setGraphic(graphic);
					});
				} else {
					Platform.runLater(() -> {
						root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
						folderTree.refresh();
					});
				}
				return false;
			}

			// API実行時のタイムアウトエラー
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	private void handleMouseClicked(MouseEvent event) {
		Node node = event.getPickResult().getIntersectedNode();
		// 選択しているアイテムがない場合は処理を中断
		if (null == node || null == folderTree.getSelectionModel()
				|| null == folderTree.getSelectionModel().getSelectedItem()) {
			return;
		}
		FolderResource resource = folderTree.getSelectionModel().getSelectedItem().getValue();
		logger.debug("Resource is " + resource.superToString());
		logger.debug("Node is " + node);
		logger.debug("Uri is " + resource.getUri());
		String uri = resource.getUri();
		if (StringUtils.isEmpty(uri)) {
			if (resource.getLabel().equals("root")) {
				if (currentPath.equals("/")) {
					return;
				}
				backW.run(() -> {
					if (!showData("/")) {
						return;
					}
					dirPath.clear();
					dirPath.setText("/");
					currentPath = "/";
					logger.debug("Current path : /");
				});
			}
			return;
		}

		if (!uri.equals(currentPath)) {
			backW.run(() -> {
				if (!showData(uri)) {
					return;
				}
				dirPath.clear();
				dirPath.setText(uri);
				currentPath = uri;
				logger.debug("Current path : " + uri);
			});
		}
		// クリックされた部分がフォルダアイコン、またはセルがダブルクリックされた場合、フォルダを展開
		if (!(node instanceof MaterialDesignIconView)
				&& !(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)) {
			return;
		}

		TreeItem<FolderResource> item = folderTree.getSelectionModel().getSelectedItem();
		// 選択したフォルダをすでに開いていて、アイコン以外をダブルクリック場合はアイテムを追加せず、展開されたフォルダを折りたたむ
		if (0 != item.getChildren().size() && !(node instanceof MaterialDesignIconView)) {
			return;
		}

		item.setExpanded(true);

	}

	private void loadResource(TreeItem<FolderResource> root) {
		try {
			logger.debug("Loading folder tree.");
			refresh.setDisable(true);
			resourceList.clear();
			treeItems.add(root);
			while (!treeItems.isEmpty()) {
				TreeItem<FolderResource> head = treeItems.poll();
				String uri = item2Uri.get(head);
				resourceList.add(uri);
				addUri(uri);
				addTreeItems(head, uri, true);
				ObservableList<TreeItem<FolderResource>> childs = head.getChildren();
				if (childs != null && !childs.isEmpty()) {
					childs.forEach(child -> treeItems.add(child));
				}
			}
			addUri("end");
			refresh.setDisable(false);
			logger.info("Folder tree has been synced.");
		} catch (Exception e) {
			addUri("end");
			logger.error("Failed to sync folder tree.");
			logger.error(e.getMessage(), e);
		}
	}

	private void loadResource(TreeItem<FolderResource> publicNode, TreeItem<FolderResource> organizationNode) {
		try {
			logger.debug("Loading folder tree.");
			refresh.setDisable(true);
			resourceList.clear();
			String uri = item2Uri.get(publicNode);
			resourceList.add(uri);
			addUri(uri);
			addTreeItems(publicNode, uri, true);
			ObservableList<TreeItem<FolderResource>> childs = publicNode.getChildren();
			if (childs != null && !childs.isEmpty()) {
				childs.forEach(child -> treeItems.add(child));
			}
			uri = item2Uri.get(organizationNode);
			resourceList.add(uri);
			addUri(uri);
			addTreeItems(organizationNode, uri, true);
			childs = organizationNode.getChildren();
			if (childs != null && !childs.isEmpty()) {
				childs.forEach(child -> treeItems.add(child));
			}
			while (!treeItems.isEmpty()) {
				TreeItem<FolderResource> head = treeItems.poll();
				uri = item2Uri.get(head);
				resourceList.add(uri);
				addUri(uri);
				addTreeItems(head, uri, true);
				ObservableList<TreeItem<FolderResource>> childsX = head.getChildren();
				if (childsX != null && !childsX.isEmpty()) {
					childsX.forEach(child -> treeItems.add(child));
				}
			}
			addUri("end");
			refresh.setDisable(false);
			logger.info("Folder tree has been synced.");
		} catch (Exception e) {
			logger.error("Failed to sync folder tree.");
			addUri("end");
			logger.error(e.getMessage(), e);
		}
	}


	private boolean showData(String uri) {
		String uriCopy = uri;
		users.clear();
		roles.clear();
		rolePermissions = FXCollections.observableArrayList();
		userPermissions = FXCollections.observableArrayList();
		rolePermissionsNotInherit = FXCollections.observableArrayList();
		userPermissionsNotInherit = FXCollections.observableArrayList();
		if (specialUris.contains(uri)) {
			showData();
			return true;
		}
		try {
			if (uri.startsWith("/public")) {
				users.addAll(fullUsers);
				roles.addAll(fullRoles);
			} else if (uri.startsWith("/organizations")) {
				Pattern pattern = Pattern.compile(rex);
				Matcher matcher = pattern.matcher(uri);
				String organizationUri = "";
				while (matcher.find()) {
					String org = matcher.group();
					organizationUri += "/organizations/" + org;
					if (ExecuteAPIService.getSourceUnit(organizationUri + "/organizations") != null) {
						List<Role> orgRole = org2Role.get(org.toLowerCase());
						if (orgRole != null) {
							roles.addAll(orgRole);
						}
						List<PermissionUser> orgUser = org2User.get(org.toLowerCase());
						if (orgUser != null) {
							users.addAll(orgUser);
						}
					}
				}
				users.addAll(userList);
				roles.addAll(roleList);
			} else {
				users.addAll(userList);
				roles.addAll(roleList);
			}
			boolean start = true;
			String stopUri = "/";
			if (uri.startsWith("/public") && !serverInfo.getOrganizationName().isEmpty())
				stopUri = "/public";
			firstFloorDelete.clear();
			otherFloorDelete.clear();
			firstFloorDeleteU.clear();
			otherFloorDeleteU.clear();
			while (!roles.isEmpty() || !users.isEmpty()) {
				loadPermissions(uri, start);
				start = false;
				if (uri.equals(stopUri)) {
					if (!roles.isEmpty()) {
						for (Role role : roles) {
							if ((serverInfo.getOrganizationName().isEmpty() && !specialUris.contains("/public"))
									|| !role.getOrganization().get().isEmpty()) {
								if (!role.equals(ROLE_SUPERUSER)) {
									Permission defaultPermission = new Permission(role.getRoleId().get(), "",
											maskEnum2String.get(PermissionEnum.NOTACCESS),
											role.getOrganization().get());
									if (firstFloorDelete.contains(role)) {
										defaultPermission.setFlag(ProcessFlag.DELETE);
										defaultPermission.setStatus(Constants.P81_STATUS_DELETE);
									} else if (otherFloorDelete.contains(role)) {
										defaultPermission.setFlag(ProcessFlag.UPDATE);
										defaultPermission.setStatus(Constants.P81_STATUS_UPDATE);
									}
									defaultPermission.setInherited(true);
									rolePermissions.add(defaultPermission);
								} else {
									Permission defaultPermission = new Permission(role.getRoleId().get(), "",
											maskEnum2String.get(PermissionEnum.ADMINISTER),
											role.getOrganization().get());
									if (firstFloorDelete.contains(role)) {
										defaultPermission.setFlag(ProcessFlag.DELETE);
										defaultPermission.setStatus(Constants.P81_STATUS_DELETE);
									} else if (otherFloorDelete.contains(role)) {
										defaultPermission.setFlag(ProcessFlag.UPDATE);
										defaultPermission.setStatus(Constants.P81_STATUS_UPDATE);
									}
									defaultPermission.setInherited(true);
									rolePermissions.add(defaultPermission);
								}
							} else {
								Permission defaultPermission = new Permission(role.getRoleId().get(), "", " ",
										role.getOrganization().get());
								if (firstFloorDelete.contains(role)) {
									defaultPermission.setFlag(ProcessFlag.DELETE);
									defaultPermission.setStatus(Constants.P81_STATUS_DELETE);
								} else if (otherFloorDelete.contains(role)) {
									defaultPermission.setFlag(ProcessFlag.UPDATE);
									defaultPermission.setStatus(Constants.P81_STATUS_UPDATE);
								}
								defaultPermission.setInherited(true);
								rolePermissions.add(defaultPermission);
							}

						}
					}
					if (!users.isEmpty()) {
						for (PermissionUser user : users) {
							Permission defaultPermission = new Permission("", user.getUserName(),
									maskEnum2String.get(PermissionEnum.NOTACCESS), user.getOrganization());
							String organizationName = serverInfo.getOrganizationName().isEmpty() ? ""
									: serverInfo.getOrganizationName();
							if (user.getUserName().equalsIgnoreCase(serverInfo.getUserName())
									&& user.getOrganization().equalsIgnoreCase(organizationName)) {
								defaultPermission.setPermission("");
							} else {
								if (firstFloorDeleteU.contains(user)) {
									defaultPermission.setFlag(ProcessFlag.DELETE);
									defaultPermission.setStatus(Constants.P81_STATUS_DELETE);
								} else if (otherFloorDeleteU.contains(user)) {
									defaultPermission.setFlag(ProcessFlag.UPDATE);
									defaultPermission.setStatus(Constants.P81_STATUS_UPDATE);
								}
							}
							userPermissions.add(defaultPermission);
						}
					}
					roles.clear();
					users.clear();
					if (uriCopy.startsWith("/public")) {
						users.addAll(fullUsers);
						roles.addAll(fullRoles);
					} else {
						users.addAll(userList);
						roles.addAll(roleList);
					}
					break;
				}

				String[] uris = uri.split("/");
				uri = "";
				for (int i = 0; i < uris.length - 1; i++) {
					uri += uris[i] + "/";
				}
				if (!uri.equals("/"))
					uri = uri.substring(0, uri.length() - 1);
			}
			showData();
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private void loadPermissions(String uri, boolean start) {
		List<Permission> permissionList = entireDataNewShow.get(uri);
		if (permissionList != null) {
			for (Permission permission : permissionList) {
				if (permission.isRole()) {
					Role check = new Role(permission.getRoleName().get(), permission.getOrganization().get());
					if (!roles.contains(check))
						continue;
					if (start) {

						if (permission.getFlag() == ProcessFlag.DELETE) {

							firstFloorDelete.add(check);
							continue;
						}
						roles.remove(check);
						permission.setInherited(false);
						rolePermissions.add(permission);
						rolePermissionsNotInherit.add(permission);
					} else {
						if (permission.getFlag() == ProcessFlag.DELETE) {
							otherFloorDelete.add(check);
							continue;
						}
						Permission copy = permission.getCopy();
						copy.setInherited(true);
						if (firstFloorDelete.contains(check)) {
							copy.setFlag(ProcessFlag.DELETE);
							copy.setStatus(Constants.P81_STATUS_DELETE);
							rolePermissionsNotInherit.add(copy);
						} else if (otherFloorDelete.contains(check) || permission.getFlag() != ProcessFlag.NONE) {
							copy.setFlag(ProcessFlag.UPDATE);
							copy.setStatus(Constants.P81_STATUS_UPDATE);
						}
						roles.remove(check);
						rolePermissions.add(copy);
					}
				} else {
					PermissionUser check = new PermissionUser(permission.getUserName().get(),
							permission.getOrganization().get());
					if (!users.contains(check))
						continue;
					if (start) {
						if (permission.getFlag() == ProcessFlag.DELETE) {
							firstFloorDeleteU.add(check);
							userPermissionsNotInherit.add(permission);
							continue;
						}
						users.remove(check);
						permission.setInherited(false);
						userPermissions.add(permission);
						userPermissionsNotInherit.add(permission);
					} else {
						if (permission.getFlag() == ProcessFlag.DELETE) {
							otherFloorDeleteU.add(check);
							continue;
						}

						Permission copy = permission.getCopy();
						if (firstFloorDeleteU.contains(check)) {
							copy.setFlag(ProcessFlag.DELETE);
							copy.setStatus(Constants.P81_STATUS_DELETE);
						} else if (otherFloorDeleteU.contains(check) || permission.getFlag() != ProcessFlag.NONE) {
							copy.setFlag(ProcessFlag.UPDATE);
							copy.setStatus(Constants.P81_STATUS_UPDATE);
						}
						users.remove(check);
						copy.setInherited(true);
						userPermissions.add(copy);
					}
				}
			}
		}
	}

	private boolean loadData(boolean get) {
		clear();
		updateServer(true);
		entireDataNew.clear();
		// uriMap.clear();
		try {
			if (get) {
				for (String uri : resourceList) {
					refresh.setDisable(true);
					loadDataGet(uri);
					refresh.setDisable(false);
				}
			} else {
				String uri = getUri();
				while (!uri.equals("end")) {
					loadDataGet(uri);
					uri = getUri();
					if(uri == null) {
						mainPage.setGetFlag(false);
						showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), new InternalServerErrorException());
						clear();
						return false;
					}
				}
			}
			entireDataNewShow = CloneUtils.clone(entireDataNew);
		} catch (Exception e) {
			mainPage.setGetFlag(false);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
			clear();
			logger.error(e.getMessage(), e);
			return false;
		}
		mainPage.setImportFlag(false);
		mainPage.setGetFlag(true);
		return true;
	}

	private void loadDataGet(String uri) {
		List<RepositoryPermission> permissions = null;
		try {
			permissions = ExecuteAPIService.getPermissionUnit(uri, alreadyAccess, specialUris);
		} catch (ResourceNotFoundException e) {
			return;
		}
		if (permissions != null) {
			String recipient = "";
			String mask = "";
			String[] split;
			for (RepositoryPermission permission : permissions) {
				recipient = permission.getRecipient();
				mask = maskEnum2String.get(PermissionEnum.get(permission.getMask()));
				if(mask == null)
					continue;
				split = recipient.split("/");
				if (split[0].equals("role:")) {
					Permission rolePermission = null;
					if (split.length == 3) {
						rolePermission = new Permission(split[2], "", mask, split[1]);
					} else {
						rolePermission = new Permission(split[1], "", mask);
					}
					List<Permission> uri2Permission = entireDataNew.get(uri);
					if (uri2Permission != null) {
						uri2Permission.add(rolePermission);
					} else {
						uri2Permission = new ArrayList<Permission>();
						uri2Permission.add(rolePermission);
						entireDataNew.put(uri, uri2Permission);
					}

				} else if (split[0].equals("user:")) {
					Permission userPermission = null;
					if (split.length == 3) {
						userPermission = new Permission("", split[2], mask, split[1]);
					} else {
						userPermission = new Permission("", split[1], mask);
					}

					List<Permission> uri2Permission = entireDataNew.get(uri);
					if (uri2Permission != null) {
						uri2Permission.add(userPermission);
					} else {
						uri2Permission = new ArrayList<Permission>();
						uri2Permission.add(userPermission);
						entireDataNew.put(uri, uri2Permission);
					}
				}
			}
		}
	}

	private List<List<String>> getBackup() {
		List<List<String>> exportData = new ArrayList<List<String>>();
		List<String> head = Arrays.asList(uriColumn, roleColumn, userColumn, organizationColumn, permissionColumn);
		exportData.add(head);
		Set<String> uris = entireDataNew.keySet();
		if (!uris.isEmpty()) {
			uris.forEach(uri -> {
				List<Permission> permissions = entireDataNew.get(uri);
				for (Permission permission : permissions) {
					List<String> row = Arrays.asList(uri, permission.getRoleName().get(),
							permission.getUserName().get(), permission.getOrganization().get(),
							"" + maskString2Int.get(permission.getPermissionString()));
					exportData.add(row);
				}
			});
		}
		return exportData;
	}

	private void addUri(String uri) {
		synchronized (uriQueue) {
			uriQueue.add(uri);
			uriQueue.notify();
		}
	}

	private String getUri() {
		synchronized (uriQueue) {
			if (uriQueue.isEmpty()) {
				try {
					uriQueue.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return uriQueue.poll();
	}
}

