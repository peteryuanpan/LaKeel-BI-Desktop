package com.legendapl.lightning.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.ws.rs.ProcessingException;

import org.apache.commons.lang3.StringUtils;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.JFXTreeView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.model.FolderResource;
import com.legendapl.lightning.model.RecentItem;
import com.legendapl.lightning.model.TableRecord;
import com.legendapl.lightning.service.ExecuteAPIService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * リポジトリ画面のコントローラクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class P02RepoController extends C01ToolbarController {

	@FXML
	private AnchorPane anchorPane;
	@FXML
	private JFXTextField dirPath;

	/** 画面に表示されるフォルダ表示用Treeview */
	@FXML
	private JFXTreeView<FolderResource> folderTree;

	/** 画面に表示されるリポジトリ表示用TableView */
	@FXML
	private JFXTreeTableView<TableRecord> repositoryTable;
	@FXML
	private JFXTreeTableColumn<TableRecord, String> name;
	@FXML
	private JFXTreeTableColumn<TableRecord, String> type;
	@FXML
	private JFXTreeTableColumn<TableRecord, String> description;
	@FXML
	private JFXTreeTableColumn<TableRecord, String> createDate;
	@FXML
	private JFXTreeTableColumn<TableRecord, String> updateDate;
	@FXML
	private JFXTreeTableColumn<TableRecord, String> uri;

	@FXML
	private HBox executeBox;

	@FXML
	private StackPane spinnerPane;

	/** バックグラウンドスレッドから設定されるAPIの実行結果 */
	public static List<ClientResourceLookup> directoryList;
	public static List<ClientResourceLookup> reportUnitList;

	/** フォルダアイコン */
	private MaterialDesignIcon folderIcon = MaterialDesignIcon.FOLDER_OUTLINE;
	private FontAwesomeIcon folderExpandedIcon = FontAwesomeIcon.FOLDER_OPEN_ALT;

	/** フォルダ遷移時の追加先フォルダ */
	private TreeItem<FolderResource> parentItem;

	/** リストビューの列幅の比率 */
	public static final double repositoryRatio = 0.65;
	public static final double nameRatio = 0.3;
	public static final double descriptionRatio = 0.7;
	public static final double createDateWidth = 100.0;
	public static final double updateDateWidth = 100.0;

	/**
	 * 初期化
	 * 
	 * @param location,resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// preferencesを読み込み
		loadPreferences();
		// 列幅の設定
		double activeWidth = currentStage.getWidth() * repositoryRatio - createDateWidth - updateDateWidth;
		name.setPrefWidth(activeWidth * nameRatio);
		description.setPrefWidth(activeWidth * descriptionRatio);
		createDate.setPrefWidth(createDateWidth);
		updateDate.setPrefWidth(updateDateWidth);

		anchorPane.getChildren().remove(spinnerPane);

		uri.visibleProperty().setValue(false);

		// TreeViewに設定するため、rootのTreeItemを作成
		TreeItem<FolderResource> root = new TreeItem<FolderResource>(new FolderResource("root"),
				new MaterialDesignIconView(folderIcon));
		root.setExpanded(true);

		if (serverInfo.getOrganizationName().isEmpty()) {
			// 組織名がない場合はルートフォルダを展開
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					timeOutFlag = addTreeItems(root, "/");
					return null;
				}
			};
			Thread thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();
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

			// 組織名がある場合は組織名のrootフォルダとPublicフォルダを追加
			FolderResource publicFoloder = new FolderResource("Public");
			publicFoloder.setUri("/public");
			MaterialDesignIconView public_graphic = new MaterialDesignIconView(folderIcon);
			public_graphic.setCursor(Cursor.HAND);
			root.getChildren().add(new TreeItem<FolderResource>(publicFoloder, public_graphic));

			FolderResource organizationFoloder = new FolderResource(serverInfo.getOrganizationName());
			organizationFoloder.setUri("/");
			MaterialDesignIconView org_graphic = new MaterialDesignIconView(folderIcon);
			org_graphic.setCursor(Cursor.HAND);
			root.getChildren().add(new TreeItem<FolderResource>(organizationFoloder, org_graphic));

		}

		dirPath.setText("/");

		// フォルダ表示用TreeViewに取得したツリーを設定
		folderTree.setRoot(root);

		// フォルダ選択時のイベントハンドラを追加
		folderTree.setOnMouseClicked((e) -> handleMouseClicked(e));

		// ディレクトリパスを編集不可能に設定
		dirPath.setEditable(false);

		// レポートが選択された際にボタンを有効化するイベントハンドラを追加
		executeBox.setDisable(true);
		repositoryTable.getSelectionModel().selectedItemProperty().addListener((record, oldVal, newVal) -> {
			if (null == newVal) {
				executeBox.setDisable(true);
			} else {
				executeBox.setDisable(false);
			}
		});

		// 空のときの表示を設定
		repositoryTable.setPlaceholder(new Label(myResource.getString("P02.table.message.empty")));

		if (object instanceof FolderResource) {
			anchorPane.getChildren().add(spinnerPane);
			FolderResource folder = (FolderResource) object;
			logger.info("Move folder to " + folder.getUri());
			setObject(null);

			logger.debug("folder is" + folder.getUri());
		}

		// FIXME:特定のフォルダに遷移できない
		// FIXME:ラベルの文字が太くなる問題を回避
		// TODO:リソースIDではなく、ラベルで表記する
		// TODO:パス入力後Enterキー入力で遷移

	}

	/**
	 * 指定されたフォルダへ直接移動する
	 * 
	 * @param folderResource
	 */
	public void moveFolder(FolderResource folderResource, Double windowWidth) {

		// 列幅の設定
		if (null != windowWidth) {
			Platform.runLater(() -> {
				double activeWidth = windowWidth * repositoryRatio - createDateWidth - updateDateWidth;
				name.setPrefWidth(activeWidth * nameRatio);
				description.setPrefWidth(activeWidth * descriptionRatio);
				createDate.setPrefWidth(createDateWidth);
				updateDate.setPrefWidth(updateDateWidth);
			});
		}

		try {
			// uriを分割
			String[] splitUri = folderResource.getUri().split("/");
			// uri結合結果の文字列を設定
			StringBuffer stringBuffer = new StringBuffer();
			// 子フォルダを追加する位置を設定
			int index = 0;

			// TreeViewに設定するためのTreeItemを作成
			MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
			graphic.setCursor(Cursor.HAND);
			TreeItem<FolderResource> root = folderTree.getRoot();

			// 組織名がある場合は組織名のrootフォルダとPublicフォルダに対応
			if (serverInfo.getOrganizationName().isEmpty() || folderResource.getUri().startsWith("/public")) {
				parentItem = root;
			} else {
				Task<Void> task = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						timeOutFlag = addTreeItems(root.getChildren().get(1), "/");
						return null;
					}
				};
				Thread thread = new Thread(task);
				thread.setDaemon(true);
				thread.start();
				parentItem = root.getChildren().get(1);
			}

			// フォルダの中身を表示
			boolean folderIsExsist = addTableViewItems(folderResource.getUri());

			// 該当のディレクトリが存在しない場合は処理を中断し、エラーダイアログを表示
			if (!folderIsExsist) {
				Platform.runLater(() -> {
					showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
							messageRes.getString(Error.ERROR_P02_02));
					anchorPane.getChildren().remove(spinnerPane);
				});
				logger.error(myResource.getString("common.error.dialog.title") + folderResource.getUri());
				return;
			}

			// uri毎にアイテムを追加
			for (String uri : splitUri) {
				// rootフォルダの場合をスキップ
				if (uri.isEmpty()) {
					continue;
				}

				// 分割後のuriを確認
				logger.debug(uri);

				// uriを結合
				stringBuffer.append("/");
				stringBuffer.append(uri);
				logger.debug("uri=" + stringBuffer.toString());

				// uriが一致するアイテムを探し、子フォルダを追加
				int count = 0;
				int maxRepeat = 1000;
				while (count < maxRepeat) {
					index = findFolderIndex(stringBuffer.toString(), parentItem);
					Thread.sleep(10);
					count++;
					if (index == -1)
						continue;
					addTreeItems(parentItem.getChildren().get(index), stringBuffer.toString());
					break;
				}

				// 次回追加するノードを選択
				parentItem = parentItem.getChildren().get(index);
			}
			logger.info("Moving has done.");

			// フォルダを選択
			Platform.runLater(() -> {
				folderTree.getSelectionModel().select(parentItem);
				folderTree.scrollTo(folderTree.getRow(parentItem));
				dirPath.setText(folderResource.getUri());
				anchorPane.getChildren().remove(spinnerPane);
			});

			// TODO：レポートを選択（引数の型を変更し、レポート名を引き継ぐ必要あり）
			// repositoryTable.getSelectionModel().select()

		} catch (Exception e) {
			Platform.runLater(() -> {
				anchorPane.getChildren().remove(spinnerPane);
			});
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	/**
	 * 指定されたuriを持つ位置を探索
	 * 
	 * @param path
	 * @param rootItem
	 */
	private int findFolderIndex(String path, TreeItem<FolderResource> rootItem) {
		// 親ノードのTreeItemを格納する変数を作成
		FolderResource itemInTree = new FolderResource();

		// 親ノードから1つずつ取得しながら位置を探索し、一致個所を返す
		for (int index = 0; index < rootItem.getChildren().size(); index++) {
			itemInTree = rootItem.getChildren().get(index).getValue();
			// uriを比較
			if (null != itemInTree && path.equals(itemInTree.getUri())) {
				// 一致したTreeItemに子フォルダ位置を返す
				return (index);
			}
		}
		return -1;
	}

	boolean timeOutFlag = false;

	/**
	 * フォルダツリークリック時の処理
	 * 
	 * @param event
	 */
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

		// uriが設定されていない場合、またはrootフォルダに対してはフォルダ展開を禁止
		if (StringUtils.isEmpty(resource.getUri()))
			return;

		// フォルダの中身を表示
		addTableViewItems(resource.getUri());
		dirPath.setText(resource.getUri());

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

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				timeOutFlag = addTreeItems(item, resource.getUri());
				return null;
			}
		};
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();

	}

	/**
	 * 指定されたパス以下のフォルダを取得し、ツリービューのアイテムとして追加する。
	 * 
	 * @param root
	 * @param path
	 * @return root
	 */
	private boolean addTreeItems(TreeItem<FolderResource> root, String path) {

		// アイテムをクリアする
		root.getChildren().clear();

		// ロード中のアイコンを設定
		JFXSpinner loadingFolderIcon = new JFXSpinner();
		loadingFolderIcon.setRadius(5);
		Platform.runLater(() -> {
			root.setGraphic(loadingFolderIcon);
		});

		try {
			ClientResourceListWrapper clientResourceListWrapper = ExecuteAPIService.getDirectory(path);
			if (null != clientResourceListWrapper && null != clientResourceListWrapper.getResourceLookups()) {
				directoryList = clientResourceListWrapper.getResourceLookups();

				// 既に中身が入っていたり、他のスレッドにより追加結果が重複する場合の対策
				if (!root.getChildren().isEmpty()) {
					return false;
				}

				// 実行結果を1件ずつroot直下に追加
				for (ClientResourceLookup resource : directoryList) {
					Platform.runLater(() -> {
						MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
						graphic.setCursor(Cursor.HAND);
						root.getChildren().add(new TreeItem<FolderResource>(new FolderResource(resource), graphic));
					});
					root.setExpanded(true);
				}
				// 展開後にフォルダアイコンを変更
				Platform.runLater(() -> {
					root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
				});

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
				return true;
			} else {
				// フォルダの中にフォルダがない場合は、展開されたアイコンを設定
				Platform.runLater(() -> {
					root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
				});
				return false;
			}

			// API実行時のタイムアウトエラー
		} catch (ProcessingException e) {
			logger.error(messageRes.getString(Error.ERROR_P02_01), e);
			Platform.runLater(() -> {
				// エラー発生時に開こうとしたフォルダのアイコンと待機中画面を元に戻す
				MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
				root.setGraphic(graphic);
				anchorPane.getChildren().remove(spinnerPane);
				// エラーダイアログを表示
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(Error.ERROR_P02_01));
			});
			logger.error(messageRes.getString(Error.ERROR_P02_01));
			return false;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 指定されたパス以下のレポートリソースを取得し、テーブルビューのアイテムとして追加する。
	 * 
	 * @param root
	 * @param path
	 */
	@SuppressWarnings("unchecked")
	private boolean addTableViewItems(String path) {

		// 列幅の更新
		double activeWidth = repositoryTable.getWidth() - createDateWidth - updateDateWidth;
		name.setPrefWidth(activeWidth * nameRatio);
		description.setPrefWidth(activeWidth * descriptionRatio);
		createDate.setPrefWidth(createDateWidth);
		updateDate.setPrefWidth(updateDateWidth);

		try {
			reportUnitList = ExecuteAPIService.getReportUnit(path).getResourceLookups();
		} catch (NullPointerException e) {
			// レポートが存在しなかった場合
			Platform.runLater(() -> {
				repositoryTable.getColumns().setAll(name, description, createDate, updateDate);
				repositoryTable.setRoot(null);
				repositoryTable.setShowRoot(false);
				repositoryTable.setEditable(true);
			});
			return false;

			// API実行時のタイムアウトエラー
		} catch (ProcessingException e) {
			logger.error(messageRes.getString(Error.ERROR_P02_01), e);
			Platform.runLater(() -> {
				anchorPane.getChildren().remove(spinnerPane);
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(Error.ERROR_P02_01));
			});
			logger.error(messageRes.getString(Error.ERROR_P02_01));
			return false;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		// TableViewのレコードを定義
		ObservableList<TableRecord> tableRecord = FXCollections.observableArrayList();

		// レポートリソースを追加していく
		for (int i = 0; i < reportUnitList.size(); i++) {
			tableRecord.add(new TableRecord(reportUnitList.get(i).getLabel(), reportUnitList.get(i).getResourceType(),
					reportUnitList.get(i).getDescription(), reportUnitList.get(i).getCreationDate().substring(2, 10),
					reportUnitList.get(i).getUpdateDate().substring(2, 10), reportUnitList.get(i).getUri()));
		}

		/**
		 * 以下、TableViewをJFXTreeTableViewに置換する際の処理
		 * 
		 * 参考:
		 * https://github.com/jfoenixadmin/JFoenix/blob/master/demo/src/main/java/demos/components/TreeTableDemo.java
		 */
		TreeItem<TableRecord> root = new RecursiveTreeItem<>(tableRecord, RecursiveTreeObject::getChildren);

		name.setCellValueFactory((TreeTableColumn.CellDataFeatures<TableRecord, String> param) -> {
			if (name.validateValue(param)) {
				return param.getValue().getValue().getName();
			} else {
				return name.getComputedValue(param);
			}
		});
		description.setCellValueFactory((TreeTableColumn.CellDataFeatures<TableRecord, String> param) -> {
			if (description.validateValue(param)) {
				return param.getValue().getValue().getDescription();
			} else {
				return description.getComputedValue(param);
			}
		});
		createDate.setCellValueFactory((TreeTableColumn.CellDataFeatures<TableRecord, String> param) -> {
			if (createDate.validateValue(param)) {
				return param.getValue().getValue().getCreateDate();
			} else {
				return createDate.getComputedValue(param);
			}
		});
		updateDate.setCellValueFactory((TreeTableColumn.CellDataFeatures<TableRecord, String> param) -> {
			if (updateDate.validateValue(param)) {
				return param.getValue().getValue().getUpdateDate();
			} else {
				return updateDate.getComputedValue(param);
			}
		});
		uri.setCellValueFactory((TreeTableColumn.CellDataFeatures<TableRecord, String> param) -> {
			if (uri.validateValue(param)) {
				return param.getValue().getValue().getUri();
			} else {
				return uri.getComputedValue(param);
			}
		});

		Platform.runLater(() -> {
			// グルーピングを不可にする
			name.setContextMenu(null);
			description.setContextMenu(null);
			createDate.setContextMenu(null);
			updateDate.setContextMenu(null);

			repositoryTable.getColumns().setAll(name, description, createDate, updateDate);
			repositoryTable.setRoot(root);
			repositoryTable.setShowRoot(false);
			repositoryTable.setEditable(true);

			// ダブルクリック時にレポートを実行
			repositoryTable.setOnMouseClicked((event) -> {
				// 選択されたオブジェクトがない場合は処理を抜ける
				if (null == repositoryTable.getSelectionModel().getSelectedItem()) {
					return;
				}
				// 左クリックかつ、ダブルクリックでない場合は処理を抜ける。
				if (!(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)) {
					return;
				}

				// レポートを実行する
				try {
					showReportPane((ActionEvent)null);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			});
		});

		return true;

	}

	/**
	 * レポート実行画面を出力
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void showReportPane(ActionEvent event) throws IOException {

		if (null == repositoryTable.getSelectionModel().getSelectedItem()) {
			logger.warn("Report is not selected!");
			return;
		}

		// 選択されたレポートのテーブルレコードを特定
		TableRecord record = repositoryTable.getSelectionModel().getSelectedItem().getValue();
		
		// レポート実行画面を出力
		showReportPane(event, record);
	}
	
	/**
	 * レポート実行画面を出力
	 * 
	 * @param event
	 * @param uri
	 * @throws IOException
	 * @author panyuan
	 */
	public void showReportPane(String uri) throws IOException {
		
		for (int i = 0; /* i < MAX */; i++) {
			TableRecord record = repositoryTable.getTreeItem(i).getValue();
			if (record == null) {
				break;
			}
			if (uri != null && uri.equals(record.getUri().getValue())) {
				showReportPane(null, record);
				break;
			}
		}
	}
	
	/**
	 * レポート実行画面を出力
	 * 
	 * @param event
	 * @param record
	 * @throws IOException
	 */
	private void showReportPane(ActionEvent event, TableRecord record) throws IOException {
		
		// 実行するレポートを画面遷移先にセット
		setObject(record.getUri().getValue());

		Stage reportStage = showPane(event, "/view/W04InputControlAnchorPane.fxml", record.getName().getValue(),
				Modality.NONE, null);

		// 画面サイズの取得
		loadWindowSize();
		Double displayWidth = virtualBounds.getWidth();
		// ウィンドウ位置が記録されているか確認。
		if (preferences.getReportWindowY() != null && preferences.getReportWindowX() != null) {
			// 記録されたウィンドウ位置
			Double preferencesX = Double.parseDouble(preferences.getReportWindowX());
			Double preferencesY = Double.parseDouble(preferences.getReportWindowY());
			Double preferencesWidth = Double.parseDouble(preferences.getReportWindowWidth());
			Double preferencesHeight = Double.parseDouble(preferences.getReportWindowHeight());

			// preferences.xmlからウィンドウ幅を取得
			reportStage.setWidth(preferencesWidth);
			reportStage.setHeight(preferencesHeight);

			// 記録されている位置が現在のディスプレイの範囲以内か確認

			// サブディスプレイが左側の場合
			if (virtualBounds.x < 0) {
				if (preferencesX >= virtualBounds.x - preferencesHeight) {
					reportStage.setX(preferencesX);
					reportStage.setY(preferencesY);
				} else {
					reportStage.setX(0);
					reportStage.setY(0);
				}
			}
			// サブディスプレイが右側、もしくは無い場合
			if (virtualBounds.x >= 0) {
				if (0 <= preferencesX && preferencesX <= displayWidth - 20) {
					reportStage.setX(preferencesX);
					reportStage.setY(preferencesY);
				} else {
					reportStage.setX(0);
					reportStage.setY(0);
				}
			}
		}

		// エクスポート時にキャンセルボタンを押下するか、レポート画面生成時にエラーが発生したた場合は処理を中断
		if (null != object && object instanceof Boolean && false == (Boolean) object) {
			setObject(null);
			return;
		}
		controller.setStage(reportStage);

		reportStage.setOnCloseRequest((WindowEvent t) -> {
			try {
				logger.debug("close&cancel");
				((W04InputControlController) (this.getController())).close(t);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		});

		// ウィンドウ最小幅を設定
		reportStage.setMinWidth(Constant.Graphic.REPORT_STAGE_MIN_WIDTH);
		reportStage.setMinHeight(Constant.Graphic.REPORT_STAGE_MIN_HEIGHT);

		reportStageList.add(reportStage);
		reportStage.show();

		// 最近表示されたアイテムにあるものかを比較し、10件以下またはない場合のみ追加
		RecentItem item = new RecentItem(record);

		// 既に最近表示されたアイテムに含まれていた場合は、位置を最後尾に変更
		int itemIndex = 0;
		for (RecentItem compareItem : serverInfo.getRecentItems()) {
			if (compareItem.getReportURI().equals(item.getReportURI())) {
				logger.debug("Already exists.");
				break;
			}
			itemIndex++;
		}
		if (itemIndex < serverInfo.getRecentItems().size()) {
			serverInfo.getRecentItems().remove(itemIndex);
		}

		// 既に最近表示されたアイテムに含まれていなかった場合は、アイテムを追加
		serverInfo.addRecentItem(record);

		// 10件を超えていた場合は一番古いものを削除
		if (Constant.Application.RECENT_ITEM_SIZE < serverInfo.getRecentItems().size()) {
			serverInfo.getRecentItems().remove(Constant.Application.RECENT_ITEM_SIZE);
			logger.debug("The last runned item removed.");
		}

		logger.debug("Size is " + serverInfo.getRecentItems().size());
		logger.debug("Added into " + itemIndex + 1);

		// 最近表示されたアイテムの情報を保存
		int serverInfoIndex = serverInfoList.getServers().indexOf(serverInfo);
		serverInfoList.getServers().set(serverInfoIndex, serverInfo);
		serverInfoList.savePreferenceDataToFile();
		logger.debug("Runned item was saved.");
	}

	/**
	 * エクスポート(CSV,XLSX,PDF)ボタンを押下した際のイベントハンドラ
	 * 
	 * レポート実行画面を表示する。 エクスポートの場合はそれぞれファイル形式名を持ったStringを引数に渡す。
	 * 
	 * @param event
	 */
	public void export(ActionEvent event) throws IOException {

		if (null == repositoryTable.getSelectionModel().getSelectedItem()) {
			logger.warn("Report is not selected!");
			return;
		}

		// 選択されたレポートのテーブルレコードを特定
		TableRecord record = repositoryTable.getSelectionModel().getSelectedItem().getValue();
		if (null == record) {
			return;
		}

		String eventStr = event.getTarget().toString();
		String fileFormat = null;

		if (eventStr.contains(Constant.ExportFileformat.CSV))
			fileFormat = Constant.ExportFileformat.CSV;
		if (eventStr.contains(Constant.ExportFileformat.XLSX))
			fileFormat = Constant.ExportFileformat.XLSX;
		if (eventStr.contains(Constant.ExportFileformat.PDF))
			fileFormat = Constant.ExportFileformat.PDF;

		String[] uriAndFileFormat = { record.getUri().getValue(), fileFormat };
		setObject(uriAndFileFormat);

		// 入力コントロールがある場合は、レポート実行画面を表示
		Stage reportStage = showPane(event, "/view/W04InputControlAnchorPane.fxml", record.getName().getValue(),
				Modality.NONE, null);

		// 画面サイズの取得
		loadWindowSize();
		Double displayWidth = virtualBounds.getWidth();

		// ウィンドウ位置が記録されているか確認。
		if (preferences.getReportWindowY() != null && preferences.getReportWindowX() != null) {
			// 記録されたウィンドウ位置
			Double preferencesX = Double.parseDouble(preferences.getReportWindowX());
			Double preferencesY = Double.parseDouble(preferences.getReportWindowY());
			Double preferencesWidth = Double.parseDouble(preferences.getReportWindowWidth());
			Double preferencesHeight = Double.parseDouble(preferences.getReportWindowHeight());

			// preferences.xmlからウィンドウ幅を取得
			reportStage.setWidth(preferencesWidth);
			reportStage.setHeight(preferencesHeight);

			// 記録されている位置が現在のディスプレイの範囲以内か確認

			// サブディスプレイが左側の場合
			if (virtualBounds.x < 0) {
				if (preferencesX >= virtualBounds.x - preferencesHeight) {
					reportStage.setX(preferencesX);
					reportStage.setY(preferencesY);
				} else {
					reportStage.setX(0);
					reportStage.setY(0);
				}
			}
			// サブディスプレイが右側、もしくは無い場合
			if (virtualBounds.x >= 0) {
				if (0 <= preferencesX && preferencesX <= displayWidth - 20) {
					reportStage.setX(preferencesX);
					reportStage.setY(preferencesY);
				} else {
					reportStage.setX(0);
					reportStage.setY(0);
				}
			}
		}

		// エクスポートがキャンセルされた場合
		if (null != object && object instanceof Boolean && false == (Boolean) object) {
			setObject(null);
			return;
		}

		reportStage.setOnCloseRequest((WindowEvent t) -> {
			try {
				logger.debug("close&cancel");
				((W04InputControlController) (this.getController())).close(t);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		});

		controller.setStage(reportStage);

		reportStageList.add(reportStage);
		reportStage.show();

	}
}
