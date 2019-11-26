package com.legendapl.lightning.adhoc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * トピック編集画面のメニューのコントローラクラスです。
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class C110TopicMenuPane extends C100AdhocBaseAnchorPane {

	@FXML
	protected static JFXButton selectField;
	
	@FXML
	protected static JFXButton filterField;
	
	@FXML
	protected static JFXButton displayField;
	
	protected static String currentFieldName = "";

	//　保存のため
	protected static String fileName = "";
	protected static String filePath = "";
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void selectField(ActionEvent event) throws IOException {
		switchRoot(getTitle("P111.window.title"), "/view/P111TopicSelectAnchorPane.fxml", topicStage, true);
		currentFieldName = AdhocConstants.MenuType.SELECT;
		logger.info("Management page: " + currentFieldName);
	}

	public void filterField(ActionEvent event) throws IOException {
		switchRoot(getTitle("P112.window.title"), "/view/P112TopicFilterAnchorPane.fxml", topicStage, false);
		currentFieldName = AdhocConstants.MenuType.FILTER;
		logger.info("Management page: " + currentFieldName);
	}

	public void displayField(ActionEvent event) throws IOException {
		switchRoot(getTitle("P113.window.title"), "/view/P113TopicDisplayAnchorPane.fxml", topicStage, false);
		currentFieldName = AdhocConstants.MenuType.DISPLAY;
		logger.info("Management page: " + currentFieldName);
	}

	@Override
	protected String getTitle(String windowId) {
		return AdhocUtils.getString(windowId) + " (" + fileName + ")";
	}
	
}
