
package com.legendapl.lightning.tools.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.controller.C00ControllerBase;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.data.ToolsData;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * 外部支援ツール画面のメニューのコントローラクラス
 * 
 * @author LAC_楊
 * @since 2017/9/5
 *
 */
public class C80ToolsMenuPane extends C00ControllerBase {
	
	/**
	 * 現在のタイトル
	 */
	protected static String currentToolName = "";

	public void userFired(ActionEvent event) throws IOException {
		switchRoot(getTitle("P81") + " (" + serverInfo.getName() + ")", "/view/P81UserAnchorPane.fxml");
		currentToolName = Constants.USER;
		logger.info("Management page: " + currentToolName);
	}
	
	public void roleFired(ActionEvent event) throws IOException {
		switchRoot(getTitle("P82") + " (" + serverInfo.getName() + ")", "/view/P82RoleAnchorPane.fxml");
		currentToolName = Constants.ROLE;
		logger.info("Management page: " + currentToolName);
	}
	
	public void scheduleFired(ActionEvent event) throws IOException {
		switchRoot(getTitle("P83") + " (" + serverInfo.getName() + ")", "/view/P83ScheduleAnchorPane.fxml");
		currentToolName = Constants.SCHEDULE;
		logger.info("Management page: " + currentToolName);
	}
	
	public void permissionFired(ActionEvent event) throws IOException {
		switchRoot(getTitle("P84") + " (" + serverInfo.getName() + ")", "/view/P84PermissionAnchorPane.fxml");
		currentToolName = Constants.PERMISSION;
		logger.info("Management page: " + currentToolName);
	}
	
	public void domainFired(ActionEvent event) throws IOException {
		switchRoot(getTitle("P85") + " (" + serverInfo.getName() + ")", "/view/P85DomainAnchorPane.fxml");
		currentToolName = Constants.DOMAIN;
		logger.info("Management page: " + currentToolName);
	}
	
	public void settingFired(ActionEvent event) throws IOException {
		switchRoot(getTitle("P89") + " (" + serverInfo.getName() + ")", "/view/P89SettingAnchorPane.fxml");
		currentToolName = Constants.SETTING;
		logger.info("Management page: " + currentToolName);
	}
	
	private void switchRoot(String title, String paneFXML) throws IOException {
		logger.debug("Swiching to new page : " + title);
		
		Parent root = ToolsData.roots.get(title);
		if(root == null) {
			logger.debug("Page not created, creating new page.");
			
			root = newRoot(paneFXML);
			ToolsData.roots.put(title, root);
		} else {
			logger.debug("Page created, switch to this page.");
		}
		Scene scene = root.getScene();
		if(scene != null) {
			//TODO ここ、何かできないか？
			scene.setRoot(new Parent() {
			});
		}
		currentStage.setTitle(title);
		currentStage.getScene().setRoot(root);
		root.requestFocus();
	}
	
	private Parent newRoot(String paneFXML) throws IOException {
		// 呼び出すダイアログのFXMLを開く
		FXMLLoader fxmlloader = new FXMLLoader(C00ControllerBase.class.getResource(paneFXML),
				ResourceBundle.getBundle(Constant.Application.MY_BUNDLE));
		// initializeを呼び出し
		Parent root = fxmlloader.load();
		// fxmlで指定されたControllerを格納
		setController(fxmlloader.getController());

		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
