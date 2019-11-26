package com.legendapl.lightning.adhoc.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.jfoenix.controls.JFXTextField;
import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.ModelType;
import com.legendapl.lightning.adhoc.factory.AdhocSaveFactory;
import com.legendapl.lightning.adhoc.model.DatabaseInfo;
import com.legendapl.lightning.adhoc.model.FolderResource;
import com.legendapl.lightning.adhoc.model.LoadDataRow;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.recentItem.AdhocRecentItem;
import com.legendapl.lightning.adhoc.recentItem.AdhocRecentItemService;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.LocalFileDeleteService;
import com.legendapl.lightning.adhoc.service.ShareDataService;
import com.legendapl.lightning.adhoc.service.XMLTransferService;
import com.legendapl.lightning.model.DataSourceImpl;
import com.legendapl.lightning.tools.data.AdhocData;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;

/**
 * ローカルからトデータを読み込む画面のコントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class P102LocalDataAnchorPane extends P100LoadDataBaseAnchorPane {
	
	@FXML
	private Button doAdhoc;
	@FXML
	private Button doTopic;
	@FXML
	private Button doDelete;
	@FXML
	private JFXTextField dirPath;
	@FXML
	private TreeView<FolderResource> folderTree;
	@FXML
	private TreeView<FolderResource> fileList;
	@FXML
	private TreeTableView<LoadDataRow> treeTableView;
	@FXML
	private TreeTableColumn<LoadDataRow, String> fileName;
	@FXML
	private TreeTableColumn<LoadDataRow, String> fileType;
	@FXML
	private TreeTableColumn<LoadDataRow, String> createTime;
	@FXML
	private TreeTableColumn<LoadDataRow, String> updateTime;
	
	private ObservableList<LoadDataRow> dataRowList = FXCollections.observableArrayList();
	private List<TreeTableColumn<LoadDataRow, String>> columnObjectList;
	private List<String> functionNameList;
	private Map<String, List<FolderResource>> mapUriToShowDataList;
	private String lastPath = null;
	
	public static LoadDataRow clickedDataRow = null;
	public static ObservableList<String> deleteUris = FXCollections.observableArrayList();
	public static Boolean runFunAfterLoading = false;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		logger.debug("initialize");

		columnObjectList = new ArrayList<TreeTableColumn<LoadDataRow, String>>();
		columnObjectList.add(fileName);
		columnObjectList.add(fileType);
		columnObjectList.add(createTime);
		columnObjectList.add(updateTime);
		
		functionNameList = new ArrayList<String>();
		functionNameList.add("getFileName");
		functionNameList.add("getFileType");
		functionNameList.add("getCreateTime");
		functionNameList.add("getUpdateTime");
		
		folderTree.setVisible(true);
		fileList.setVisible(false);
		dirPath.setText(null);
		lastPath = null;
		
		folderTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		doAdhoc.disableProperty().bind(doAdhocButtonDisable());
		doTopic.disableProperty().bind(doTopicButtonDisable());
		doDelete.disableProperty().bind(doDeleteButtonDisable());
		
		treeTableView.setOnMouseClicked((event) -> handleActionOnMouseClickedTreeTableView(event));
		
		logger.debug("Loading list and tree folder.");
		loadListAndTree();
	}
	
	/**
	 * 切り替えボタンのイベント
	 * 
	 * @param event
	 */
	public void cutoverFired(ActionEvent event) {
		super.cutoverFired(event);
		Platform.runLater(() -> {
			// initialize
			final TreeItem<FolderResource> resource = turnTree ? 
					folderTree.getSelectionModel().getSelectedItem() :
					fileList.getSelectionModel().getSelectedItem();
			final List<TreeItem<FolderResource>> resources = turnTree ?
					folderTree.getSelectionModel().getSelectedItems() :
					fileList.getSelectionModel().getSelectedItems() ;
			final String uri;
			if (!resources.contains(resource)) {
				if (1 == resources.size()) uri = resources.get(0).getValue().getUri();
				else uri = null;
			}
			else uri = resource.getValue().getUri();
			// deleteUris
			AdhocUtils.setAll(deleteUris, getDeleteUrisByResources(resources));
			// dirPath
			dirPath.clear();
			dirPath.setText(getUriForShow(uri));
		});
	}

	/**
	 * 
	 * @return
	 */
	private ObservableValue<? extends Boolean> doAdhocButtonDisable() {
		return new BooleanBinding() {
			{
				super.bind(fileList.getSelectionModel().getSelectedItems(),
						treeTableView.getSelectionModel().getSelectedItems(), deleteUris);
			}
			@Override
			protected boolean computeValue() {
				List<TreeItem<FolderResource>> resources = fileList.getSelectionModel().getSelectedItems();
				List<TreeItem<LoadDataRow>> rows = treeTableView.getSelectionModel().getSelectedItems();
				if (null == resources && null == rows)
					return true;
				if (null != resources && resources.size() > 1)
					return true;
				if (null != rows && rows.size() > 1)
					return true;
				if (null != resources && 1 == resources.size() && !turnTree) {
					String bundle = resources.get(0).getValue().getFileType();
					if (ModelType.TOPIC == getTypeByBundleString(bundle))
						return false;
				}
				if (null != rows && 1 == rows.size()) {
					String bundle = rows.get(0).getValue().getFileType().get();
					if (ModelType.TOPIC == getTypeByBundleString(bundle))
						return false;
				}
				return true;
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	private ObservableValue<? extends Boolean> doTopicButtonDisable() {
		return new BooleanBinding() {
			{
				super.bind(fileList.getSelectionModel().getSelectedItems(),
						treeTableView.getSelectionModel().getSelectedItems(), deleteUris);
			}
			@Override
			protected boolean computeValue() {
				List<TreeItem<FolderResource>> resources = fileList.getSelectionModel().getSelectedItems();
				List<TreeItem<LoadDataRow>> rows = treeTableView.getSelectionModel().getSelectedItems();
				if (null == resources && null == rows)
					return true;
				if (null != resources && resources.size() > 1)
					return true;
				if (null != rows && rows.size() > 1)
					return true;
				if (null != resources && 1 == resources.size() && !turnTree) {
					String bundle = resources.get(0).getValue().getFileType();
					if (ModelType.TOPIC == getTypeByBundleString(bundle))
						return false;
					if (ModelType.ADHOC == getTypeByBundleString(bundle))
						return false;
				}
				if (null != rows && 1 == rows.size()) {
					String bundle = rows.get(0).getValue().getFileType().get();
					if (ModelType.TOPIC == getTypeByBundleString(bundle))
						return false;
					if (ModelType.ADHOC == getTypeByBundleString(bundle))
						return false;
				}
				return true;
			}
		};
	}

	/**
	 * アドホックビュー編集画面を開く
	 * 
	 * @param event
	 */
	public void doAdhoc(ActionEvent event) {

		logger.debug("doAdhoc");
		if (null == clickedDataRow) {
			logger.warn("clickedDataRow is null.");
			return;
		}

		final ModelType fromType = getTypeByBundleString(clickedDataRow.getFileType().get());

		backW.run(() -> {
			Adhoc adhoc = null;

			switch (fromType) {
			case TOPIC:
				// ローカルからトピックデータを取得
				// トピックデータからアドホックデータに変換
				try {
					AdhocSaveFactory.fromAdhoc = false;
					AdhocSaveFactory.fileName = AdhocUtils.getString("P121.newAdhocNameLabel.text");
					AdhocSaveFactory.filePath = null;
					File file = new File(clickedDataRow.getUri());
					Topic topic = XMLTransferService.loadTopicFromFile(file);
					topic.setOthers();
					adhoc = new Adhoc(topic);
					adhoc.setTopicName(clickedDataRow.getFileName().get());
					adhoc.setOthers();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
							AdhocUtils.getString("ERROR_READ_TOPIC_DATA"));
					throw new Exception(e);
				}
				break;
			case ADHOC:
			default:
				// ローカルからアドホックデータを取得
				try {
					AdhocSaveFactory.fromAdhoc = true;
					AdhocSaveFactory.fileName = clickedDataRow.getFileName().get();
					AdhocSaveFactory.filePath = clickedDataRow.getUri();
					File file = new File(clickedDataRow.getUri());
					adhoc = XMLTransferService.loadAdhocFromFile(file);
					adhoc.setOthers();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
							AdhocUtils.getString("ERROR_READ_TOPIC_DATA"));
					throw new Exception(e);
				}
				break;
			}

			// トピックツリーをチェック
			if (null == adhoc.getTopicTree()) {
				NullPointerException e = new NullPointerException(
						AdhocUtils.getString("ERROR_READ_TOPIC_TREE_NULL_EMPTY"));
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"), e.getMessage());
				throw e;
			}

			// データベースをチェク
			GetDatabaseFuncImplAdhoc func = new GetDatabaseFuncImplAdhoc(adhoc);
			getDatabase(func);

			// データを保存
			setObject(adhoc);
			ShareDataService.share(adhoc);
		}, () -> {
			// その後、アドホックビュー編集画面を開く
			try {
				String paneFXML = "/view/P121AdhocAnchorPane.fxml";
				adhocStage = showPane(event, paneFXML, getTitle("P121", AdhocUtils.bundleMessage),
						Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
				AdhocData.roots.put(paneFXML, adhocStage.getScene().getRoot());
				adhocStage.setMinWidth(AdhocConstants.Graphic.ADHOC_STAGE_MIN_WIDTH);
				adhocStage.setMinHeight(AdhocConstants.Graphic.ADHOC_STAGE_MIN_HEIGHT);
				adhocStage.setMaximized(true);
				adhocStage.show();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
						AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
				return;
			}

			// 最近アイテムを追加
			AdhocRecentItem recentItem = new AdhocRecentItem();
			recentItem.setReportLabel(clickedDataRow.getFileName().get());
			recentItem.setReportURI(clickedDataRow.getUri());
			recentItem.setAdhocFromType(fromType);
			serverInfo.getRecentItems().remove(recentItem);
			serverInfo.getRecentItems().add(0, recentItem);
		});
	}

	/**
	 * 
	 * @return
	 */
	private ObservableValue<? extends Boolean> doDeleteButtonDisable() {
		return new BooleanBinding() {
			{
				super.bind(deleteUris);
			}
			@Override
			protected boolean computeValue() {
				if (deleteUris.contains("adhoc")) { // can not delete root folder
					return true;
				}
				return deleteUris.isEmpty();
			}
		};
	}

	/**
	 * @throws Exception
	 */
	private class GetDatabaseFuncImplAdhoc implements GetDatabaseFunc {
		Adhoc adhoc;
		public GetDatabaseFuncImplAdhoc(Adhoc adhoc) {
			this.adhoc = adhoc;
		}
		@Override
		public DatabaseInfo get() throws Exception {
			DatabaseInfo database = adhoc.getDatabase();
			DataSourceImpl datasource = database.getDataSourceFromLocal();
			String password = datasource.getPassword();
			if (null == password || password.isEmpty()) {
				throw new PasswordNullEmptyException();
			}
			return database;
		}
	}

	/**
	 * 編集画面を開く
	 * 
	 * @param event
	 */
	public void doTopic(ActionEvent event) {

		logger.debug("doTopic");
		if (null == clickedDataRow) {
			logger.warn("clickedDataRow is null.");
			return;
		}

		final ModelType fromType = getTypeByBundleString(clickedDataRow.getFileType().get());
		switch (fromType) {
		case TOPIC:
			showTopicView(event);
			break;
		case ADHOC:
			showAdhocView(event, fromType);
			break;
		default:
			break;
		}
	}

	public void showTopicView (ActionEvent event) {
		backW.run(
		() -> {
			// まず、ローカルからトピックデータを取得
			// トピックデータからトピックのJAVAクラスに変換
			Topic topic;
			try {
				File file = new File(clickedDataRow.getUri());
				topic = XMLTransferService.loadTopicFromFile(file);
				topic.setOthers();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"), 
						AdhocUtils.getString("ERROR_READ_TOPIC_DATA"));
				throw new Exception(e);
			}
			
			//　データベースをチェク
			GetDatabaseFuncImplTopic func = new GetDatabaseFuncImplTopic(topic);
			getDatabase(func);
			
			// データを保存
			setObject(topic);
			ShareDataService.share(topic);
		},
		() -> {
			// その後、トピック編集画面を開く
			try {
				C110TopicMenuPane.filePath = clickedDataRow.getUri();
				C110TopicMenuPane.fileName = clickedDataRow.getFileName().get();
				String paneFXML = "/view/P111TopicSelectAnchorPane.fxml";
				topicStage = showPane(event, paneFXML, 
									  getTopicTitle("P111.window.title", C110TopicMenuPane.fileName), 
									  Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
				AdhocData.roots.put(paneFXML, topicStage.getScene().getRoot());
				topicStage.setMinWidth(AdhocConstants.Graphic.TOPIC_STAGE_MIN_WIDTH);
				topicStage.setMinHeight(AdhocConstants.Graphic.TOPIC_STAGE_MIN_HEIGHT);
				topicStage.show();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"), 
						AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
				return;
			}
		});
	}

	public void showAdhocView(ActionEvent event, ModelType fromType) {
		backW.run(() -> {
			Adhoc adhoc = null;

			// ローカルからアドホックデータを取得
			try {
				AdhocSaveFactory.fromAdhoc = true;
				AdhocSaveFactory.fileName = clickedDataRow.getFileName().get();
				AdhocSaveFactory.filePath = clickedDataRow.getUri();
				File file = new File(clickedDataRow.getUri());
				adhoc = XMLTransferService.loadAdhocFromFile(file);
				adhoc.setOthers();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
						AdhocUtils.getString("ERROR_READ_TOPIC_DATA"));
				throw new Exception(e);
			}

			// トピックツリーをチェック
			if (null == adhoc.getTopicTree()) {
				NullPointerException e = new NullPointerException(
						AdhocUtils.getString("ERROR_READ_TOPIC_TREE_NULL_EMPTY"));
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"), e.getMessage());
				throw e;
			}

			// データベースをチェク
			GetDatabaseFuncImplAdhoc func = new GetDatabaseFuncImplAdhoc(adhoc);
			getDatabase(func);

			// データを保存
			setObject(adhoc);
			ShareDataService.share(adhoc);
		}, () -> {
			// その後、アドホックビュー編集画面を開く
			try {
				String paneFXML = "/view/P121AdhocAnchorPane.fxml";
				adhocStage = showPane(event, paneFXML, getTitle("P121", AdhocUtils.bundleMessage),
						Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
				AdhocData.roots.put(paneFXML, adhocStage.getScene().getRoot());
				adhocStage.setMinWidth(AdhocConstants.Graphic.ADHOC_STAGE_MIN_WIDTH);
				adhocStage.setMinHeight(AdhocConstants.Graphic.ADHOC_STAGE_MIN_HEIGHT);
				adhocStage.setMaximized(true);
				adhocStage.show();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
						AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
				return;
			}

			// 最近アイテムを追加
			AdhocRecentItem recentItem = new AdhocRecentItem();
			recentItem.setReportLabel(clickedDataRow.getFileName().get());
			recentItem.setReportURI(clickedDataRow.getUri());
			recentItem.setAdhocFromType(fromType);
			serverInfo.getRecentItems().remove(recentItem);
			serverInfo.getRecentItems().add(0, recentItem);
		});
	}

	/**
	 * @throws Exception 
	 */
	private class GetDatabaseFuncImplTopic implements GetDatabaseFunc {
		Topic topic;
		public GetDatabaseFuncImplTopic(Topic topic) {
			this.topic = topic;
		}
		@Override
		public DatabaseInfo get() throws Exception {
			DatabaseInfo database = topic.getDatabase();
			DataSourceImpl datasource = database.getDataSourceFromLocal();
			String password = datasource.getPassword();
			if (null == password || password.isEmpty()) {
				throw new PasswordNullEmptyException();
			}
			return database;
		}
	}
	
	/**
	 * 最近アイテムから<br>
	 * アドホックビュー編集画面を開く
	 * 
	 * @param recentItem
	 */
	public void doRecentItem(ActionEvent event) {
		
		logger.debug("doRecentItem");
		if (null == clickedDataRow) {
			logger.warn("clickedDataRow is null.");
			return;
		}
		
		// リストモドルに切り替えする
		if (turnTree) {
			cutoverFired(event);
		}
		
		// リストアイテムを選択する
		final String uri = clickedDataRow.getUri();
		final ModelType fromType = getTypeByBundleString(clickedDataRow.getFileType().get());
		fileList.getRoot().getChildren().forEach(fileItem -> {
			if (null != fileItem) {
				if (fileItem.getValue().getUri().equals(uri)) {
					try {
						fileList.getSelectionModel().clearSelection();
						fileList.getSelectionModel().select(fileItem);
						dirPath.clear();
						dirPath.setText(getUriForShow(uri));
						lastPath = uri;
						showDataOnTable(uri);
						switch (fromType) {
						case TOPIC:
							showTopicView(null);
							break;
						case ADHOC:
							showAdhocView(null, fromType);
							break;
						default:
							break;
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					return;
				}
			}
		});
	}

	/**
	 * ファイルを削除する確認
	 * @param event
	 */
	public void doDeleteConfirm(ActionEvent event) {
		Alert alert = AlertWindowService.getAlertConfirm(AdhocUtils.getString("ALERT_CONFIRM_DELETE_TOPIC"));
		Optional<ButtonType> result = alert.showAndWait();
		if (ButtonType.OK == result.get()) {
			doDelete(event);
		} else {
			event.consume();
		}
	}

	/**
	 * フォルダまたはファイルを削除する
	 * @param event
	 */
	public void doDelete(ActionEvent event) {
		
		logger.debug("doDelete");
		if (null == deleteUris || deleteUris.isEmpty()) {
			logger.warn("deleteUris is null or empty.");
			return;
		}
		
		// initialize
		List<Exception> exceptions = new ArrayList<>();
		
		backW.run(() -> {
			// for each deleteUri
			for (String deleteUri : deleteUris) {
				File file = new File(deleteUri);
				if (null != file && file.exists()) {
					try {
						try {
							// delete file
							LocalFileDeleteService.delete(file);
						} catch (java.nio.file.FileSystemException e) {
							if (e.getClass() == java.nio.file.FileSystemException.class) {
								// if delete failed because of process in use
								// run system garbage collections
								// delete again
								System.gc();
								Thread.sleep(1000);
								LocalFileDeleteService.delete(file);
							} else {
								// else throw exception
								throw new Exception(e);
							}
						}
						
					} catch (Exception e) {
						// delete failed, add exception to list
						exceptions.add(e);
					}
				}
			}
		},
		() -> {
			if (exceptions.isEmpty()) {
				// delete all successfully
				AlertWindowService.showInfoNotInBack(AdhocUtils.getString("SUCCESS_DELETE_FILES"));
			} else {
				// delete failed, deal with exceptions
				String errors = new String();
				for (Exception e : exceptions) {
					logger.info(e.getMessage(), e);
					errors = errors + e.getMessage();
				}
				AlertWindowService.showInfoNotInBack(AdhocUtils.getString("ERROR_DELETE_FILES"), errors);
			}
			
			// refresh pane
			refresh.fire();
			
			// refresh recent items
			// TODO
		});
	}
	
	/**
	 * テーブルのアイテムを押すイベント
	 * 
	 * @param event
	 */
	private void handleActionOnMouseClickedTreeTableView(MouseEvent event) {
		
		// check
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode) {
			return;
		}
		
		// clear
		folderTree.getSelectionModel().clearSelection();
		fileList.getSelectionModel().clearSelection();
		
		// initialize
		final List<TreeItem<LoadDataRow>> rows = treeTableView.getSelectionModel().getSelectedItems();
		final TreeItem<LoadDataRow> row = treeTableView.getSelectionModel().getSelectedItem();
		final LoadDataRow value;
		final String uri;
		if (!rows.contains(row)) {
			if (1 == rows.size()) {
				value = rows.get(0).getValue();
				uri = value.getUri();
			}
			else {
				value = null;
				uri = null;
			}
		}
		else {
			value = row.getValue();
			uri = value.getUri();
		}
		
		// deleteUris
		AdhocUtils.setAll(deleteUris, getDeleteUrisByRows(rows));
		
		// clickedDataRow
		clickedDataRow = value;
		
		// dirPath
		dirPath.clear();
		dirPath.setText(getUriForShow(uri));
		
		// double Clicked
		if (MouseButton.PRIMARY.equals(event.getButton()) && 2 == event.getClickCount()) {
			if (1 == rows.size()) {
				final ModelType fromType = getTypeByBundleString(clickedDataRow.getFileType().get());
				switch (fromType) {
				case TOPIC:
					showTopicView(null);
					break;
				case ADHOC:
					showAdhocView(null, fromType);
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * ファイルリストのアイテムを押すイベント
	 * 
	 * @param event
	 */
	private void handleActionOnMouseClickedFileList(MouseEvent event) {
		
		// check
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode || 
				null == fileList.getSelectionModel() ||
				null == fileList.getSelectionModel().getSelectedItem() ||
				null == fileList.getSelectionModel().getSelectedItem().getValue()) {
			return;
		}
		
		// clear
		treeTableView.getSelectionModel().clearSelection();
		
		// initialize
		final List<TreeItem<FolderResource>> resources = fileList.getSelectionModel().getSelectedItems();
		final TreeItem<FolderResource> resource = fileList.getSelectionModel().getSelectedItem();
		final FolderResource value;
		final String uri;
		if (!resources.contains(resource)) {
			if (1 == resources.size()) {
				value = resources.get(0).getValue();
				uri = value.getUri();
			}
			else {
				value = null;
				uri = null;
			}
		}
		else {
			value = resource.getValue();
			uri = value.getUri();
		}
		
		// deleteUris
		AdhocUtils.setAll(deleteUris, getDeleteUrisByResources(resources));
		
		// dirPath
		dirPath.clear();
		dirPath.setText(getUriForShow(uri));
		
		// show
		lastPath = uri;
		showDataOnTable(uri);
		
		// clickedDataRow
		if (null != value) {
			clickedDataRow = new LoadDataRow();
			clickedDataRow.setFileType(value.getFileType());
			clickedDataRow.setFileName(value.getLabel());
			clickedDataRow.setUri(value.getUri());
		}
		
		// double Clicked
		if (MouseButton.PRIMARY.equals(event.getButton()) && 2 == event.getClickCount()) {
			if (1 == resources.size()) {
				final ModelType fromType = getTypeByBundleString(clickedDataRow.getFileType().get());
				switch (fromType) {
				case TOPIC:
					showTopicView(null);
					break;
				case ADHOC:
					showAdhocView(null, fromType);
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * ツリーフォルダのアイテムを押すイベント
	 * 
	 * @param event
	 */
	private void handleActionOnMouseClickedFolderTree(MouseEvent event) {
		
		// check
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode || 
				null == folderTree.getSelectionModel() ||
				null == folderTree.getSelectionModel().getSelectedItem() ||
				null == folderTree.getSelectionModel().getSelectedItem().getValue()) {
			return;
		}
		
		// clear
		treeTableView.getSelectionModel().clearSelection();
		
		// initialize
		final List<TreeItem<FolderResource>> resources = folderTree.getSelectionModel().getSelectedItems();
		final TreeItem<FolderResource> resource = folderTree.getSelectionModel().getSelectedItem();
		final FolderResource value;
		final String uri;
		if (!resources.contains(resource)) {
			if (1 == resources.size()) {
				value = resources.get(0).getValue();
				uri = value.getUri();
			}
			else {
				value = null;
				uri = null;
			}
		}
		else {
			value = resource.getValue();
			uri = value.getUri();
		}
		
		// deleteUris
		AdhocUtils.setAll(deleteUris, getDeleteUrisByResources(resources));
		
		// dirPath
		dirPath.clear();
		dirPath.setText(getUriForShow(uri));
		
		// check
		if (null != lastPath && lastPath.equals(uri) &&
				!(clickedNode instanceof MaterialDesignIconView)) {
			return;
		}
		
		// setExpanded
		if (clickedNode instanceof MaterialDesignIconView) {
			TreeItem<FolderResource> item = folderTree.getSelectionModel().getSelectedItem();
			item.setExpanded(true);
		}
		
		// show
		lastPath = uri;
		showDataOnTable(uri);
	}

	/**
	 * 
	 * @param uri
	 */
	private void showDataOnTable(String uri) {
		
		dataRowList = FXCollections.observableArrayList();
		
		List<FolderResource> files = mapUriToShowDataList.get(uri);
		if (files != null) {
			for (FolderResource file : files) {
				if (file.getFileType() == AdhocUtils.getString("P100.fileType.UNKNOW")) {
					continue;
				}
				LoadDataRow dataRow = new LoadDataRow(
						file.getLabel(),
				   		null, // description
				   		file.getFileType(),
				   		file.getCreationDate(),
				   		file.getUpdateDate(),
				   		file.getUri()
				);
				dataRowList.add(dataRow);
			}
		}
		
		showData(dataRowList, columnObjectList, functionNameList, treeTableView);
	}

	/**
	 * 
	 */
	private void cleanBeforeLoading() {
		dirPath.clear();
		dirPath.setText(null);
		lastPath = null;
		clickedDataRow = null;
		deleteUris.clear();;
		dataRowList = FXCollections.observableArrayList();
		showData(dataRowList, columnObjectList, functionNameList, treeTableView);
	}

	/**
	 * 
	 */
	@Override protected void loadListAndTree() {
		
		cleanBeforeLoading();
		TreeItem<FolderResource> listRoot = new TreeItem<FolderResource>(new FolderResource("root"), new MaterialDesignIconView(folderIcon));
		TreeItem<FolderResource> treeRoot = new TreeItem<FolderResource>(new FolderResource("root"), new MaterialDesignIconView(folderIcon));
		
		backW.run(
		() -> {
			Map<String, FolderResource> map = new HashMap<>();
			List<String> uris = new ArrayList<>();
			List<FolderResource> resources = new ArrayList<>();
			
			FileVisitor<Path> fileVisitor = new FileVisitor<Path>() {
				@Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				@Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				@Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.TERMINATE;
				}
				@Override public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					try {
						return visitFileImpl(path, map, uris, resources, listRoot, treeRoot);
					} catch (Exception e) {
						throw new IOException(e);
					}
				}
			};
			
			Platform.runLater(() -> {
				try {
					loadListAndTreeImpl(fileVisitor, map, uris, resources, listRoot, treeRoot);
				} catch (Exception e) {
					logger.error("Failed to get list/tree from local.");
					logger.error(e.getMessage(), e);
					AlertWindowService.showExceptionMessage(AdhocUtils.getString("SERVER_ERROR_GET"), e);
				} finally {
					// show empty data at the last
					showData(dataRowList, columnObjectList, functionNameList, treeTableView);
				}
			});
			
			// sleep 500ms
			Thread.sleep(500);
		},
		() -> {
			// runFunAfterLoading
			Platform.runLater(() -> {
				if (runFunAfterLoading) {
					runFunAfterLoading = !runFunAfterLoading;
					clickedDataRow = AdhocRecentItemService.clickedDataRow;
					doRecentItem(null);
				}
			});
		});
		
		fileList.setRoot(listRoot);
		fileList.setShowRoot(false);
		fileList.setOnMouseClicked((e) -> handleActionOnMouseClickedFileList(e));
	
		folderTree.setRoot(treeRoot);
		folderTree.setShowRoot(false);
		folderTree.setOnMouseClicked((e) -> handleActionOnMouseClickedFolderTree(e));
	}
	
	/**
	 * 
	 * @param fileVisitor
	 * @param map
	 * @param uris
	 * @param resources
	 * @param listRoot
	 * @param treeRoot
	 * @throws IOException 
	 */
	private void loadListAndTreeImpl(
			FileVisitor<Path> fileVisitor, Map<String, FolderResource> map, 
			List<String> uris, List<FolderResource> resources, 
			TreeItem<FolderResource> listRoot, TreeItem<FolderResource> treeRoot) throws IOException {
		
		// insert list and tree
		Files.walkFileTree(Paths.get(AdhocConstants.Application.ADHOC_FILE_PATH), fileVisitor);
		
		// clean list data
		List<TreeItem<FolderResource>> removeChildren = new ArrayList<>();
		listRoot.getChildren().forEach(treeItem -> {
			if (treeItem.getValue().getFileType() == AdhocUtils.getString("P100.fileType.UNKNOW")) {
				removeChildren.add(treeItem);
			}
		});
		listRoot.getChildren().removeAll(removeChildren);
		
		// sort children by label
		sortChildrenByLabel(treeRoot);
		sortChildrenByLabel(listRoot);
		
		// insert map (mapUriToShowDataList)
		mapUriToShowDataList = new HashMap<>();
		for (int i = 0; i < uris.size(); i ++) {
			if (null == mapUriToShowDataList.get(uris.get(i))) {
				List<FolderResource> showDataList = new ArrayList<>();
				for (int j = 0; j < uris.size(); j ++) {
					if (uris.get(i).equals(uris.get(j))) {
						showDataList.add(resources.get(j));
					}
				}
				sortListByLabel(showDataList);
				mapUriToShowDataList.put(uris.get(i), showDataList);
			}
		}
		
		logger.info("List and Folder tree has been synced.");
	}

	/**
	 * 
	 * @param path
	 * @param map
	 * @param uris
	 * @param resources
	 * @param listRoot
	 * @param treeRoot
	 * @return
	 * @throws IOException
	 */
	private FileVisitResult visitFileImpl(
			Path path, Map<String, FolderResource> map, 
			List<String> uris, List<FolderResource> resources, 
			TreeItem<FolderResource> listRoot, TreeItem<FolderResource> treeRoot) throws IOException {
		
		// check
		File file = path.toFile();
		if (null == file || !file.exists() || !file.getName().endsWith(".xml")) {
			return FileVisitResult.CONTINUE;
		}
		// insert list
		TreeItem<FolderResource> leaf = new TreeItem<FolderResource>(new FolderResource(file));
		listRoot.getChildren().add(leaf);
		// insert tree
		String paths[] = path.toString().split(Pattern.quote("\\"));
		String treePath = new String("");
		TreeItem<FolderResource> node = treeRoot;
		for (int i = 0; i < paths.length; i++) {
			if (i != 0) treePath = treePath.concat("\\");
			treePath = treePath.concat(paths[i]);
			if (null == map.get(treePath)) {
				if (i + 1 != paths.length) {
					FolderResource resource = new FolderResource(FolderResource.transferName(paths[i]), treePath);
					map.put(treePath, resource);
				} else {
					map.put(treePath, leaf.getValue());
				}
			}
			FolderResource resource = new FolderResource(map.get(treePath));
			TreeItem<FolderResource> child = new TreeItem<FolderResource>(resource);
			TreeItem<FolderResource> childx = findChild(child, node.getChildren());
			if (null == childx) {
				if (i + 1 != paths.length) { // folder node
					setUnExpandedIcon(child);
					addListener(child);
					node.getChildren().add(child);
					node = child;
				} else { // leaf node
					uris.add(child.getValue().getUri());
					resources.add(resource);
					uris.add(node.getValue().getUri());
					resources.add(resource);
				}
			} else {
				node = childx;
			}
		}
		return FileVisitResult.CONTINUE;
	}
	
	/**
	 * 「child」が「children」にあるかどうかを判断する
	 * 
	 * @param child
	 * @param childs
	 * @return
	 */
	private TreeItem<FolderResource> findChild(TreeItem<FolderResource> child, List<TreeItem<FolderResource>> children) {
		if (child != null && children != null) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).getValue().getUri().equals(child.getValue().getUri())) {
					return children.get(i);
				}
			}
		}
		return null;
	}
}
