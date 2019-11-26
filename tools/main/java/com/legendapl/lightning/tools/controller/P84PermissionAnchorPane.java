
package com.legendapl.lightning.tools.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.JFXTreeView;
import com.legendapl.lightning.model.FolderResource;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.model.CsvRow;
import com.legendapl.lightning.tools.model.Permission;
import com.legendapl.lightning.tools.model.PermissionEnum;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

/**
 * 権限画面のコントローラクラス
 *
 * @author LAC_徐
 * @since 2017/09/06
 *
 */

public class P84PermissionAnchorPane extends P80BaseToolsAnchorPane {

	@FXML
	protected JFXTreeView<FolderResource> folderTree;

	@FXML
	protected JFXTextField dirPath;

	@FXML
	protected JFXToggleButton toggle;
	@FXML
	protected JFXToggleButton toggleInherit;
	@FXML
	protected AnchorPane test;


	/** フォルダアイコン */
	protected MaterialDesignIcon folderIcon = MaterialDesignIcon.FOLDER_OUTLINE;
	protected FontAwesomeIcon folderExpandedIcon = FontAwesomeIcon.FOLDER_OPEN_ALT;

	/** 画面に表示されるリポジトリ表示用TableView */
	@FXML
	protected JFXTreeTableView<Permission> permissionTable;
	@FXML
	protected JFXTreeTableView<Permission> permissionTable1;
	@FXML
	protected JFXTreeTableColumn<Permission, String> roleName;
	@FXML
	protected JFXTreeTableColumn<Permission, String> userName;
	@FXML
	protected JFXTreeTableColumn<Permission, String> permission;
	@FXML
	protected JFXTreeTableColumn<Permission, String> organization;
	@FXML
	protected JFXTreeTableColumn<Permission, String> status;
	@FXML
	protected JFXTreeTableColumn<Permission, String> permission1;
	@FXML
	protected JFXTreeTableColumn<Permission, String> organization1;
	@FXML
	protected JFXTreeTableColumn<Permission, String> status1;
	@FXML
	protected JFXButton refresh;


	protected ObservableList<Permission> rolePermissions = FXCollections.observableArrayList();
	protected ObservableList<Permission> userPermissions = FXCollections.observableArrayList();
	protected ObservableList<Permission> rolePermissionsNotInherit = FXCollections.observableArrayList();
	protected ObservableList<Permission> userPermissionsNotInherit = FXCollections.observableArrayList();

	protected final String uriColumn = Constants.P84_TABLE_COLUMN_URICOLUMN;
	protected final String roleColumn = Constants.P84_TABLE_COLUMN_ROLECOLUMN;
	protected final String userColumn = Constants.P84_TABLE_COLUMN_USERCOLUMN;
	protected final String permissionColumn = Constants.P84_TABLE_COLUMN_PERMISSIONCOLUMN;
	protected final String organizationColumn = Constants.P84_TABLE_COLUMN_ORGANIZATIONCOLUMN;

	protected List<JFXTreeTableColumn<Permission, String>> roleColumns = new ArrayList<JFXTreeTableColumn<Permission, String>>();
	protected List<JFXTreeTableColumn<Permission, String>> userColumns = new ArrayList<JFXTreeTableColumn<Permission, String>>();
	protected static List<String> roleFunctions = new ArrayList<String>();
	protected static List<String> userFunctions = new ArrayList<String>();

	protected boolean RoleView = true;
	protected boolean showInherit = true;



	protected P84PermissionAnchorPane page;

	@SuppressWarnings("serial")
	final protected Map<String, Integer> maskString2Int = Collections.unmodifiableMap(

			new HashMap<String, Integer>() {
				{
					put(Constants.P84_MARK_0, 0);
					put(Constants.P84_MARK_1, 1);
					put(Constants.P84_MARK_2, 2);
					put(Constants.P84_MARK_6, 6);
					put(Constants.P84_MARK_18, 18);
					put(Constants.P84_MARK_30, 30);
					put(Constants.P84_MARK_32, 32);
				}
			});

	@SuppressWarnings("serial")
	final protected Map<PermissionEnum, String> maskEnum2String = Collections
			.unmodifiableMap(new HashMap<PermissionEnum, String>() {
				{
					put(PermissionEnum.NOTACCESS, Constants.P84_MARK_0);
					put(PermissionEnum.ADMINISTER, Constants.P84_MARK_1);
					put(PermissionEnum.READONLY, Constants.P84_MARK_2);
					put(PermissionEnum.READWRITE, Constants.P84_MARK_6);
					put(PermissionEnum.READDELETE, Constants.P84_MARK_18);
					put(PermissionEnum.READWRITEDELETE, Constants.P84_MARK_30);
					put(PermissionEnum.EXECUTEONLY, Constants.P84_MARK_32);
				}
			});

	@Override
	public void init(URL location, ResourceBundle resources) {
		// preferencesを読み込み
		loadPreferences();

		if (roleColumns.isEmpty()) {
			roleColumns = Arrays.asList(roleName, organization, permission, status);
			userColumns = Arrays.asList(userName, organization1, permission1, status1);
			roleFunctions = Arrays.asList("getRoleName", "getOrganization", "getPermission", "getStatus");
			userFunctions = Arrays.asList("getUserName", "getOrganization", "getPermission", "getStatus");
		}
		logger.debug("initialization successed.");
		page = new P84PermissionAnchorPaneReport(this);
		page.initReport();
		logger.debug("initialization successed and data has been loaded.");
	}

	protected void changPage() {
		logger.debug("change the method of loading permission.");
		page = new P84PermissionAnchorPaneAPI(this);
		page.initReport();
		logger.debug("initialization successed and data has been loaded.");
	}

	@Override
	protected List<String> getHelpContent() {
		return Arrays.asList(
				Constants.P84_HELP_CONTENT_PERMISSION,
				new String(""),
				Constants.P84_HELP_CONTENT_PERMISSION_0,
				Constants.P84_HELP_CONTENT_PERMISSION_1,
				Constants.P84_HELP_CONTENT_PERMISSION_2,
				Constants.P84_HELP_CONTENT_PERMISSION_6,
				Constants.P84_HELP_CONTENT_PERMISSION_18,
				Constants.P84_HELP_CONTENT_PERMISSION_30,
				Constants.P84_HELP_CONTENT_PERMISSION_32,
				Constants.P84_HELP_CONTENT_PERMISSION_EMPTY
		);
	}

	/*
	 * フォルダの最新の情報に更新
	 */
	public void refresh() {
		page.refresh();
	}

	/*
	 * 【CSVインポート】 押下
	 */
	public boolean csvImportWork(List<CsvRow> csvRows) {
		return page.csvImportWork(csvRows);
	}


	/*
	 * 【CSVエクスポート】 押下
	 */
	public boolean csvExportWork(File csvFileExport) {

		return page.csvExportWork(csvFileExport);
	}

	/*
	 * 【取得】 押下
	 */
	public boolean getWork() {
		return page.getWork();
	}


	/*
	 * 【適用】 押下
	 */
	public boolean applyWork() {
		return page.applyWork();
	}

	/*
	 * ビューを変更
	 */
	public void changeView() {
		RoleView = !RoleView;
		page.changeView();
	}

	public void changeInherit() {
		showInherit = !showInherit;
		page.changeInherit();
	}

	protected void initReport() {

	}

}
