package com.legendapl.lightning.adhoc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.service.AlertWindowService;

import javafx.event.ActionEvent;

/**
 * データを読み込む画面のメニューのコントローラクラスです。
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class C100AdhocMenuPane extends C100AdhocBaseAnchorPane {
	
	protected static String currentFieldName = "";
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void serverField(ActionEvent event) throws IOException {
		try {
			switchRoot(getTitle("P101", AdhocUtils.bundleMessage) + " (" + serverInfo.getName() + ")", "/view/P101ServerDataAnchorPane.fxml", currentStage);
			currentFieldName = AdhocConstants.MenuType.SERVER;
			logger.info("Management page: " + currentFieldName);
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("BI_SERVER_CONNECT_ERROR"));
		}
	}

	public void localField(ActionEvent event) throws IOException {
		try {
			switchRoot(getTitle("P102", AdhocUtils.bundleMessage) + " (" + serverInfo.getName() + ")",  "/view/P102LocalDataAnchorPane.fxml", currentStage);
			currentFieldName = AdhocConstants.MenuType.LOCAL;
			logger.info("Management page: " + currentFieldName);
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("BI_SERVER_CONNECT_ERROR"));
		}
	}
}
