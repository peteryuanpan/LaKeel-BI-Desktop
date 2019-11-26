package com.legendapl.lightning.adhoc.controller;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.SaveFileChooserService;
import com.legendapl.lightning.adhoc.service.ShareDataService;
import com.legendapl.lightning.adhoc.service.XMLTransferService;
import com.legendapl.lightning.tools.data.AdhocData;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 *トピック編集画面の共通コントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public abstract class P110TopicBaseAnchorPane extends C110TopicMenuPane {
	
	@FXML
	protected JFXButton save;
	
	@FXML
	protected JFXButton cancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);
	}
	
	private class TopicSaveService extends SaveFileChooserService {
		public Topic topic;
		public TopicSaveService() {
			super();
			this.getProperty().setTitle(AdhocUtils.getString("P110.button.save"));
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
			XMLTransferService.saveDataToXML(topic, outputFile);
		}
		@Override protected void doFailed(File outputFile, Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showErrorNotInBack(AdhocUtils.getString("ERROR_SAVE_TITLE"), AdhocUtils.getString("ERROR_SAVE_EXPORT_FAILED"));
		}
		@Override protected void doSuccess(File outputFile) {
			logger.info("Saved Topic.");
			AlertWindowService.showInfoNotInBack(AdhocUtils.getString("SUCCESS_SAVE_TITLE"));
			cancel(null);
		}
	}
	private TopicSaveService saveService = new TopicSaveService();
	
	/**
	 *　保存ボタン
	 * 
	 * @param event
	 */
	public void save(ActionEvent event) {
		// データを取得する
		saveService.topic = ShareDataService.loadTopic();
		// 保存
		saveService.getProperty().setInitialFileName(C110TopicMenuPane.fileName);
		saveService.getProperty().setInitialDirectoryPath(getInitialDirectoryPath(C110TopicMenuPane.filePath));
		saveService.save(topicStage);
	}
	
	/**
	 * 保存パスを取得
	 */
	private String getInitialDirectoryPath(String filePath) {
		if (null == filePath || "".equals(filePath)) {
			return AdhocConstants.Application.ADHOC_FILE_PATH;
		} else {
			File parent = (new File(filePath)).getParentFile();
			return parent.getPath();
		}
	}
	
	/**
	 *　キャンセルボタン
	 * 
	 * @param event
	 */
	public void cancel(ActionEvent event) {
		
		ShareDataService.clear();
		AdhocData.roots.remove("/view/P111TopicSelectAnchorPane.fxml");
		AdhocData.roots.remove("/view/P112TopicFilterAnchorPane.fxml");
		AdhocData.roots.remove("/view/P113TopicDisplayAnchorPane.fxml");
		setObject(null);
		// TODO: 他のクエリの実装
		
		Platform.runLater(() -> {
			logger.info("Close the topic stage.");
			topicStage.hide();
			topicStage.close();
		}); 
	}
	
}
