package com.legendapl.lightning.adhoc.factory;

import java.io.File;
import java.util.Arrays;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.controller.C100AdhocBaseAnchorPane;
import com.legendapl.lightning.adhoc.controller.P121AdhocAnchorPane;
import com.legendapl.lightning.adhoc.model.FolderResource;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.SaveFileChooserService;
import com.legendapl.lightning.adhoc.service.XMLTransferService;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class AdhocSaveFactory extends AdhocBaseFactory {

	public static MenuButton saveButton;
	public static Label adhocNameLabel;
	static MenuItem menuItemSave;
	static MenuItem menuItemSaveWithName;
	static AdhocSaveService saveService;
	
	public static Boolean fromAdhoc;
	public static String fileName;
	public static String filePath;
	
	public static void init() {
		menuItemSave = new MenuItem(AdhocUtils.getString("P121.button.save.MenuItem.save"));
		menuItemSaveWithName = new MenuItem(AdhocUtils.getString("P121.button.save.MenuItem.saveWithName"));
		menuItemSave.getStyleClass().add("save-button-menuItem");
		menuItemSave.setDisable(null == fromAdhoc ? true : !fromAdhoc);
		menuItemSave.setOnAction(event -> handleActionSave());
		menuItemSaveWithName.getStyleClass().add("save-button-menuItem");
		menuItemSaveWithName.setOnAction(event -> handleActionSaveWithName());
		saveButton.getItems().setAll(menuItemSave, menuItemSaveWithName);
		saveService = new AdhocSaveService();
	}
	
	public static void handleActionSave() {
		// check
		if (!checkFilterBeforeSave()) {
			doAfterCheckFilterFailed();
			return;
		}
		// setAdhoc
		setAdhocPurposeForSave();
		// save
		File outputFile = new File(filePath);
		saveService.save(outputFile);
	}

	public static void handleActionSaveWithName() {
		// check
		if (!checkFilterBeforeSave()) {
			doAfterCheckFilterFailed();
			return;
		}
		// setAdhoc
		setAdhocPurposeForSave();
		// save
		saveService.getProperty().setInitialFileName(AdhocSaveFactory.fileName);
		saveService.getProperty().setInitialDirectoryPath(getInitialDirectoryPath(AdhocSaveFactory.filePath));
		saveService.save(C100AdhocBaseAnchorPane.adhocStage);
	}
	
	private static boolean checkFilterBeforeSave() {
		boolean checkFlg = true;
		if (!filterPaneFactory.doFilterItemCheck()) {
			checkFlg = false;
		} else if (!filterPaneFactory.doFilterCheck()) {
			checkFlg = false;
		}
		return checkFlg;
	}
	
	private static void doAfterCheckFilterFailed() {
		AlertWindowService.showInfo(AdhocUtils.getString("INFO_ADHOC_SAVE_CHECK_FILTER_FAILED"));
	}
	
	private static void setAdhocPurposeForSave() {
		LayoutTransferFactory.transferToAdhoc(P121AdhocAnchorPane.viewModelType);
		adhoc.setViewModelType(P121AdhocAnchorPane.viewModelType);
		adhoc.setDataModelType(P121AdhocAnchorPane.dataModelType);
		filterPaneFactory.doSave();
		// TODO: 他のデータセット操作
	}
	
	private static String getInitialDirectoryPath(String filePath) {
		if (null == filePath || "".equals(filePath)) {
			return AdhocConstants.Application.ADHOC_FILE_PATH;
		} else {
			File parent = (new File(filePath)).getParentFile();
			return parent.getPath();
		}
	}
	
	static class AdhocSaveService extends SaveFileChooserService {
		public AdhocSaveService() {
			super();
			this.getProperty().setTitle(AdhocUtils.getString("P121.button.save"));
			this.getProperty().setFilterTypes(Arrays.asList(ExtensionFilterType.XML));
			this.getProperty().setStartWithPath(AdhocConstants.Application.ADHOC_FILE_PATH);
		}
		@Override protected void doCustomerCheck(File outputFile) throws Exception {
			String fileName = SaveFileChooserService.getRealFileName(outputFile.getName(), ExtensionFilterType.XML);
			if (fileName.isEmpty()) { // 名前が空場合
				String detailErrors = AdhocUtils.getString("ERROR_SAVE_FILE_NAME_EMPTY");
				throw new RuntimeException(detailErrors);
			}
			if (fileName.contains(".")) { // 名前が「.」を含む場合
				String detailErrors = AdhocUtils.getString("ERROR_SAVE_FILE_NAME_ILLEGAL");
				throw new RuntimeException(detailErrors);
			}
		}
		@Override protected void doSave(File outputFile) throws Exception {
			XMLTransferService.saveDataToXML(adhoc, outputFile);
		}
		@Override protected void doFailed(File outputFile, Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showErrorNotInBack(AdhocUtils.getString("ERROR_SAVE_TITLE"), AdhocUtils.getString("ERROR_SAVE_EXPORT_FAILED"));
		}
		@Override protected void doSuccess(File outputFile) {
			fromAdhoc = true;
			fileName = FolderResource.transferName(outputFile.getName());
			filePath = outputFile.getAbsolutePath();
			Platform.runLater(() -> {
				// menuItemSave
				menuItemSave.setDisable(null == fromAdhoc ? true : !fromAdhoc);
				// adhocNameLabel
				String newLabel = FolderResource.transferName(outputFile.getName());
				adhocNameLabel.setText(newLabel);
				// clear StatementFactory
				StatementFactory.clear();
			});
			logger.info("Saved Adhoc.");
			AlertWindowService.showInfoNotInBack(AdhocUtils.getString("SUCCESS_SAVE_TITLE"));
		}
	}
	
	public static void setSaveButton(MenuButton saveButton) {
		AdhocSaveFactory.saveButton = saveButton;
	}

	public static void setAdhocNameLabel(Label adhocNameLabel) {
		AdhocSaveFactory.adhocNameLabel = adhocNameLabel;
	}
	
	public static Boolean menuItemSaveDisable() {
		return menuItemSave.isDisable();
	}
}
